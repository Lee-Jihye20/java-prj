package com.example.kintai.repository;

import com.example.kintai.entity.CompanySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {
    Optional<CompanySettings> findByCompanyId(Long companyId);
}