package com.toolrent.rentservice.Service;

import com.toolrent.rentservice.DTO.ChangeStockRequest;
import com.toolrent.rentservice.DTO.CreateKardexRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @Transactional
    public RentEntity createLoan(RentEntity rent, String employeeRun) {
        //The employee is searched in the database
        Employee employee = fetchEmployeeInDB(employeeRun);

        //The client is searched in the database
        Client client = fetchClientInDB(employeeRun);

        //Check that the client doesn't have a debt
        if (client.getStatus().equals("Restringido") || client.getDebt() > 0) {
            throw new RuntimeException("El cliente no puede arrendar debido a deudas impagas");
        }

        //Check that the client doesn't have (or will have) 5 active rents
        if(client.getActiveRents() >= 5){
            throw new RuntimeException("El cliente no puede tener más de 5 arriendos activos");
        }

        //Check that the client doesn't have any overdue rents
        List<RentEntity> overdueRents = rentRepository.findOverdueLoans(client.getRun(), LocalDate.now());
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
            Long toolItemId = toolItemRequest.getToolItemId();
            ToolItem toolItem = fetchToolItemInDB(toolItemId);

            //The tool type from the tool item is saved
            Long toolTypeId = toolItemRequest.getToolTypeId();
            ToolType toolType = fetchToolTypeInDB(toolTypeId);

            //The stock and status are validated
            if (!toolItem.getStatus().equals("DISPONIBLE") || toolType.getAvailableStock() < 1) {
                throw new RuntimeException("La herramienta " + toolItem.getSerialNumber() + " no está disponible.");
            }

            //it's verified that the client doesn't have a rented tool of this type
            if (rentedToolsIds.contains(toolTypeId)) {
                throw new RuntimeException("El cliente ya tiene arrendada una herramienta de tipo: " + toolType.getName());
            }

            //The relationship object is prepared for later storage
            toolItemRequest.setToolItemId(toolItem.getId());

            // CREAR SNAPSHOT (Copia local)
            RentXToolItemEntity newItem = new RentXToolItemEntity();
            newItem.setToolItemId(toolItemId);
            newItem.setToolNameSnapshot(toolType.getName());
            newItem.setSerialNumberSnapshot(toolItem.getSerialNumber());
            newItem.setAgreedPrice(toolType.getRentalFee());

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
        rent.setEmployeeNameSnapshot(employee.getName());

        RentEntity savedLoan = rentRepository.save(rent);

        //Create and save the associated kardex for each tool, reduce the tool stock and save the relationships
        for (RentXToolItemEntity item : toolItemsToSave) {
            //Link to the saved loan
            item.setRent(savedLoan);
            rentXToolItemService.save(item);

            //Update status
            updateStatus(item);

            //Update Stock
            updateStock(item.getToolTypeId());

            //Create associated kardex
            createKardex(item.getToolTypeId(), "PRESTAMO");
        }

        //Update the client active rents
        updateActiveRents(client);

        return savedLoan;
    }

    //Private methods
    private ToolItem getToolItem(Long id) {
        return restTemplate.getForObject("http://inventory-service/inventory/tool-item/" + id, ToolItem.class);
    }

    private void updateStatus(RentXToolItemEntity item){
        try {
            ToolItem toolItem = getToolItem(item.getToolItemId());
            toolItem.setStatus("PRESTADA");
            restTemplate.put("http://inventory-service/inventory/tool-item/" + toolItem.getId(), toolItem, ToolItem.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error actualizando estado en inventario. Datos inconsistentes.");
        }
    }

    private void updateStock(Long toolTypeId) {
        try {
            ChangeStockRequest changeStockRequest = new ChangeStockRequest(toolTypeId, -1);
            restTemplate.put("http://inventory-service/inventory/tool-type/change-available-stock", changeStockRequest, ToolType.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error actualizando stock en inventario. Datos inconsistentes.");
        }
    }

    private void createKardex(Long toolTypeId, String operationType) {
        try {
            CreateKardexRequest createKardexRequest = new CreateKardexRequest(toolTypeId, operationType, 1);
            restTemplate.postForObject("http://kardex-service/kardex/entry", createKardexRequest, Void.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error creando kardex. Datos inconsistentes.");
        }
    }

    private void updateActiveRents(Client client) {
        try {
            client.setActiveRents(client.getActiveRents() + 1);
            restTemplate.put("http://client-service/client/" + client.getRun(), Client.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error actualizando datos del cliente.");
        }
    }

    private Employee fetchEmployeeInDB(String run) {
        Employee employee;
        try {
            employee = restTemplate.getForObject("http://employee-service/employee/" + run, Employee.class);
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
            client = restTemplate.getForObject("http://client-service/client/" + run, Client.class);
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
}
