package com.toolrent.inventoryservice.Controller;

import com.toolrent.inventoryservice.Entity.ToolItemEntity;
import com.toolrent.inventoryservice.Enum.ToolDamageLevel;
import com.toolrent.inventoryservice.Enum.ToolStatus;
import com.toolrent.inventoryservice.Service.ToolItemService;
import com.toolrent.inventoryservice.Service.ToolTypeService;
import com.toolrent.inventoryservice.Service.ToolValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/inventory/tool-items")
@CrossOrigin("*")
public class ToolItemController {
    private final ToolItemService toolItemService;
    private final ToolTypeService toolTypeService;
    private final ToolValidationService toolValidationService;

    @Autowired
    public ToolItemController(ToolItemService toolItemService, ToolTypeService toolTypeService, ToolValidationService toolValidationService) {
        this.toolItemService = toolItemService;
        this.toolTypeService = toolTypeService;
        this.toolValidationService = toolValidationService;
    }

    //Create tool item
    @PostMapping
    public ResponseEntity<?> createToolItem(@RequestBody ToolItemEntity toolItem) {
        //First, it's verified that the tool item doesn't exist
        if ((toolItem.getId() != null && toolItemService.exists(toolItem.getId())) || toolItemService.existsBySerialNumber(toolItem.getSerialNumber())) {
            return new ResponseEntity<>("La unidad de herramienta ya existe en la base de datos", HttpStatus.CONFLICT);
        }

        //Then, the data is validated for accuracy
        if (toolValidationService.isInvalidSerialNumber(toolItem.getSerialNumber())) {
            return new ResponseEntity<>("Número de serie de la herramienta inválido", HttpStatus.BAD_REQUEST);
        }

        if (toolValidationService.isInvalidToolStatus(toolItem.getStatus().toString())) {
            return new ResponseEntity<>("Estado de la herramienta inválido", HttpStatus.BAD_REQUEST);
        }

        if (toolValidationService.isInvalidDamageLevel(toolItem.getDamageLevel().toString())) {
            return new ResponseEntity<>("Nivel de daño de la herramienta inválido", HttpStatus.BAD_REQUEST);
        }

        if(toolItem.getToolType() == null || toolItem.getToolType().getId() ==  null){
            return new ResponseEntity<>("Cree o ingrese el tipo de herramienta antes de ingresar la herramienta", HttpStatus.BAD_REQUEST);
        }

        if(!toolTypeService.exists(toolItem.getToolType().getId())){
            return new ResponseEntity<>("El tipo de herramienta no se encontró en la base de datos", HttpStatus.NOT_FOUND);
        }

        ToolItemEntity newToolItem = toolItemService.createToolItem(toolItem);
        return new ResponseEntity<>(newToolItem, HttpStatus.CREATED);
    }

    //Get tool item
    @GetMapping
    public ResponseEntity<List<ToolItemEntity>> getAllToolItems() {
        List<ToolItemEntity> toolItems = toolItemService.getAllToolItems();
        return new ResponseEntity<>(toolItems, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ToolItemEntity> getToolItemById(@PathVariable Long id) {
        return toolItemService.getToolItemById(id)
                .map(toolItem -> new ResponseEntity<>(toolItem, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/serial-number/{serialNumber}")
    public ResponseEntity<ToolItemEntity> getToolItemBySerialNumber(@PathVariable String serialNumber){
        return toolItemService.getToolItemBySerialNumber(serialNumber)
                .map(toolItem -> new ResponseEntity<>(toolItem, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/type/{toolTypeId}")
    public ResponseEntity<?> getFirstAvailableToolItem(@PathVariable Long toolTypeId) {
        try {
            ToolItemEntity toolItem = toolItemService.getFirstAvailableByType(toolTypeId);
            return new ResponseEntity<>(toolItem, HttpStatus.OK);
        } catch (RuntimeException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    //Update tool item
    @PutMapping("/enable-tool-item/{serialNumber}")
    public ResponseEntity<?> enableToolItem(@PathVariable String serialNumber) {
        //Verify that the tool exist in the database
        Optional<ToolItemEntity> dbToolItem = toolItemService.getToolItemBySerialNumber(serialNumber);
        if (dbToolItem.isEmpty()) {
            return new ResponseEntity<>("La herramienta no existe en la base de datos", HttpStatus.NOT_FOUND);
        }

        if(dbToolItem.get().getDamageLevel() == ToolDamageLevel.IRREPARABLE){
            return new ResponseEntity<>("La herramienta tiene un daño irreparable, por ende, no puede volver a estar disponible", HttpStatus.BAD_REQUEST);
        }

        if(dbToolItem.get().getStatus() == ToolStatus.PRESTADA){
            return new ResponseEntity<>("La herramienta está en préstamo, no se puede habilitar", HttpStatus.BAD_REQUEST);
        }

        ToolItemEntity enabledTool = toolItemService.enableToolItem(dbToolItem.get().getId());
        return new ResponseEntity<>(enabledTool, HttpStatus.OK);
    }

    @PutMapping("/disable-tool-item/{serialNumber}")
    public ResponseEntity<?> disableToolItem(@PathVariable String serialNumber, @RequestBody ToolItemEntity toolItem) {
        //Verify that the tool item exist in the database
        Optional<ToolItemEntity> dbToolItem = toolItemService.getToolItemBySerialNumber(serialNumber);
        if (dbToolItem.isEmpty()) {
            return new ResponseEntity<>("La herramienta no existe en la base de datos", HttpStatus.NOT_FOUND);
        }

        if(toolItem.getDamageLevel() == ToolDamageLevel.NO_DANADA){
            return new ResponseEntity<>("No se puede deshabilitar una herramienta no dañada", HttpStatus.BAD_REQUEST);
        }

        if(dbToolItem.get().getStatus() == ToolStatus.PRESTADA){
            return new ResponseEntity<>("La herramienta está en préstamo, no se puede deshabilitar", HttpStatus.BAD_REQUEST);
        }

        ToolItemEntity disabledTool = toolItemService.disableToolItem(dbToolItem.get().getId(), toolItem);
        return new ResponseEntity<>(disabledTool, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteToolItemById(@PathVariable Long id){
        boolean deletedToolItem = toolItemService.deleteToolItemById(id);
        return deletedToolItem ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }


}
