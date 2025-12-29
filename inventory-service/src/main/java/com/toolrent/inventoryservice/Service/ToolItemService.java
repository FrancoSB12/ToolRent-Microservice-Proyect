package com.toolrent.inventoryservice.Service;

import com.toolrent.inventoryservice.DTO.CreateKardexRequest;
import com.toolrent.inventoryservice.Entity.ToolItemEntity;
import com.toolrent.inventoryservice.Entity.ToolTypeEntity;
import com.toolrent.inventoryservice.Enum.ToolDamageLevel;
import com.toolrent.inventoryservice.Enum.ToolStatus;
import com.toolrent.inventoryservice.Repository.ToolItemRepository;
import jakarta.persistence.Access;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class ToolItemService {
    private final RestTemplate restTemplate;
    ToolItemRepository toolItemRepository;
    ToolTypeService toolTypeService;
    ToolValidationService toolValidationService;

    @Autowired
    public ToolItemService(ToolItemRepository toolItemRepository, ToolTypeService toolTypeService, ToolValidationService toolValidationService, RestTemplate restTemplate) {
        this.toolItemRepository = toolItemRepository;
        this.toolTypeService = toolTypeService;
        this.toolValidationService = toolValidationService;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public ToolItemEntity createToolItem(ToolItemEntity newToolItem){
        //The tool type is searched in the database and increase the available and total stock
        ToolTypeEntity managedToolType;
        if(newToolItem.getStatus() != ToolStatus.DISPONIBLE || newToolItem.getDamageLevel() != ToolDamageLevel.NO_DANADA){
            managedToolType = toolTypeService.increaseTotalStock(newToolItem.getToolType(), 1);
        } else {
            managedToolType = toolTypeService.increaseBothStocks(newToolItem.getToolType(), 1);
        }

        //it's saved in the updated toolType in the dbToolItem
        newToolItem.setToolType(managedToolType);

        //It's saved the toolItem
        ToolItemEntity savedToolItem = toolItemRepository.save(newToolItem);

        //Create and save the associated kardex
        CreateKardexRequest createKardexRequest = new CreateKardexRequest(managedToolType.getId(), "REGISTRO", 1);
        String url = "http://kardex-service/kardex/entry";
        restTemplate.postForObject(url, createKardexRequest, Void.class);

        return savedToolItem;
    }

    public List<ToolItemEntity> getAllToolItems(){
        //Sorted by tool type name so that they appear together on the frontend
        return toolItemRepository.findAll(Sort.by(Sort.Direction.ASC, "toolType.name"));
    }

    public Optional<ToolItemEntity> getToolItemById(Long id){
        return toolItemRepository.findById(id);
    }

    public Optional<ToolItemEntity> getToolItemBySerialNumber(String serialNumber){ return toolItemRepository.findBySerialNumber(serialNumber); }

    public ToolItemEntity getFirstAvailableByType(Long typeId) {
        List<ToolDamageLevel> acceptableLevel = List.of(ToolDamageLevel.NO_DANADA);

        return toolItemRepository.findFirstByToolType_IdAndStatusAndDamageLevelIn(
                typeId,
                ToolStatus.DISPONIBLE,
                acceptableLevel
        ).orElseThrow(() -> new RuntimeException("No hay unidades disponibles de este tipo de herramienta."));
    }

    public boolean exists(Long id){ return toolItemRepository.existsById(id); }

    public boolean existsBySerialNumber(String serialNumber){ return toolItemRepository.existsBySerialNumber(serialNumber); }

    public ToolItemEntity enableToolItem(Long id){
        //"Irreparable" and "Dada de Baja" case are verified in the controller

        //The tool item is searched in the database ande changes its status and damage level
        Optional<ToolItemEntity> dbToolItem = getToolItemById(id);
        ToolItemEntity dbToolItemEnt = dbToolItem.get();

        //It's ensured that the tool has the corresponding statuses
        dbToolItemEnt.setStatus(ToolStatus.DISPONIBLE);
        dbToolItemEnt.setDamageLevel(ToolDamageLevel.NO_DANADA);

        //The tool type is searched in the database and increase the available stock
        ToolTypeEntity updatedToolType = toolTypeService.increaseAvailableStock(dbToolItemEnt.getToolType(), 1);

        //it's saved in the updated toolType in the dbToolItemEntity
        dbToolItemEnt.setToolType(updatedToolType);

        return toolItemRepository.save(dbToolItemEnt);
    }

    //This method is used for internal maintenance
    @Transactional
    public ToolItemEntity disableToolItem(Long id, ToolItemEntity toolItem){
        //The tool item is searched in the database
        Optional<ToolItemEntity> dbToolItem = getToolItemById(id);
        ToolItemEntity dbToolItemEnt = dbToolItem.get();

        if(toolItem.getDamageLevel() == ToolDamageLevel.IRREPARABLE || toolItem.getDamageLevel() == ToolDamageLevel.DESUSO){
            //The only way to unintentionally disable an undamaged tool is if the admin does so by mistake, which is a human error that cannot be corrected in the code

            //Since in both cases the output is the same, the same method is used for "Irreparable" or is if "Dada de baja" due to disuse
            processIrreparableTool(dbToolItemEnt, toolItem.getDamageLevel());

            //The tool type is searched in the database and decrease the available stock
            ToolTypeEntity updatedToolType = toolTypeService.decreaseAvailableStock(dbToolItemEnt.getToolType(), 1);

            //it's saved in the updated toolType in the dbToolItem
            dbToolItemEnt.setToolType(updatedToolType);

            //Creates the associated kardex
            CreateKardexRequest createKardexRequest = new CreateKardexRequest(updatedToolType.getId(), "BAJA", 1);
            String url = "http://kardex-service/kardex/entry";
            restTemplate.postForObject(url, createKardexRequest, Void.class);

        } else {
            //Any other type of damage sends the tool to "En reparación"
            //It's ensured that the tool has the corresponding statuses
            dbToolItemEnt.setStatus(ToolStatus.EN_REPARACION);
            dbToolItemEnt.setDamageLevel(toolItem.getDamageLevel());

            //The tool type is searched in the database and decrease the available stock
            ToolTypeEntity updatedToolType = toolTypeService.decreaseAvailableStock(dbToolItemEnt.getToolType(), 1);

            //it's saved in the updated toolType in the dbToolItem
            dbToolItemEnt.setToolType(updatedToolType);

            //Create and save the associated kardex
            CreateKardexRequest createKardexRequest = new CreateKardexRequest(updatedToolType.getId(), "REPARACION", 1);
            String url = "http://kardex-service/kardex/entry";
            restTemplate.postForObject(url, createKardexRequest, Void.class);
        }
        return toolItemRepository.save(dbToolItemEnt);
    }

    //This method is used when a tool is returned damaged


    public boolean deleteToolItemById(Long id){
        if(toolItemRepository.existsById(id)){
            toolItemRepository.deleteById(id);
            return true;

        } else{
            return false;
        }
    }

    //Private method
    private void processIrreparableTool(ToolItemEntity dbToolItem, ToolDamageLevel toolDamageLevel) {
        //It's ensured that the tool has the corresponding statuses
        dbToolItem.setStatus(ToolStatus.DADA_DE_BAJA);
        if(toolDamageLevel == ToolDamageLevel.IRREPARABLE) {
            //If the tool is "Irreparable" and not only unused
            dbToolItem.setDamageLevel(ToolDamageLevel.IRREPARABLE);
        } else {
            //If the tool is only unused
            dbToolItem.setDamageLevel(ToolDamageLevel.DESUSO);
        }

        //The tool type is searched in the database and decrease the total stock, since an irreparable or unused tool can't be "Disponible" again
        ToolTypeEntity updatedToolType = toolTypeService.decreaseTotalStock(dbToolItem.getToolType(), 1);

        //it's saved in the updated toolType in the dbToolItem
        dbToolItem.setToolType(updatedToolType);
    }

}
