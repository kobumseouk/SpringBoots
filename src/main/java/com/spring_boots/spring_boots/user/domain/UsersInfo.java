package com.spring_boots.spring_boots.user.domain;

import com.spring_boots.spring_boots.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UsersInfo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_info_id")
    private Long userInfoId;

    @Column(name = "address")
    private String address; //우편번호

    @Column(name = "streetAddress")
    private String streetAddress; //도로명 주소

    @Column(name = "detailedAddress")
    private String detailedAddress; //상세 주소

    @Column(name = "phone")
    private String phone;

    @ManyToOne
    @JoinColumn(name = "user_id")   //왜래키 설정
    private Users users;

}
