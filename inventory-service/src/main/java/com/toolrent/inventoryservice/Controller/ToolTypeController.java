package com.toolrent.inventoryservice.Controller;

import com.toolrent.inventoryservice.DTO.ChangeStockRequest;
import com.toolrent.inventoryservice.Entity.ToolTypeEntity;
import com.toolrent.inventoryservice.Service.ToolTypeService;
import com.toolrent.inventoryservice.Service.ToolValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory/tool-type")
@CrossOrigin("*")
public class ToolTypeController {
    private final ToolTypeService toolTypeService;
    private final ToolValidationService toolValidationService;

    @Autowired
    public ToolTypeController(ToolTypeService toolTypeService, ToolValidationService toolValidationService) {
        this.toolTypeService = toolTypeService;
        this.toolValidationService = toolValidationService;
    }

    //Create tool type
    @PostMapping
    public ResponseEntity<?> createToolType(@RequestBody ToolTypeEntity toolType){
        //First, it's verified that the tool type doesn't exist
        if (toolType.getId() != null && toolTypeService.exists(toolType.getId())) {
            return new ResponseEntity<>("El tipo de herramienta ya existe en la base de datos", HttpStatus.CONFLICT);
        }

        //Then, the data is validated for accuracy
        if (toolValidationService.isInvalidName(toolType.getName())) {
            return new ResponseEntity<>("Nombre de la herramienta inválido", HttpStatus.BAD_REQUEST);
        }

        if (toolValidationService.isInvalidName(toolType.getCategory())) {
            return new ResponseEntity<>("Nombre de la categoría de la herramienta inválida", HttpStatus.BAD_REQUEST);
        }

        if (toolValidationService.isInvalidToolName(toolType.getModel())) {
            return new ResponseEntity<>("Modelo de la herramienta inválido", HttpStatus.BAD_REQUEST);
        }

        if (toolValidationService.isInvalidStockOrFee(toolType.getReplacementValue())) {
            return new ResponseEntity<>("Valor de reposición de la herramienta inválido", HttpStatus.BAD_REQUEST);
        }

        if (toolValidationService.isInvalidStockOrFee(toolType.getRentalFee())) {
            return new ResponseEntity<>("Tarifa de arriendo de la herramienta inválida", HttpStatus.BAD_REQUEST);
        }

        if (toolValidationService.isInvalidStockOrFee(toolType.getDamageFee())) {
            return new ResponseEntity<>("Tarifa de daño de la herramienta inválida", HttpStatus.BAD_REQUEST);
        }

        ToolTypeEntity newToolType = toolTypeService.createToolType(toolType);
        return new ResponseEntity<>(newToolType, HttpStatus.CREATED);
    }

    //Get tool type
    @GetMapping
    public ResponseEntity<List<ToolTypeEntity>> getAllToolTypes(){
        List<ToolTypeEntity> toolTypes = toolTypeService.getAllToolTypes();
        return new ResponseEntity<>(toolTypes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ToolTypeEntity> getToolTypeById(@PathVariable Long id){
        return toolTypeService.getToolTypeById(id)
                .map(toolType -> new ResponseEntity<>(toolType, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ToolTypeEntity> getToolTypeByName(@PathVariable String name) {
        return toolTypeService.getToolTypeByName(name)
                .map(toolType -> new ResponseEntity<>(toolType, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    //Update tool
    @PutMapping("/{id}")
    public ResponseEntity<?> updateToolType(@PathVariable Long id, @RequestBody ToolTypeEntity toolType){
        try {
            //Verify that the tool has an id and the id isn't null
            if (toolType.getId() == null) {
                return new ResponseEntity<>("El id no puede estar vacío o ser nulo", HttpStatus.BAD_REQUEST);
            }

            //Verify that the tool type exist in the database
            if (!toolTypeService.exists(id)) {
                return new ResponseEntity<>("El tipo de herramienta no existe en la base de datos", HttpStatus.NOT_FOUND);
            }

            ToolTypeEntity updatedToolType = toolTypeService.updateToolType(id, toolType);
            return new ResponseEntity<>(updatedToolType, HttpStatus.OK);

        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/change-available-stock")
    public ResponseEntity<?> changeAvailableStock(@RequestBody ChangeStockRequest request){
        Long id = request.getId();
        Integer quantity = request.getQuantity();

        //Verify that the tool type exist in the database
        if (!toolTypeService.exists(id)) {
            return new ResponseEntity<>("El tipo de herramienta no existe en la base de datos", HttpStatus.NOT_FOUND);
        }

        ToolTypeEntity updatedToolType = toolTypeService.changeAvailableStock(id, quantity);
        return new ResponseEntity<>(updatedToolType, HttpStatus.OK);
    }

    @PutMapping("/change-total-stock")
    public ResponseEntity<?> changeTotalStock(@RequestBody ChangeStockRequest request){
        //Verify that the tool type exist in the database
        Long id = request.getId();
        if (!toolTypeService.exists(id)) {
            return new ResponseEntity<>("El tipo de herramienta no existe en la base de datos", HttpStatus.NOT_FOUND);
        }

        Integer quantity = request.getQuantity();
        ToolTypeEntity updatedToolType = toolTypeService.changeTotalStock(id, quantity);
        return new ResponseEntity<>(updatedToolType, HttpStatus.OK);
    }

    @PutMapping("/increase-both-stock")
    public ResponseEntity<?> increaseBothStock(@RequestBody ChangeStockRequest request){
        //Verify that the tool type exist in the database
        Long id = request.getId();
        if (!toolTypeService.exists(id)) {
            return new ResponseEntity<>("El tipo de herramienta no existe en la base de datos", HttpStatus.NOT_FOUND);
        }

        Integer quantity = request.getQuantity();
        if(toolValidationService.isInvalidStockOrFee(quantity)) {
            return new ResponseEntity<>("Cantidad de incremento de stock inválido", HttpStatus.BAD_REQUEST);
        }

        ToolTypeEntity updatedToolType = toolTypeService.changeTotalStock(id, quantity);
        return new ResponseEntity<>(updatedToolType, HttpStatus.OK);
    }

    //Delete tool type
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteToolTypeById(@PathVariable Long id){
        boolean deletedToolType = toolTypeService.deleteToolTypeById(id);
        return deletedToolType ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
