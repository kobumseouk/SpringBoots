package com.spring_boots.spring_boots.user.domain;

import com.spring_boots.spring_boots.common.BaseTimeEntity;
import com.spring_boots.spring_boots.user.dto.response.UserResponseDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users extends BaseTimeEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "user_real_id", nullable = false, unique = true)
    private String userRealId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_deleted", nullable = true)
    private boolean isDeleted = false;

    @Column(name = "delete_reason")
    private String deleteReason;

    @Column(name = "role", nullable = true)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "provider", nullable = true)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsersInfo> usersInfoList = new ArrayList<>();

    @Override // 권한 반환
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }

    @Override //사용자의 id를 반환(고유 값)
    public String getUsername() {
        return userRealId;
    }

    @Override //사용자의 패스워드를 반환
    public String getPassword() {
        return password;
    }

    @Override //계정 만료 여부
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override // 계정 잠금 여부 반환
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override // 패스워드의 만료 여부 반환
    public boolean isCredentialsNonExpired() {
        // 패스워드가 만료되었는지 확인하는 로직
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public UserResponseDto toResponseDto() {
        return UserResponseDto.builder()
                .email(email)
                .username(username)
                .userRealId(userRealId)
                .build();
    }
}
