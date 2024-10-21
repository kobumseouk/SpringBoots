package com.spring_boots.spring_boots.user.domain;

import com.spring_boots.spring_boots.common.BaseTimeEntity;
import com.spring_boots.spring_boots.orders.entity.Orders;
import com.spring_boots.spring_boots.user.dto.UserDto;
import com.spring_boots.spring_boots.user.dto.request.AdminGrantTokenRequestDto;
import com.spring_boots.spring_boots.user.dto.request.UserUpdateRequestDto;
import com.spring_boots.spring_boots.user.dto.response.UserResponseDto;
import com.spring_boots.spring_boots.user.dto.response.UsersInfoResponseDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

    @Column(name = "password")
    private String password;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "delete_reason")
    private String deleteReason;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsersInfo> usersInfoList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orders> ordersList = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
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
        //isDeleted 가 false 일 때만 활성화된 사용자로 간주
        return !isDeleted;
    }

    public void deleteUser() {
        this.isDeleted = true;
    }

    public UserResponseDto toResponseDto() {
        //UsersInfo 를 UsersInfoResponseDto 로 변경
        List<UsersInfoResponseDto> userInfoDtos = usersInfoList.stream()
                .map(UsersInfo::toUsersInfoResponseDto)
                .toList();

        return UserResponseDto.builder()
                .userId(userId)
                .email(email)
                .username(username)
                .userRealId(userRealId)
                .provider(provider)
                .role(role)
                .createdAt(getCreatedAt())
                .userInfoList(userInfoDtos)
                .message("사용자 있음")
                .build();
    }

    public void updateUser(UserUpdateRequestDto userUpdateRequestDto) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        //비밀번호 값이 들어있지않다면 변경 하지않는다.
        if (userUpdateRequestDto.getUpdatePassword() != null) {
            this.password = encoder.encode(userUpdateRequestDto.getUpdatePassword());
        }
        this.email = userUpdateRequestDto.getEmail();
    }

    public void updateToRole(AdminGrantTokenRequestDto adminGrantTokenRequestDto) {
        this.role = adminGrantTokenRequestDto.getRoles();
    }

    public String getMember() {
        return username;
    }

    public UserDto toUserDto() {
        return UserDto.builder()
                .userId(this.userId)
                .username(this.username)
                .userRealId(this.userRealId)
                .email(this.email)
                .password(this.password)
                .isDeleted(this.isDeleted)
                .deleteReason(this.deleteReason)
                .role(this.role)
                .provider(this.provider)
                .usersInfoList(this.usersInfoList)
                .ordersList(this.ordersList)
                .build();
    }

    //구글로 로그인시 업데이트
    public Users updateName(String username) {
        this.username = username;
        this.provider = Provider.GOOGLE;
        return this;
    }
}
