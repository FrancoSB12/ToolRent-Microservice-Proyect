package com.toolrent.inventoryservice.Service;

import com.toolrent.inventoryservice.DTO.ChargeClientFeeRequest;
import com.toolrent.inventoryservice.DTO.CreateKardexRequest;
import com.toolrent.inventoryservice.DTO.ToolItemIdsRequest;
import com.toolrent.inventoryservice.Entity.ToolItemEntity;
import com.toolrent.inventoryservice.Entity.ToolTypeEntity;
import com.toolrent.inventoryservice.Enum.ToolDamageLevel;
import com.toolrent.inventoryservice.Enum.ToolStatus;
import com.toolrent.inventoryservice.Model.Client;
import com.toolrent.inventoryservice.Model.Rent;
import com.toolrent.inventoryservice.Model.RentXToolItem;
import com.toolrent.inventoryservice.Repository.ToolItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
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
            managedToolType = toolTypeService.changeTotalStock(newToolItem.getToolType().getId(), 1);
        } else {
            managedToolType = toolTypeService.increaseBothStocks(newToolItem.getToolType().getId(), 1);
        }

        //it's saved in the updated toolType in the dbToolItem
        newToolItem.setToolType(managedToolType);

        //It's saved the toolItem
        ToolItemEntity savedToolItem = toolItemRepository.save(newToolItem);

        //Create and save the associated kardex
        createKardex(managedToolType.getName(), "REGISTRO", 1);

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

    public List<ToolTypeEntity> getToolTypesByToolItemIds(List<Long> toolItemIds){
        return toolItemRepository.findToolTypesByToolItemIds(toolItemIds);
    }

    public boolean exists(Long id){ return toolItemRepository.existsById(id); }

    public boolean existsBySerialNumber(String serialNumber){ return toolItemRepository.existsBySerialNumber(serialNumber); }

    public ToolItemEntity updateToolItem(Long id, ToolItemEntity tool){
        //The tool is searched in the database
        Optional<ToolItemEntity> databaseToolItem = toolItemRepository.findById(id);
        ToolItemEntity databaseToolItemEntity = databaseToolItem.get();

        //Each attribute updatable is checked to see which one was updated
        if(tool.getStatus() != null){
            if(toolValidationService.isInvalidToolStatus(tool.getStatus().toString())){
                throw new IllegalArgumentException("Estado de la herramienta inválido");
            }
            databaseToolItemEntity.setStatus(tool.getStatus());
        }

        if(tool.getDamageLevel() != null){
            if(toolValidationService.isInvalidDamageLevel(tool.getDamageLevel().toString())){
                throw new IllegalArgumentException("Nivel de daño de la herramienta inválido");
            }
            databaseToolItemEntity.setDamageLevel(tool.getDamageLevel());
        }

        return toolItemRepository.save(databaseToolItemEntity);
    }

    public ToolItemEntity enableToolItem(Long id){
        //"Irreparable" and "Dada de Baja" case are verified in the controller

        //The tool item is searched in the database ande changes its status and damage level
        Optional<ToolItemEntity> dbToolItem = getToolItemById(id);
        ToolItemEntity dbToolItemEnt = dbToolItem.get();

        //It's ensured that the tool has the corresponding statuses
        dbToolItemEnt.setStatus(ToolStatus.DISPONIBLE);
        dbToolItemEnt.setDamageLevel(ToolDamageLevel.NO_DANADA);

        //The tool type is searched in the database and increase the available stock
        ToolTypeEntity updatedToolType = toolTypeService.changeAvailableStock(dbToolItemEnt.getToolType().getId(), 1);

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
            ToolTypeEntity updatedToolType = toolTypeService.changeAvailableStock(dbToolItemEnt.getToolType().getId(), -1);

            //it's saved in the updated toolType in the dbToolItem
            dbToolItemEnt.setToolType(updatedToolType);

            //Creates the associated kardex
            createKardex(updatedToolType.getName(), "BAJA", -1);

        } else {
            //Any other type of damage sends the tool to "En reparación"
            //It's ensured that the tool has the corresponding statuses
            dbToolItemEnt.setStatus(ToolStatus.EN_REPARACION);
            dbToolItemEnt.setDamageLevel(toolItem.getDamageLevel());

            //The tool type is searched in the database and decrease the available stock
            ToolTypeEntity updatedToolType = toolTypeService.changeAvailableStock(dbToolItemEnt.getToolType().getId(), -1);

            //it's saved in the updated toolType in the dbToolItem
            dbToolItemEnt.setToolType(updatedToolType);

            //Create and save the associated kardex
            createKardex(updatedToolType.getName(), "REPARACION", -1);
        }
        return toolItemRepository.save(dbToolItemEnt);
    }

    //This method is used when a tool is returned damaged
    @Transactional
    public ToolItemEntity evaluateDamage(Long toolId, ToolItemEntity toolItemFromFront){
        //The tool is searched in the database
        Optional<ToolItemEntity> dbToolItem = getToolItemById(toolId);
        ToolItemEntity dbToolItemEnt = dbToolItem.get();

        List<RentXToolItem> toolItemHistory = getToolItemHistoryInDB(toolId);

        if(toolItemHistory.isEmpty()){
            throw new RuntimeException("Esta herramienta nunca ha sido prestada, no se puede cobrar a nadie");
        }

        //The first one in the list is the most recent rent that the tool has
        Rent lastRent = fetchRentInDB(toolItemHistory.get(0).getRentId());
        Client clientToCharge = fetchClientInDB(lastRent.getClientRun());

        if(toolItemFromFront.getDamageLevel() == ToolDamageLevel.IRREPARABLE){
            processIrreparableTool(dbToolItemEnt, toolItemFromFront.getDamageLevel());

        } else if(toolItemFromFront.getDamageLevel() == ToolDamageLevel.NO_DANADA){
            //This is the case where it was false that the tool was damaged
            dbToolItemEnt.setStatus(ToolStatus.DISPONIBLE);
            dbToolItemEnt.setDamageLevel(ToolDamageLevel.NO_DANADA);

            toolTypeService.changeAvailableStock(dbToolItemEnt.getToolType().getId(), 1);

            if(clientToCharge.getDebt() == 0){
                try {
                    clientToCharge.setStatus("Activo");
                    restTemplate.put("http://client-service/client/" + clientToCharge.getRun(), clientToCharge, Client.class);
                } catch (RestClientException e) {
                    throw new RuntimeException("Error actualizando datos del cliente.");
                }
            }
            return toolItemRepository.save(dbToolItemEnt);

        } else{
            //It's ensured that the tool has the corresponding status and damage level
            dbToolItemEnt.setStatus(ToolStatus.EN_REPARACION);
            dbToolItemEnt.setDamageLevel(toolItemFromFront.getDamageLevel());
        }

        //Replacement value or repair charge is applied to the client and creates the associated kardex
        if(toolItemFromFront.getDamageLevel() == ToolDamageLevel.IRREPARABLE){
            ChargeClientFeeRequest clientFeeRequest = new ChargeClientFeeRequest(clientToCharge, dbToolItemEnt.getToolType().getReplacementValue());
            restTemplate.put("http://fee-service/fee/charge-client", clientFeeRequest, Void.class);
            createKardex(dbToolItemEnt.getToolType().getName(), "BAJA", -1);

        } else if(toolItemFromFront.getDamageLevel() != ToolDamageLevel.NO_DANADA){
            ChargeClientFeeRequest clientFeeRequest = new ChargeClientFeeRequest(clientToCharge, dbToolItemEnt.getToolType().getDamageFee());
            restTemplate.put("http://fee-service/fee/charge-client", clientFeeRequest, Void.class);
        }

        //The available stock isn't reduced since is already reduced by the rent
        return toolItemRepository.save(dbToolItemEnt);
    }

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
        ToolTypeEntity updatedToolType = toolTypeService.changeTotalStock(dbToolItem.getToolType().getId(), -1);

        //it's saved in the updated toolType in the dbToolItem
        dbToolItem.setToolType(updatedToolType);
    }

    private List<RentXToolItem> getToolItemHistoryInDB(Long toolItemId) {
        List<RentXToolItem> rentXToolItem;
        try {
            rentXToolItem = restTemplate.getForObject("http://rent-service/rent-x-tool-item/tool-history/" + toolItemId, List.class);
        } catch (HttpClientErrorException.NotFound e) {
            //The client microservice responded, but said that the client doesn't exist
            throw new RuntimeException("Herramienta no encontrada con ID: " + toolItemId);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            //The client microservice responded, but with another error (400, 401, 500, etc.)
            throw new RuntimeException("Error en servicio de clientes: " + e.getResponseBodyAsString());

        } catch (RestClientException e) {
            //The client microservice didn't respond (Off, Timeout, DNS, etc.)
            throw new RuntimeException("El servicio de clientes está caído o no responde.");
        }

        return rentXToolItem;
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

    private Rent fetchRentInDB(Long rentId) {
        Rent rent;
        try {
            rent = restTemplate.getForObject("http://rent-service/rent/" + rentId, Rent.class);
        } catch (HttpClientErrorException.NotFound e) {
            //The client microservice responded, but said that the client doesn't exist
            throw new RuntimeException("Préstamo no encontrado con ID: " + rentId);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            //The client microservice responded, but with another error (400, 401, 500, etc.)
            throw new RuntimeException("Error en servicio de clientes: " + e.getResponseBodyAsString());

        } catch (RestClientException e) {
            //The client microservice didn't respond (Off, Timeout, DNS, etc.)
            throw new RuntimeException("El servicio de clientes está caído o no responde.");
        }

        return rent;
    }

    private void createKardex(String toolTypeName, String operationType, Integer stock) {
        try {
            CreateKardexRequest createKardexRequest = new CreateKardexRequest(toolTypeName, operationType, stock);
            restTemplate.postForObject("http://kardex-service/kardex/entry", createKardexRequest, Void.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error creando kardex. Datos inconsistentes.");
        }
    }
}
