package com.spring_boots.spring_boots.user.dto.request;

import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.domain.UsersInfo;
import com.spring_boots.spring_boots.user.dto.response.UsersInfoResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class UserUpdateRequestDto {
    private String currentPassword; //현재 비밀번호
    private String updatePassword;  //변경할 비밀번호
    private String email; //변경할 이메일
    private List<UsersInfoResponseDto> address;

    public UsersInfo toUsersInfo(Users user) {
        UsersInfoResponseDto usersInfo = address.get(0);    //여러개의 리스트중 첫번째 갖고오기
        return UsersInfo.builder()
                .address(usersInfo.getAddress())
                .streetAddress(usersInfo.getStreetAddress())
                .detailedAddress(usersInfo.getDetailedAddress())
                .phone(usersInfo.getPhone())
                .users(user)
                .build();
    }
}
