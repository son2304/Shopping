package com.kt.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.exception.CustomException;
import com.kt.common.exception.ErrorCode;
import com.kt.common.support.Preconditions;
import com.kt.repository.user.UserRepository;
import com.kt.security.JwtService;
import com.mysema.commons.lang.Pair;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public Pair<String, String> login(String loginId, String password) {
		var user = userRepository.findByLoginId(loginId)
			.orElseThrow( () -> new CustomException(ErrorCode.FAIL_LOGIN));

		// 비밀번호가 일치하는지 확인
		// Bcrypt로 암호화된 정보 -> 단방향 해시암호화 -> 기본 5번 해시알고리즘 돌림
		// 요청 들어온 password를 또 해시알고리즘 돌려서 맞는지 비교함
		Preconditions.validate(passwordEncoder.matches(password, user.getPassword()), ErrorCode.FAIL_LOGIN);

		// 로그인 성공 처리 -> JWT 토큰을 발급
		// 헤더에 넣어서 줄수도 있고, 바디에 넣어서 줄수도 있고(v), 쿠키에 넣어서 줄수도 있고
		// access 토큰과 refresh 토큰을 발급해서 보내줘야 함
		var accessToken = jwtService.issue(user.getId(), jwtService.getAccessExpiration());
		var refreshToken = jwtService.issue(user.getId(), jwtService.getRefreshExpiration());

		return Pair.of(accessToken, refreshToken);
	}
}
