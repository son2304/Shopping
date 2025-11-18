package com.kt.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {
	private final RedisProperties redisProperties;
	// redisson에서 분산락 동작은
	// DB에 접근하기 전에 락을 획득하고 redis <key, value>에 저장
	// 작업 수행
	// 작업이 끝나면 락 해제 - redis에서 삭제
	// redis는 만료시간을 설정할 수 있음

	@Bean
	public RedissonClient redissonClient() {
		var config = new Config();
		var uri = String.format("redis://%s:%s", redisProperties.getHost(), redisProperties.getPort());

		config.useSingleServer().setAddress(uri);

		return Redisson.create(config);
	}
}
