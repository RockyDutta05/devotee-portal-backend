package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.ContactInfoRequest;
import com.devoteeportal.backend.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactInfoRequestRepository extends JpaRepository<ContactInfoRequest, UUID> {
    List<ContactInfoRequest> findByTargetId(UUID targetId);
    List<ContactInfoRequest> findByRequesterId(UUID requesterId);
    long countByTargetIdAndStatus(UUID targetId, RequestStatus status);
}
