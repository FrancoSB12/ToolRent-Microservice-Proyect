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
        Employee employee;
        try {
            employee = getEmployee(employeeRun);
        } catch (HttpClientErrorException.NotFound e) {
            //The employee microservice responded, but said that the employee doesn't exist
            throw new RuntimeException("Empleado no encontrado con RUN: " + employeeRun);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            //The employee microservice responded, but with another error (400, 401, 500, etc.)
            throw new RuntimeException("Error en servicio de empleados: " + e.getResponseBodyAsString());

        } catch (RestClientException e) {
            //The employee microservice didn't respond (Off, Timeout, DNS, etc.)
            throw new RuntimeException("El servicio de empleados está caído o no responde.");
        }

        //The client is searched in the database
        Client client;
        try {
            client = getClient(rent.getClientRun());
        } catch (HttpClientErrorException.NotFound e) {
            //The client microservice responded, but said that the client doesn't exist
            throw new RuntimeException("Cliente no encontrado con RUN: " + rent.getClientRun());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            //The client microservice responded, but with another error (400, 401, 500, etc.)
            throw new RuntimeException("Error en servicio de clientes: " + e.getResponseBodyAsString());

        } catch (RestClientException e) {
            //The client microservice didn't respond (Off, Timeout, DNS, etc.)
            throw new RuntimeException("El servicio de clientes está caído o no responde.");
        }

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
            Long toolTypeId = toolItemRequest.getToolTypeId();

            ToolItem toolItem;
            try {
                toolItem = getToolItem(toolItemId);
            } catch (HttpClientErrorException.NotFound e) {
                //The inventory microservice responded, but said that the tool item doesn't exist
                throw new RuntimeException("La herramienta con ID " + toolItemId + " no existe.");

            } catch (HttpClientErrorException | HttpServerErrorException e) {
                //The inventory microservice responded, but with another error (400, 401, 500, etc.)
                throw new RuntimeException("Error en servicio de inventario: " + e.getResponseBodyAsString());

            } catch (RestClientException e) {
                //The inventory microservice didn't respond (Off, Timeout, DNS, etc.)
                throw new RuntimeException("El servicio de inventario está caído o no responde.");
            }

            //The tool type from the tool item is saved
            ToolType toolType;
            try {
                toolType = getToolType(toolTypeId);
            } catch (HttpClientErrorException.NotFound e) {
                //The inventory microservice responded, but said that the tool item doesn't exist
                throw new RuntimeException("El tipo de herramienta con ID " + toolTypeId + " no existe.");

            } catch (HttpClientErrorException | HttpServerErrorException e) {
                //The inventory microservice responded, but with another error (400, 401, 500, etc.)
                throw new RuntimeException("Error en servicio de inventario: " + e.getResponseBodyAsString());

            } catch (RestClientException e) {
                //The inventory microservice didn't respond (Off, Timeout, DNS, etc.)
                throw new RuntimeException("El servicio de inventario está caído o no responde.");
            }

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
        // A. Actualizar estado de herramientas a "PRESTADA"
        for (RentXToolItemEntity item : toolItemsToSave) {
            //Link to the saved loan
            item.setRent(savedLoan);
            rentXToolItemService.save(item);

            try {
                //Update status
                updateStatus(item);
            } catch (RestClientException e) {
                throw new RuntimeException("Error actualizando estado en inventario. Datos inconsistentes.");
            }

            try {
                //Update Stock
                updateStock(item.getToolTypeId());
            } catch (RestClientException e) {
                throw new RuntimeException("Error actualizando stock en inventario. Datos inconsistentes.");
            }

            try {
                //Create associated kardex
                createKardex(item.getToolTypeId(), "PRESTAMO");
            } catch (RestClientException e) {
                throw new RuntimeException("Error creando kardex. Datos inconsistentes.");
            }
        }

        //Update the client active rents
        try {
            updateActiveRents(client);
        } catch (RestClientException e) {
            throw new RuntimeException("Error actualizando datos del cliente.");
        }

        return savedLoan;
    }

    //Private methods
    private Employee getEmployee(String run) {
        return restTemplate.getForObject("http://employee-service/employee/" + run, Employee.class);
    }

    private Client getClient(String run) {
        return restTemplate.getForObject("http://client-service/client/" + run, Client.class);
    }

    private ToolItem getToolItem(Long id) {
        return restTemplate.getForObject("http://inventory-service/inventory/tool-item/" + id, ToolItem.class);
    }

    private ToolType getToolType(Long id) {
        return restTemplate.getForObject("http://inventory-service/inventory/tool-type/" + id, ToolType.class);
    }

    private void updateStatus(RentXToolItemEntity item){
        ToolItem toolItem = getToolItem(item.getToolItemId());
        toolItem.setStatus("PRESTADA");
        restTemplate.put("http://inventory-service/inventory/tool-item/" + toolItem.getId(), toolItem, ToolItem.class);
    }

    private void updateStock(Long toolTypeId) {
        ChangeStockRequest changeStockRequest = new ChangeStockRequest(toolTypeId, -1);
        restTemplate.put("http://inventory-service/inventory/tool-type/change-available-stock", changeStockRequest, ToolType.class);
    }

    private void createKardex(Long toolTypeId, String operationType) {
        CreateKardexRequest createKardexRequest = new CreateKardexRequest(toolTypeId, operationType, 1);
        restTemplate.postForObject("http://kardex-service/kardex/entry", createKardexRequest, Void.class);
    }

    private void updateActiveRents(Client client) {
        client.setActiveRents(client.getActiveRents() + 1);
        restTemplate.put("http://client-service/client/" + client.getRun(), Client.class);
    }
}
