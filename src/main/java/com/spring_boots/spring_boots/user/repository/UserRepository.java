package com.spring_boots.spring_boots.user.repository;

import com.spring_boots.spring_boots.user.domain.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users,Long> {
    @EntityGraph(attributePaths = {"usersInfoList", "ordersList"})
    Optional<Users> findById(Long id);
    Optional<Users> findByEmail(String email);
    boolean existsByUserRealId(String userRealId);
    @EntityGraph(attributePaths = {"usersInfoList"})
    Optional<Users> findByUserRealId(String userRealId);
}
