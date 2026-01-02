package com.toolrent.rentservice.Service;

import com.toolrent.rentservice.Entity.RentXToolItemEntity;
import com.toolrent.rentservice.Repository.RentXToolItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class RentXToolItemService {
    RentXToolItemRepository rentXToolItemRepository;

    @Autowired
    public RentXToolItemService(RentXToolItemRepository rentXToolItemRepository) {
        this.rentXToolItemRepository = rentXToolItemRepository;
    }

    //List all loan tools
    public List<RentXToolItemEntity> getAllRentToolItemsByRent_Id(Long rentId){
        return rentXToolItemRepository.findByRent_Id(rentId);
    }

    public boolean existsByLoanId(Long loanId){
        return rentXToolItemRepository.existsByRent_Id(loanId);
    }

    public Set<Long> getActiveLoansToolTypeIdsByClient(String clientId){
        return rentXToolItemRepository.findActiveRentsToolTypeIdsByClient(clientId);
    }

    public List<RentXToolItemEntity> getHistoryByToolId(Long toolId){
        return rentXToolItemRepository.findHistoryByToolId(toolId);
    }

    public List<Object[]> getMostLoanedToolsBetweenDates(LocalDate start, LocalDate end){
        return rentXToolItemRepository.findMostLoanedToolsBetweenDates(start, end);
    }

    public RentXToolItemEntity save(RentXToolItemEntity rentXToolItemEntity){
        return rentXToolItemRepository.save(rentXToolItemEntity);
    }

}
