package com.toolrent.kardexservice.Service;

import com.toolrent.kardexservice.Enum.KardexOperationType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class KardexValidationService {
    //Verify that the user input doesn't contain SQL injections
    public boolean hasMaliciousQuery(String input){
        String lowerCase = input.toLowerCase();
        return lowerCase.contains("drop") || lowerCase.contains("delete") || lowerCase.contains("insert") || lowerCase.contains("update")
                || lowerCase.contains("select") || lowerCase.contains("truncate") || lowerCase.contains("--") ||  lowerCase.contains(";");
    }

    public boolean isInvalidOperationType(String operationType){
        if(operationType == null || operationType.isEmpty() || hasMaliciousQuery(operationType)) return true;

        //Verify that the kardex operation type is correct
        String formattedOperationType = operationType.toUpperCase().replace(" ", "_");
        for(KardexOperationType kardexOperationType : KardexOperationType.values()){
            if(!kardexOperationType.name().equals(formattedOperationType)){
                return true;
            }
        }

        return false;
    }

    public boolean isInvalidDate(LocalDate date){
        return date == null || date.isBefore(LocalDate.now());
    }

    public boolean isInvalidStock(Integer number){
        return number == null || number < 0;
    }
}
