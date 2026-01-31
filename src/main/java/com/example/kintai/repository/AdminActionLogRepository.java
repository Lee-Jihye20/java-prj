package com.example.kintai.repository;

import com.example.kintai.entity.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

    List<AdminActionLog> findByAdmin_IdOrderByCreatedAtDesc(Long adminId, org.springframework.data.domain.Pageable pageable);

    List<AdminActionLog> findTop100ByAdmin_IdOrderByCreatedAtDesc(Long adminId);
    
    List<AdminActionLog> findByAdmin_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long adminId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
