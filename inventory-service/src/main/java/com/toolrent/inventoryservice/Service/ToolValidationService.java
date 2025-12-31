package com.toolrent.inventoryservice.Service;

import com.toolrent.inventoryservice.Enum.ToolDamageLevel;
import com.toolrent.inventoryservice.Enum.ToolStatus;
import org.springframework.stereotype.Service;

@Service
public class ToolValidationService {
    //Regex precompiled for efficiency
    private static final String NAME_REGEX = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
    private static final String TOOL_NAME_REGEX = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ \\d*/\"'.#, -]+$";
    private static final String SERIAL_REGEX = "^[a-zA-Z0-9.-]{5,30}$";

    //Verify that the user input doesn't contain SQL injections
    public boolean hasMaliciousQuery(String input){
        String lowerCase = input.toLowerCase();
        return lowerCase.contains("drop") || lowerCase.contains("delete") || lowerCase.contains("insert") || lowerCase.contains("update")
                || lowerCase.contains("select") || lowerCase.contains("truncate") || lowerCase.contains("--") ||  lowerCase.contains(";");
    }

    public boolean isInvalidSerialNumber(String serialNumber){
        return serialNumber == null || !serialNumber.matches(SERIAL_REGEX) || hasMaliciousQuery(serialNumber);
    }

    public boolean isInvalidName(String name){
        //Name and Malicious Query validation
        return name == null || !name.matches(NAME_REGEX) || hasMaliciousQuery(name);
    }

    public boolean isInvalidToolName(String toolName){
        //Name of the tool and Malicious Query validation
        return toolName == null || !toolName.matches(TOOL_NAME_REGEX) || hasMaliciousQuery(toolName);
    }

    public boolean isInvalidStockOrFee(Integer number){
        return number == null || number < 0;
    }

    public boolean isInvalidToolStatus(String status){
        if(status == null || status.isEmpty() || hasMaliciousQuery(status)) return true;

        //Verify that the tool status is correct
        String formattedStatus = status.toUpperCase().replace(" ", "_");
        for(ToolStatus toolStatus : ToolStatus.values()){
            if(toolStatus.name().equals(formattedStatus)){
                return false;
            }
        }

        //If the for loop ends without finding an equality, it's invalid
        return true;
    }

    public boolean isInvalidDamageLevel(String damageLevel){
        if(damageLevel == null || damageLevel.isEmpty() || hasMaliciousQuery(damageLevel)) return true;

        //Verify that the tool damage is correct
        String formattedDamageLevel = damageLevel.toUpperCase().replace(" ", "_");
        for(ToolDamageLevel toolDamageLevel : ToolDamageLevel.values()){
            if(toolDamageLevel.name().equals(formattedDamageLevel)){
                return false;
            }
        }

        //If the for loop ends without finding an equality, it's invalid
        return true;
    }
}
