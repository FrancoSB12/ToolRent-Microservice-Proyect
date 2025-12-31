package com.toolrent.feeservice.Service;

import com.toolrent.feeservice.DTO.ApplyLateFeeRequest;
import com.toolrent.feeservice.DTO.UpdateLateReturnFeeRequest;
import com.toolrent.feeservice.Entity.LateReturnFeeEntity;
import com.toolrent.feeservice.Model.Client;
import com.toolrent.feeservice.Repository.LateReturnFeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class FeeService {
    private final LateReturnFeeRepository lateReturnFeeRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public FeeService(LateReturnFeeRepository lateReturnFeeRepository, RestTemplate restTemplate) {
        this.lateReturnFeeRepository = lateReturnFeeRepository;
        this.restTemplate = restTemplate;
    }

    private static final Integer DEFAULT_FEE = 5000;    //Default value in case the db is empty

    public Integer getLateReturnFee() {
        return lateReturnFeeRepository.findById(1L)
                .map(LateReturnFeeEntity::getCurrentLateReturnFee)
                .orElse(DEFAULT_FEE);
    }

    @Transactional
    public void updateGlobalLateReturnFee(UpdateLateReturnFeeRequest newFee) {
        LateReturnFeeEntity config = lateReturnFeeRepository.findById(1L)
                .orElse(new LateReturnFeeEntity());

        config.setId(1L); //Ensure that it is the ID of the fee
        config.setCurrentLateReturnFee(newFee.getNewLateReturnFee());
        lateReturnFeeRepository.save(config);
    }

    @Transactional
    public void applyLateReturnFee(ApplyLateFeeRequest lateFeeRequest) {
        Integer lateReturnFee = lateFeeRequest.getLateReturnFee();
        Client client = lateFeeRequest.getClient();

        int daysBetween = (int) ChronoUnit.DAYS.between(lateFeeRequest.getReturnDate(), LocalDate.now());
        Integer lateReturnFeeDays = daysBetween * lateReturnFee;

        try {
            client.setDebt(client.getDebt() + lateReturnFeeDays);
            restTemplate.put("http://client-service/client/" + client.getRun(), client, Client.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error actualizando datos del cliente.");
        }
    }

    @Transactional
    public void chargeClientFee(Client client, Integer fee) {
        client.setDebt(client.getDebt() + fee);
        client.setStatus("Restringido");

        restTemplate.put("http://client-service/client/" + client.getRun(), client, Client.class);
    }

}
