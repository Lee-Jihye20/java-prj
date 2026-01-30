package com.example.kintai.repository;

import com.example.kintai.entity.FixRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixRequestRepository extends JpaRepository<FixRequest, Long> {

    List<FixRequest> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<FixRequest> findAllByUser_CompanyId(Long companyId);

    List<FixRequest> findByStatusAndUser_CompanyId(String status, Long companyId);
}
