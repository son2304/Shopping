package com.kt.security;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtService {
	private final JwtProperties jwtProperties;
	// Key는 설정한 어떤 임의의 값을 통해서 생성함
	public String issue(Long id, Date expiration) {
		// id 값은 jwt의 식별자 같은 개념 -> User의 id값
		// claim -> jwt안에 들어갈 정보를 Map형태로 넣는데 id, 1

		// 2가지의 토큰으로 웹에서는 제어
		// access token -> 짧은 유효기간 : 5분 - refresh token으로 새로운 access token발급
		// refresh token -> 긴 유효기간 : 12시간 - 만료되면 로그인 다시 해야함

		return Jwts.builder()
			.subject("kt-cloud-shopping")
			.issuer("son")
			.issuedAt(new Date())
			.id(id.toString())
			.expiration(expiration)
			// Key를 구현해서 넣어줘야 함
			.signWith((jwtProperties.getSecret()))
			.compact();
	}

	public Date getAccessExpiration() {
		return jwtProperties.getAccessTokenExpirationDate();
	}
	public Date getRefreshExpiration() {
		return jwtProperties.getRefreshTokenExpirationDate();
	}

	public boolean validate(String token) {
		return Jwts.parser()
			.verifyWith(jwtProperties.getSecret())
			.build()
			.isSigned(token);
	}

	public Long parseId(String token) {
		var id = Jwts.parser()
			.verifyWith(jwtProperties.getSecret())
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getId();

		return Long.valueOf(id);
	}
}
