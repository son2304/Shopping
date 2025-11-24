package com.kt.controller.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.resoponse.ApiResult;
import com.kt.common.support.TechUpLogger;
import com.kt.domain.history.HistoryType;
import com.kt.dto.auth.LoginRequest;
import com.kt.dto.auth.LoginResponse;
import com.kt.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	// 인증 관련 컨트롤러
	// 인증방식 크게 3가지 있음
	// 1. 세션 기반 인증 -> 서버쪽 작은 공간에 사용자 정보를 저장 - 만료시간 있음
	// 장점 : 서버에서 관리하기 때문에 보안성이 좋음
	// A서버에서 인가를 해줌, 세션에서 저장하고 있음 / B서버 세션에는 인가된 정보가 있을까? -> 없음
	// 해결책으로 세션클러스터링, 스티키세션 -> redis등 외부 저장소를 통해서 단일 세션 구현, 세션이 A서버에서 생성되었다면 A서버로 트래픽 고정
	// 2. 토큰 기반 인증 (JWT) -> 사용자가 토큰을 가지고 있다가 요청할 때마다 같이 줌 -> 서버 입장에서 신뢰성이 없음
	// 단점 : 매번 검사를 해야함
	// 장점 : 서버에서 관리하지 않아서 부하가 적음, 분산 환경에 유리
	// 3. OAuth2.0 기반 인증
	// 내 서버에서 하는게 아닌 남한테 맡기는 방식 (구글, 카카오, 네이버, 깃허브, 페이스북 등) - 소셜로그인
	// 장점 -> 사용자 편하려고 만든게 아닌 서버 개발자들 편하려고 쓰는거임
	// 왜 편한가? -> 개인정보를 취급하지 않아도 됨, 인가 작업을 내가 안해도 됨

	@TechUpLogger(type = HistoryType.LOGIN, content = "사용자 로그인")
	@PostMapping("/login")
	public ApiResult<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
		var pair = authService.login(loginRequest.loginId(), loginRequest.password());

		return ApiResult.ok(new LoginResponse(pair.getFirst(), pair.getSecond()));
	}
}
