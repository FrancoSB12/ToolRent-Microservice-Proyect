package com.toolrent.kardexservice.Service;

import com.toolrent.kardexservice.DTO.CreateKardexRequest;
import com.toolrent.kardexservice.Entity.KardexEntity;
import com.toolrent.kardexservice.Enum.KardexOperationType;
import com.toolrent.kardexservice.Repository.KardexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class KardexService {
    KardexRepository kardexRepository;

    @Autowired
    public KardexService(KardexRepository kardexRepository) {
        this.kardexRepository = kardexRepository;
    }

    public List<KardexEntity> getAllKardex(){
        return kardexRepository.findAll();
    }

    //Aqui iria el get by tooltype name o id

    public List<KardexEntity> getKardexByDateRange(LocalDate startDate, LocalDate endDate){
        return kardexRepository.findByDateBetween(startDate, endDate);
    }

    public void createRegisterToolTypeKardex(Long idToolType){
        KardexEntity kardex = new KardexEntity();
        kardex.setOperationType(KardexOperationType.REGISTRO);
        kardex.setDate(LocalDate.now());
        kardex.setStockInvolved(0); //The stock is 0 because it's creating the type of tool
        kardex.setToolTypeId(idToolType);
        kardexRepository.save(kardex);
    }

    public void createKardexEntry(CreateKardexRequest request){
        KardexEntity kardex = new KardexEntity();
        kardex.setOperationType(request.getOperationType());
        kardex.setDate(LocalDate.now());
        kardex.setStockInvolved(1);     //For every other movement, the stock involved is always 1
        kardex.setToolTypeId(request.getToolTypeId());
        kardexRepository.save(kardex);
    }
}

