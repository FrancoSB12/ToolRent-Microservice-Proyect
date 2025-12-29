package com.toolrent.employeeservice.Service;

import org.springframework.stereotype.Service;

@Service
public class EmployeeValidationService {
    private static final String NAME_REGEX = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String CLEAN_PHONE_REGEX = "[\\s\\-()]";
    private static final String CELLPHONE_REGEX = "\\+569\\d{8}";

    //Verify that the user input doesn't contain SQL injections
    public boolean hasMaliciousQuery(String input){
        String lowerCase = input.toLowerCase();
        return lowerCase.contains("drop") || lowerCase.contains("delete") || lowerCase.contains("insert") || lowerCase.contains("update")
                || lowerCase.contains("select") || lowerCase.contains("truncate") || lowerCase.contains("--") ||  lowerCase.contains(";");
    }

    //Verify that the run is valid
    public boolean isInvalidRun(String run){
        //Null and Malicious Query validation
        if(run == null || run.isEmpty() || hasMaliciousQuery(run)) return true;

        //Normalize the run
        run = run.replace(".","").replace("-","").toUpperCase();

        return run.length() < 2;
    }

    public boolean isInvalidName(String name){
        //Name and Malicious Query validation
        return name == null || !name.matches(NAME_REGEX) || hasMaliciousQuery(name);
    }

    public boolean isInvalidEmail(String email){
        //Email and Malicious Query validation
        return email == null || !email.matches(EMAIL_REGEX) || hasMaliciousQuery(email);
    }

    public boolean isInvalidCellphone(String cellphone){
        //Null and Malicious Query validation
        if(cellphone == null || cellphone.isEmpty() || hasMaliciousQuery(cellphone)) return true;

        String reformatedCellphone = reformatCellphone(cellphone);
        return !reformatedCellphone.matches(CELLPHONE_REGEX) || reformatedCellphone.length() != 12;
    }

    //Private method
    public String reformatCellphone(String cellphone){
        String clean = cellphone.replaceAll(CLEAN_PHONE_REGEX, "");

        if(clean.startsWith("+56")) return clean;
        else if(clean.startsWith("0")) return "+56" + clean.substring(1);
        else if(clean.length() == 9 && clean.startsWith("9")) return "+56"  + clean;

        return clean;
    }
}
