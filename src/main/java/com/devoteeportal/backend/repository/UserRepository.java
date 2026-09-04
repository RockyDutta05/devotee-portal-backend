package com.devoteeportal.backend.repository;

import com.devoteeportal.backend.entity.ApprovalStatus;
import com.devoteeportal.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByApprovalStatus(ApprovalStatus approvalStatus);

    @Query("SELECT u FROM User u WHERE u.approvalStatus = :approvalStatus AND " +
           "(:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "u.phone LIKE CONCAT('%', :search, '%'))")
    List<User> findPendingSignupsWithFilters(@org.springframework.data.repository.query.Param("approvalStatus") ApprovalStatus status,
                                             @org.springframework.data.repository.query.Param("search") String search,
                                             org.springframework.data.domain.Sort sort);
}
