package com.toolrent.rentservice.Service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class RentValidationService {

    public boolean isInvalidDate(LocalDate date){
        return date == null || date.isBefore(LocalDate.now());
    }

    public boolean isInvalidReturnDate(LocalDate returnDate, LocalDate rentDate){
        return returnDate == null || returnDate.isBefore(rentDate);
    }
}
