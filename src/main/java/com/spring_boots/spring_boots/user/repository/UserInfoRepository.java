package com.spring_boots.spring_boots.user.repository;

import com.spring_boots.spring_boots.user.domain.UsersInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UsersInfo,Long> {
    Optional<UsersInfo> findByUsers_UserId(Long userId);
}
