package com.toolrent.feeservice.Repository;

import com.toolrent.feeservice.Entity.LateReturnFeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LateReturnFeeRepository extends JpaRepository<LateReturnFeeEntity, Long> {
}
