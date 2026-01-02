package com.toolrent.rentservice.Service;

import com.toolrent.rentservice.Entity.RentEntity;
import com.toolrent.rentservice.Entity.RentXToolItemEntity;
import com.toolrent.rentservice.Repository.RentXToolItemRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
       List<RentXToolItemEntity> entities = rentXToolItemRepository.findHistoryByToolId(toolId);
       return entities.stream().map(entity -> {
           RentXToolItemEntity model = new RentXToolItemEntity();

           //Copy data fields
           BeanUtils.copyProperties(entity, model);

           //Manual mapping of Rent
           if (entity.getRent() != null) {
               RentEntity rentModel = new RentEntity();
               //Copy rent data
               BeanUtils.copyProperties(entity.getRent(), rentModel);

               //To avoid infinite loops, it's set the Rent tools list to null
               rentModel.setRentTools(null);

               //It's set to the model
               model.setRent(rentModel);
           }

           return model;
       }).collect(Collectors.toList());
    }

    public List<Object[]> getMostLoanedToolsBetweenDates(LocalDate start, LocalDate end){
        return rentXToolItemRepository.findMostLoanedToolsBetweenDates(start, end);
    }

    public RentXToolItemEntity save(RentXToolItemEntity rentXToolItemEntity){
        return rentXToolItemRepository.save(rentXToolItemEntity);
    }

}
