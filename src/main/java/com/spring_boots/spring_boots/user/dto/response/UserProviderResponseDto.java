package com.spring_boots.spring_boots.user.dto.response;

import com.spring_boots.spring_boots.user.domain.Provider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProviderResponseDto {
    private Provider provider;
}
