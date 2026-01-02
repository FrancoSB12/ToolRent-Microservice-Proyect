package com.toolrent.rentservice.Service;

import com.toolrent.rentservice.DTO.*;
import com.toolrent.rentservice.Entity.RentEntity;
import com.toolrent.rentservice.Entity.RentXToolItemEntity;
import com.toolrent.rentservice.Model.Client;
import com.toolrent.rentservice.Model.Employee;
import com.toolrent.rentservice.Model.ToolItem;
import com.toolrent.rentservice.Model.ToolType;
import com.toolrent.rentservice.Repository.RentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class RentService {
    RentRepository rentRepository;
    RentXToolItemService rentXToolItemService;
    RestTemplate restTemplate;

    @Autowired
    public RentService(RentRepository rentRepository, RentXToolItemService rentXToolItemService, RestTemplate restTemplate) {
        this.rentRepository = rentRepository;
        this.rentXToolItemService = rentXToolItemService;
        this.restTemplate = restTemplate;
    }

    public List<RentEntity> getAllRents(){
        return rentRepository.findAllWithDetails();
    }

    public Optional<RentEntity> getRentById(Long id){
        return rentRepository.findByIdWithDetails(id);
    }

    public List<RentEntity> getActiveRentsByClient(String clientRun){
        return rentRepository.findActiveRentsByClient(clientRun);
    }

    public List<RentEntity> getRentByStatus(String status){
        return rentRepository.findByStatusWithDetails(status);
    }

    public List<RentEntity> getRentByReturnDateBeforeAndValidity(){
        return rentRepository.findByReturnDateBeforeAndValidity(LocalDate.now(), "Atrasado");
    }

    public List<RentEntity> getRentByValidity(String validity){
        return rentRepository.findByValidity(validity);
    }

    public List<ToolRankingProjection> getToolRanking(LocalDate startDate, LocalDate endDate){
        return rentRepository.findTopRentedTools(startDate, endDate);
    }

    public List<Map<String, Object>> getMostRentedTools(LocalDate startDate, LocalDate endDate){
        List<Object[]> results = rentXToolItemService.getMostLoanedToolsBetweenDates(startDate, endDate);

        //The shape of the object is changed to display the name of the tool and the number of loans
        List<Map<String, Object>> loanedToolList = results.stream()
                .map(r -> Map.of(
                        "toolName", r[0],
                        "totalLoans", r[1]
                ))
                .toList();

        //The most borrowed tool(s) is/are sought
        List<Map<String, Object>> mostLoanedTools = new ArrayList<>();
        long maxLoans = 0L;

        for(Map<String, Object> mapTool : loanedToolList){
            Long totalLoans = (Long) mapTool.get("totalLoans");

            if(totalLoans > maxLoans){
                //If a tool is found that has more loans
                maxLoans = totalLoans;
                mostLoanedTools.clear();
                mostLoanedTools.add(mapTool);

            } else if(totalLoans == maxLoans){
                //If the tool has the same number of loans as the current one
                mostLoanedTools.add(mapTool);
            }
            //If totalLoans < maxLoans it does nothing
        }
        return mostLoanedTools;
    }

    public boolean exists(Long id){
        return rentRepository.existsById(id);
    }

    public boolean existsClient(String clientRun) {
        try {
            getClient(clientRun);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado verificando cliente");
        }
    }

    public boolean existsEmployee(String employeeRun){
        try {
            getEmployee(employeeRun);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado verificando empleado");
        }
    }

    @Transactional
    public RentEntity createRent(RentEntity rent, String employeeRun) {
        //The employee is searched in the database
        Employee employee = fetchEmployeeInDB(employeeRun);

        //The client is searched in the database
        Client client = fetchClientInDB(rent.getClientRun());

        //Check that the client doesn't have a debt
        if (client.getStatus().equals("Restringido") || client.getDebt() > 0) {
            throw new RuntimeException("El cliente no puede arrendar debido a deudas impagas");
        }

        //Check that the client doesn't have (or will have) 5 active rents
        if(client.getActiveRents() >= 5){
            throw new RuntimeException("El cliente no puede tener más de 5 arriendos activos");
        }

        //Check that the client doesn't have any overdue rents
        List<RentEntity> overdueRents = rentRepository.findClientOverdueRents(client.getRun(), LocalDate.now());
        if(!overdueRents.isEmpty()){
            throw new RuntimeException("El cliente tiene préstamos atrasados pendientes. No puede arrendar nuevo.");
        }

        //Save the rent tools list
        List<RentXToolItemEntity> toolItemsFromFront = rent.getRentTools();
        if (toolItemsFromFront == null || toolItemsFromFront.isEmpty()) {
            throw new RuntimeException("La lista de herramientas no puede estar vacía.");
        }

        //Tool processing
        Set<Long> rentedToolsIds = rentXToolItemService.getActiveLoansToolTypeIdsByClient(client.getRun());
        List<RentXToolItemEntity> toolItemsToSave = new ArrayList<>();

        for (RentXToolItemEntity toolItemRequest : toolItemsFromFront) {
            ToolItem toolItem = fetchToolItemInDB(toolItemRequest.getToolItemId());
            ToolType toolType = toolItem.getToolType();

            //The stock and status are validated
            if (!toolItem.getStatus().equals("DISPONIBLE") || toolType.getAvailableStock() < 1) {
                throw new RuntimeException("La herramienta " + toolItem.getSerialNumber() + " no está disponible.");
            }

            //it's verified that the client doesn't have a rented tool of this type
            if (rentedToolsIds.contains(toolType.getId())) {
                throw new RuntimeException("El cliente ya tiene arrendada una herramienta de tipo: " + toolType.getName());
            }

            //The relationship object is prepared for later storage
            toolItemRequest.setToolItemId(toolItem.getId());
            toolItemRequest.setToolTypeId(toolType.getId());
            toolItemRequest.setToolNameSnapshot(toolType.getName());
            toolItemRequest.setSerialNumberSnapshot(toolItem.getSerialNumber());

            toolItemsToSave.add(toolItemRequest);
        }

        //Check that the return date isn't before the rent date
        if(rent.getReturnDate().isBefore(rent.getRentDate())){
            throw new RuntimeException("La fecha de devolución es previa a la de entrega");
        }

        //If all tools have stock and the client doesn't have or hasn't exceeded the tool limit then save the loan
        rent.setClientRun(client.getRun());
        rent.setClientNameSnapshot(client.getName() + " " + client.getSurname());

        rent.setEmployeeRun(employee.getRun());
        rent.setEmployeeNameSnapshot(employee.getName()+ " " + employee.getSurname());

        //Set the late return fee
        Integer lateReturnFee = fetchLateReturnFeeInDB();
        rent.setLateReturnFee(lateReturnFee);

        RentEntity savedLoan = rentRepository.save(rent);

        //Create and save the associated kardex for each tool, reduce the tool stock and save the relationships
        for (RentXToolItemEntity item : toolItemsToSave) {
            //Link to the saved loan
            item.setRent(savedLoan);
            rentXToolItemService.save(item);

            //Update status
            ToolItem toolItem = getToolItem(item.getToolItemId());
            updateStatus(toolItem, "PRESTADA");

            //Update Stock
            updateAvailableStock(item.getToolTypeId(), -1);

            //Create associated kardex
            ToolType toolType = fetchToolTypeInDB(item.getToolTypeId());
            createKardex(toolType.getName(), "PRESTAMO", -1, employeeRun);
        }

        //Update the client active rents
        updateActiveRents(client, 1);

        return savedLoan;
    }

    @Transactional
    public RentEntity returnRent(Long id, RentReturnRequest returnRequest){
        //The rent is searched in the database
        Optional<RentEntity> dbLoan = getRentById(id);
        RentEntity dbRentEnt = dbLoan.get();

        //The client is searched in the database
        Client client = fetchClientInDB(dbRentEnt.getClientRun());

        //The list of tools from the frontend is converted into a map for quick searching
        Map<Long, String> toolDamageMap = new HashMap<>();
        if(returnRequest.getReturnedTools() != null){
            for (ToolReturnInfoDTO info : returnRequest.getReturnedTools()) {
                toolDamageMap.put(info.getToolItemId(), info.getDamageLevel());
            }
        }

        //Update stock and validity of the tool
        List<RentXToolItemEntity> dbRentItems = rentXToolItemService.getAllRentToolItemsByRent_Id(id);

        //The database is browsed
        for(RentXToolItemEntity dbRentItem : dbRentItems) {
            Long toolItemId = dbRentItem.getToolItemId();

            //Verify that this tool came from the frontend
            if (!toolDamageMap.containsKey(toolItemId)) {
                throw new RuntimeException("Falta información de devolución para la herramienta ID: " + toolItemId);
            }

            String reportedDamageLevel = toolDamageMap.get(toolItemId);

            //The tool info is obtained
            ToolItem remoteToolItem = fetchToolItemInDB(toolItemId);
            ToolType toolType = remoteToolItem.getToolType();

            //The update logic is applied
            if (reportedDamageLevel.equals("EN_EVALUACION")) {
                try {
                    remoteToolItem.setStatus("EN_REPARACION");
                    remoteToolItem.setDamageLevel(reportedDamageLevel);
                    restTemplate.put("http://inventory-service/inventory/tool-item/" + remoteToolItem.getId(), remoteToolItem, ToolItem.class);
                } catch (RestClientException e) {
                    throw new RuntimeException("Error actualizando estado en inventario. Datos inconsistentes.");
                }

                createKardex(toolType.getName(), "DEVOLUCION",1, dbRentEnt.getEmployeeRun());
                createKardex(toolType.getName(), "REPARACION", -1, dbRentEnt.getEmployeeRun());

                //Since the tool returned by the client is damaged, the client is prevented from requesting more tools until the damage level has been verified
                //If the tools were indeed in good condition, they will be "Active" again
                client.setStatus("Restringido");

            } else if(reportedDamageLevel.equals("NO_DANADA")){
                //"EN_EVALUACION" it's a placeholder until the tool damage is evaluated
                //If it isn't in under review then it's "No dañada"
                remoteToolItem.setStatus("DISPONIBLE");
                remoteToolItem.setDamageLevel("NO_DANADA");
                restTemplate.put("http://inventory-service/inventory/tool-item/" + remoteToolItem.getId(), remoteToolItem, ToolItem.class);

                updateAvailableStock(toolType.getId(), 1);

                createKardex(toolType.getName(), "DEVOLUCION", 1, dbRentEnt.getEmployeeRun());

            } else{
                throw new RuntimeException("La herramienta ya tiene un nivel de daño");
            }
        }

        //Decrease the client's active loans count
        updateActiveRents(client, -1);

        //If the client returned the tools late
        if(dbRentEnt.getReturnDate().isBefore(LocalDate.now())){
            //Late return fee calculation and change of status to the client
            ApplyLateFeeRequest lateFeeRequest = new ApplyLateFeeRequest(dbRentEnt.getReturnDate(), dbRentEnt.getLateReturnFee(), client);
            restTemplate.put("http://fee-service/fee/apply-late-return-fee", lateFeeRequest, Integer.class);

            dbRentEnt.setValidity("Atrasado");
        } else{
            dbRentEnt.setValidity("Puntual");
        }

        //Finish rent
        dbRentEnt.setReturnTime(LocalTime.now());
        dbRentEnt.setStatus("Finalizado");
        return rentRepository.save(dbRentEnt);
    }

    @Transactional
    public void checkAndSetLateStatuses() {
        List<RentEntity> activeRents = rentRepository.findByStatusWithDetails("Activo");

        for (RentEntity rent : activeRents) {
            if (rent.getReturnDate().isBefore(LocalDate.now())) {

                //Update the validity to 'Atrasado'
                rent.setValidity("Atrasado");
                rentRepository.save(rent);

                //Restrict the client if it isn't
                Client client = fetchClientInDB(rent.getClientRun());
                if (!client.getStatus().equals("Restringido")) {
                    try {
                        client.setStatus("Restringido");
                        restTemplate.put("http://client-service/client/" + client.getRun(), client, Client.class);
                    } catch (RestClientException e) {
                        throw new RuntimeException("Error actualizando datos del cliente.");
                    }
                }
            }
        }
    }

    //Private methods
    private Client getClient(String run){
        return restTemplate.getForObject("http://client-service/client/" + run, Client.class);
    }

    private Employee getEmployee(String run){
        return restTemplate.getForObject("http://employee-service/employee/" + run, Employee.class);
    }

    private ToolItem getToolItem(Long id) {
        return restTemplate.getForObject("http://inventory-service/inventory/tool-item/" + id, ToolItem.class);
    }

    private void updateStatus(ToolItem toolItem, String status){
        try {
            toolItem.setStatus(status);
            restTemplate.put("http://inventory-service/inventory/tool-item/" + toolItem.getId(), toolItem, ToolItem.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error actualizando estado en inventario. Datos inconsistentes.");
        }
    }

    private void updateAvailableStock(Long toolTypeId, Integer quantity) {
        try {
            ChangeStockRequest changeStockRequest = new ChangeStockRequest(toolTypeId, quantity);
            restTemplate.put("http://inventory-service/inventory/tool-type/change-available-stock", changeStockRequest, ToolType.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error actualizando stock en inventario. Datos inconsistentes.");
        }
    }

    private void createKardex(String toolTypeName, String operationType, Integer stock, String employeeRun) {
        try {
            CreateKardexRequest createKardexRequest = new CreateKardexRequest(toolTypeName, operationType, stock, employeeRun);
            restTemplate.postForObject("http://kardex-service/kardex/entry", createKardexRequest, Void.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error creando kardex. Datos inconsistentes.");
        }
    }

    private void updateActiveRents(Client client, Integer quantity) {
        try {
            client.setActiveRents(client.getActiveRents() + quantity);
            restTemplate.put("http://client-service/client/" + client.getRun(), client, Client.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error actualizando datos del cliente.");
        }
    }

    private Employee fetchEmployeeInDB(String run) {
        Employee employee;
        try {
            employee = getEmployee(run);
        } catch (HttpClientErrorException.NotFound e) {
            //The employee microservice responded, but said that the employee doesn't exist
            throw new RuntimeException("Empleado no encontrado con RUN: " + run);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            //The employee microservice responded, but with another error (400, 401, 500, etc.)
            throw new RuntimeException("Error en servicio de empleados: " + e.getResponseBodyAsString());

        } catch (RestClientException e) {
            //The employee microservice didn't respond (Off, Timeout, DNS, etc.)
            throw new RuntimeException("El servicio de empleados está caído o no responde.");
        }
        return employee;
    }

    private Client fetchClientInDB(String run) {
        Client client;
        try {
            client = getClient(run);
        } catch (HttpClientErrorException.NotFound e) {
            //The client microservice responded, but said that the client doesn't exist
            throw new RuntimeException("Cliente no encontrado con RUN: " + run);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            //The client microservice responded, but with another error (400, 401, 500, etc.)
            throw new RuntimeException("Error en servicio de clientes: " + e.getResponseBodyAsString());

        } catch (RestClientException e) {
            //The client microservice didn't respond (Off, Timeout, DNS, etc.)
            throw new RuntimeException("El servicio de clientes está caído o no responde.");
        }

        return client;
    }

    private ToolItem fetchToolItemInDB(Long id) {
        ToolItem toolItem;
        try {
            toolItem = getToolItem(id);
        } catch (HttpClientErrorException.NotFound e) {
            //The inventory microservice responded, but said that the tool item doesn't exist
            throw new RuntimeException("La herramienta con ID " + id + " no existe.");

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            //The inventory microservice responded, but with another error (400, 401, 500, etc.)
            throw new RuntimeException("Error en servicio de inventario: " + e.getResponseBodyAsString());

        } catch (RestClientException e) {
            //The inventory microservice didn't respond (Off, Timeout, DNS, etc.)
            throw new RuntimeException("El servicio de inventario está caído o no responde.");
        }

        return toolItem;
    }

    private ToolType fetchToolTypeInDB(Long id) {
        ToolType toolType;
        try {
            toolType = restTemplate.getForObject("http://inventory-service/inventory/tool-type/" + id, ToolType.class);
        } catch (HttpClientErrorException.NotFound e) {
            //The inventory microservice responded, but said that the tool item doesn't exist
            throw new RuntimeException("El tipo de herramienta con ID " + id + " no existe.");

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            //The inventory microservice responded, but with another error (400, 401, 500, etc.)
            throw new RuntimeException("Error en servicio de inventario: " + e.getResponseBodyAsString());

        } catch (RestClientException e) {
            //The inventory microservice didn't respond (Off, Timeout, DNS, etc.)
            throw new RuntimeException("El servicio de inventario está caído o no responde.");
        }

        return toolType;
    }

    private Integer fetchLateReturnFeeInDB() {
        Integer lateReturnFee;
        try {
            lateReturnFee = restTemplate.getForObject("http://fee-service/fee/current-late-return-fee", Integer.class);
        } catch (HttpClientErrorException.NotFound e) {
            //The fee microservice responded, but said that the client doesn't exist
            throw new RuntimeException("Multa por atraso no encontrada");

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            //The fee microservice responded, but with another error (400, 401, 500, etc.)
            throw new RuntimeException("Error en servicio de multas: " + e.getResponseBodyAsString());

        } catch (RestClientException e) {
            //The fee microservice didn't respond (Off, Timeout, DNS, etc.)
            throw new RuntimeException("El servicio de multas está caído o no responde.");
        }

        return lateReturnFee;
    }
}
