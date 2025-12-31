package com.toolrent.inventoryservice.Service;

import com.toolrent.inventoryservice.DTO.CreateKardexRequest;
import com.toolrent.inventoryservice.DTO.UpdateToolTypeRequest;
import com.toolrent.inventoryservice.Entity.ToolTypeEntity;
import com.toolrent.inventoryservice.Repository.ToolTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
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

    @Transactional
    public ToolTypeEntity createToolType(ToolTypeEntity newToolType){
        //Save the toolType
        ToolTypeEntity savedToolType = toolTypeRepository.save(newToolType);

        //Create and save the associated kardex
        CreateKardexRequest createKardexRequest = new CreateKardexRequest(newToolType.getName(), "REGISTRO", 0);
        String url = "http://kardex-service/kardex/entry";
        restTemplate.postForObject(url, createKardexRequest, Void.class);

        return savedToolType;
    }

    public boolean exists(Long id){
        return toolTypeRepository.existsById(id);
    }

    public ToolTypeEntity updateToolType(Long id, UpdateToolTypeRequest toolTypeRequest){
        Optional<ToolTypeEntity> dbToolType = getToolTypeById(id);
        ToolTypeEntity dbToolTypeEnt = dbToolType.get();

        if(toolTypeRequest.getName() != null){
            if(toolValidationService.isInvalidName(toolTypeRequest.getName())){
                throw new IllegalArgumentException("Nombre de la herramienta inválido");
            }
            dbToolTypeEnt.setName(toolTypeRequest.getName());
        }

        if(toolTypeRequest.getCategory() != null){
            if(toolValidationService.isInvalidName(toolTypeRequest.getCategory())){
                throw new IllegalArgumentException("Categoría de la herramienta inválido");
            }
            dbToolTypeEnt.setCategory(toolTypeRequest.getCategory());
        }

        if(toolTypeRequest.getModel() != null){
            if(toolValidationService.isInvalidToolName(toolTypeRequest.getModel())){
                throw new IllegalArgumentException("Modelo de la herramienta inválido");
            }
            dbToolTypeEnt.setModel(toolTypeRequest.getModel());
        }

        if(toolTypeRequest.getTotalStock() != null){
            if(toolValidationService.isInvalidStockOrFee(toolTypeRequest.getTotalStock())){
                throw new IllegalArgumentException("Valor del stock total de la herramienta inválido");
            }
            dbToolTypeEnt.setTotalStock(toolTypeRequest.getTotalStock());
        }

        if(toolTypeRequest.getAvailableStock() != null){
            if(toolValidationService.isInvalidStockOrFee(toolTypeRequest.getAvailableStock())){
                throw new IllegalArgumentException("Valor del stock disponible de la herramienta inválido");
            }
            dbToolTypeEnt.setAvailableStock(toolTypeRequest.getAvailableStock());
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
