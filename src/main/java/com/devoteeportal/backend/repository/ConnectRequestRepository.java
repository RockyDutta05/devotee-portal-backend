package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.ConnectRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConnectRequestRepository extends JpaRepository<ConnectRequest, UUID> {
    List<ConnectRequest> findByTargetId(UUID targetId);
    List<ConnectRequest> findByRequesterId(UUID requesterId);
}
