package com.spring_boots.spring_boots.user.repository;

import com.spring_boots.spring_boots.user.domain.UsersInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UsersInfo,Long> {
    //Users 엔티티에 있는 userId 값을 찾아서 반환
    Optional<UsersInfo> findByUsers_UserId(Long userId);
}
