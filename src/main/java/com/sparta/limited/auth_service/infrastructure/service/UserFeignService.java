package com.sparta.limited.auth_service.infrastructure.service;

import com.sparta.limited.auth_service.application.dto.request.AuthLoginRequest;
import com.sparta.limited.auth_service.application.dto.request.AuthSignupRequest;
import com.sparta.limited.auth_service.application.dto.response.AuthSignupResponse;
import com.sparta.limited.auth_service.application.info.UserInfo;
import com.sparta.limited.auth_service.application.mapper.AuthDtoMapper;
import com.sparta.limited.auth_service.infrastructure.dto.request.UserCreateInternalRequest;
import com.sparta.limited.auth_service.infrastructure.dto.response.UserCreateInternalResponse;
import com.sparta.limited.auth_service.infrastructure.dto.response.UserSearchInternalResponse;
import com.sparta.limited.auth_service.infrastructure.feign.UserFeignClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserFeignService {

    private final UserFeignClient userFeignClient;

    public AuthSignupResponse createUser(AuthSignupRequest request, String password) {
        UserCreateInternalRequest userCreateRequest = AuthDtoMapper.toCreateUserInternalDto(request,
            password);
        try {
            UserCreateInternalResponse userCreateResponse = userFeignClient.createUser(
                userCreateRequest);
            return AuthDtoMapper.toSignupExternalResponse(userCreateResponse);
        } catch (FeignException e) {
            if (e.status() == 409) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 사용자명 입니다.");
            }
            throw new RuntimeException("user-service의 사용자 생성 기능에 문제가 발생했습니다.");
        }

    }

    public UserInfo searchUserByUsername(AuthLoginRequest request) {
        try {
            UserSearchInternalResponse userSearchResponse = userFeignClient.searchUserByUsername(
                request.getUsername());
            return UserInfo.from(
                userSearchResponse.getUserId(),
                userSearchResponse.getUsername(),
                userSearchResponse.getPassword(),
                userSearchResponse.getRole(),
                userSearchResponse.getGender(),
                userSearchResponse.getAge(),
                userSearchResponse.getAddress()
            );
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다");
            }
            throw new RuntimeException("user-service의 username 기반 사용자 조회 기능에 문제가 발생했습니다.");
        }

    }
}
