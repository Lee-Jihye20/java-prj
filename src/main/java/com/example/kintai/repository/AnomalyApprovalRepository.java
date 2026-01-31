package com.example.kintai.repository;

import com.example.kintai.entity.AnomalyApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnomalyApprovalRepository extends JpaRepository<AnomalyApproval, Long> {

    Optional<AnomalyApproval> findByAttendance_Id(Long attendanceId);

    List<AnomalyApproval> findByAttendance_IdAndAnomalyType(Long attendanceId, String anomalyType);

    Optional<AnomalyApproval> findFirstByAttendance_IdAndAnomalyType(Long attendanceId, String anomalyType);

    boolean existsByAttendance_IdAndAnomalyTypeAndApprovedTrue(Long attendanceId, String anomalyType);

    List<AnomalyApproval> findByApproved(Boolean approved);

    List<AnomalyApproval> findByAttendance_User_CompanyId(Long companyId);
}
