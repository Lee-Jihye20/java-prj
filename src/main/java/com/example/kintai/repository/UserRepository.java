package com.example.kintai.repository;

import com.example.kintai.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndCompanyId(String username, Long companyId);

    Optional<User> findByUsernameAndCompanyCompanyCode(String username, String companyCode);

    List<User> findAllByCompanyId(Long companyId);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);
}
