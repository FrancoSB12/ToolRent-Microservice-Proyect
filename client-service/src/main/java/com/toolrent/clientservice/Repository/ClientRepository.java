package com.toolrent.clientservice.Repository;

import com.toolrent.clientservice.Entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, String>  {
    List<ClientEntity> findByStatus(String status);
}
