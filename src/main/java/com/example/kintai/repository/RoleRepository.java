package com.example.kintai.repository;

import com.example.kintai.entity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByCompany_Id(Long companyId);
    
    @EntityGraph(attributePaths = {"permissions"})
    @Query("SELECT r FROM Role r WHERE r.company.id = :companyId")
    List<Role> findByCompany_IdWithPermissions(@Param("companyId") Long companyId);
    
    Optional<Role> findByNameAndCompany_Id(String name, Long companyId);
    List<Role> findByCompany_IdAndIsSystemRole(Long companyId, Boolean isSystemRole);
}
