package com.toolrent.inventoryservice.Service;

import com.toolrent.inventoryservice.DTO.CreateKardexRequest;
import com.toolrent.inventoryservice.Entity.ToolTypeEntity;
import com.toolrent.inventoryservice.Model.Employee;
import com.toolrent.inventoryservice.Repository.ToolTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ToolTypeService {
    private final RestTemplate restTemplate;
    ToolTypeRepository toolTypeRepository;
    ToolValidationService toolValidationService;

    @Autowired
    public ToolTypeService(RestTemplate restTemplate, ToolTypeRepository toolTypeRepository, ToolValidationService toolValidationService) {
        this.restTemplate = restTemplate;
        this.toolTypeRepository = toolTypeRepository;
        this.toolValidationService = toolValidationService;
    }

    public List<ToolTypeEntity> getAllToolTypes(){
        return toolTypeRepository.findAll();
    }

    public Optional<ToolTypeEntity> getToolTypeById(Long id){
        return toolTypeRepository.findById(id);
    }

    public Optional<ToolTypeEntity> getToolTypeByName(String name){
        return toolTypeRepository.findByName(name);
    }

    public List<ToolTypeEntity> getToolTypesByToolItemIds(List<Long> toolItemIds){
        return toolTypeRepository.findToolTypesByIds(toolItemIds);
    }

    @Transactional
    public ToolTypeEntity createToolType(ToolTypeEntity newToolType, String employeeRun){
        //Save the toolType
        ToolTypeEntity savedToolType = toolTypeRepository.save(newToolType);

        Employee employee;
        try {
            employee = restTemplate.getForObject("http://employee-service/employee/" + employeeRun, Employee.class);
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

        if(employee == null){
            throw new RuntimeException("Empleado no encontrado con RUN: " + employeeRun);
        }

        //Create and save the associated kardex
        CreateKardexRequest createKardexRequest = new CreateKardexRequest(newToolType.getName(), "REGISTRO", 0, employeeRun, employee.getName() + " " + employee.getSurname());
        String url = "http://kardex-service/kardex/entry";
        restTemplate.postForObject(url, createKardexRequest, Void.class);

        return savedToolType;
    }

    public boolean exists(Long id){
        return toolTypeRepository.existsById(id);
    }

    public ToolTypeEntity updateToolType(Long id, ToolTypeEntity toolType){
        Optional<ToolTypeEntity> dbToolType = getToolTypeById(id);
        ToolTypeEntity dbToolTypeEnt = dbToolType.get();

        if(toolType.getName() != null){
            if(toolValidationService.isInvalidToolName(toolType.getName())){
                throw new IllegalArgumentException("Nombre de la herramienta inválido");
            }
            String oldName = dbToolTypeEnt.getName();
            if(!oldName.equals(toolType.getName())){
                try {
                    String url = "http://kardex-service/kardex/sync-tool-name";
                    Map<String, String> body = new HashMap<>();
                    body.put("name", oldName);
                    body.put("newName", toolType.getName());

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

                    restTemplate.exchange(
                            url,
                            HttpMethod.PUT,
                            requestEntity,
                            Void.class
                    );
                } catch (Exception e) {
                    System.err.println("Error sincronizando nombre con Kardex: " + e.getMessage());
                }
            }
            dbToolTypeEnt.setName(toolType.getName());
        }

        if(toolType.getCategory() != null){
            if(toolValidationService.isInvalidName(toolType.getCategory())){
                throw new IllegalArgumentException("Categoría de la herramienta inválido");
            }
            dbToolTypeEnt.setCategory(toolType.getCategory());
        }

        if(toolType.getModel() != null){
            if(toolValidationService.isInvalidToolName(toolType.getModel())){
                throw new IllegalArgumentException("Modelo de la herramienta inválido");
            }
            dbToolTypeEnt.setModel(toolType.getModel());
        }

        if(toolType.getReplacementValue() != null){
            if(toolValidationService.isInvalidStockOrFee(toolType.getReplacementValue())){
                throw new IllegalArgumentException("Valor de reposición de la herramienta inválida");
            }
            dbToolTypeEnt.setReplacementValue(toolType.getReplacementValue());
        }

        if(toolType.getTotalStock() != null){
            if(toolValidationService.isInvalidStockOrFee(toolType.getTotalStock())){
                throw new IllegalArgumentException("Valor del stock total de la herramienta inválido");
            }
            dbToolTypeEnt.setTotalStock(toolType.getTotalStock());
        }

        if(toolType.getAvailableStock() != null){
            if(toolValidationService.isInvalidStockOrFee(toolType.getAvailableStock())){
                throw new IllegalArgumentException("Valor del stock disponible de la herramienta inválido");
            }
            dbToolTypeEnt.setAvailableStock(toolType.getAvailableStock());
        }

        if(toolType.getRentalFee() != null){
            if(toolValidationService.isInvalidStockOrFee(toolType.getRentalFee())){
                throw new IllegalArgumentException("Tarifa de arriendo de la herramienta invalida");
            }
            dbToolTypeEnt.setRentalFee(toolType.getRentalFee());
        }

        if(toolType.getDamageFee() != null){
            if(toolValidationService.isInvalidStockOrFee(toolType.getDamageFee())){
                throw new IllegalArgumentException("Tarifa de daño de la herramienta invalida");
            }
            dbToolTypeEnt.setDamageFee(toolType.getDamageFee());
        }

        return toolTypeRepository.save(dbToolTypeEnt);
    }

    public ToolTypeEntity changeAvailableStock(Long id, Integer quantity){
        Optional<ToolTypeEntity> dbToolType = getToolTypeById(id);
        ToolTypeEntity dbToolTypeEnt = dbToolType.get();

        dbToolTypeEnt.setAvailableStock(dbToolTypeEnt.getAvailableStock() + quantity);
        return toolTypeRepository.save(dbToolTypeEnt);
    }

    public ToolTypeEntity changeTotalStock(Long id, Integer quantity){
        Optional<ToolTypeEntity> dbToolType = getToolTypeById(id);
        ToolTypeEntity dbToolTypeEnt = dbToolType.get();

        dbToolTypeEnt.setTotalStock(dbToolTypeEnt.getTotalStock() + quantity);
        return toolTypeRepository.save(dbToolTypeEnt);
    }

    public ToolTypeEntity increaseBothStocks(Long id, Integer quantity){
        Optional<ToolTypeEntity> dbToolType = getToolTypeById(id);
        ToolTypeEntity dbToolTypeEnt = dbToolType.get();

        dbToolTypeEnt.setTotalStock(dbToolTypeEnt.getTotalStock() + quantity);
        dbToolTypeEnt.setAvailableStock(dbToolTypeEnt.getAvailableStock() + quantity);

        return toolTypeRepository.save(dbToolTypeEnt);
    }

    public boolean deleteToolTypeById(Long id){
        if(toolTypeRepository.existsById(id)){
            toolTypeRepository.deleteById(id);
            return true;

        } else{
            return false;
        }
    }
}
