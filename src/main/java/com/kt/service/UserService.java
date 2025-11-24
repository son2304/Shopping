package com.kt.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.ErrorCode;
import com.kt.common.Preconditions;
import com.kt.domain.order.Order;
import com.kt.domain.orderproduct.OrderProduct;
import com.kt.domain.product.Product;
import com.kt.domain.user.User;
import com.kt.dto.user.UserRequest;
import com.kt.repository.order.OrderRepository;
import com.kt.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

// 구현체가 하나 이상 필요로해야 인터페이스가 의미가 있다.
// 인터페이스 : 구현체 , 1 : 1 로 다 나눠야 하는가
// 관례를 지키려고 추상화를 굳이하는 것을 관습적 추상화 라고 함
// 인터페이스를 굳이 나눴을 때 불편한 점

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final OrderRepository orderRepository;

	// 트랜잭션 처리해줘
	// PSA - Portable Service Abstraction
	// 환경설정을 살짝 바꿔서 일관된 서비스를 제공하는 것
	public void create(UserRequest.Create request) {
		var newUser = User.normalUser(
			request.loginId(),
			passwordEncoder.encode(request.password()),
			request.name(),
			request.email(),
			request.mobile(),
			request.gender(),
			request.birthday(),
			LocalDateTime.now(),
			LocalDateTime.now()
		);

		userRepository.save(newUser);
	}


	public boolean isDuplicateLoginId(String loginId) {
		return userRepository.existsByLoginId(loginId);
	}

	public void changePassword(Long id, String oldPassword ,String password) {
		var user = userRepository.findByIdOrThrow(id, ErrorCode.NOT_FOUND_USER);

		// 검증 진입
		// 긍정적인 상황만 생각하자 -> 패스워드가 이전것과 달라야 => 긍정적 시나리오
		// 패스워드가 같으면 안되는데 -> 긍적적이지 않은 시나리오
		// 검증에 안걸리길 원하는 상황을 적어주면됨 :
		Preconditions.validate(user.getPassword().equals(oldPassword), ErrorCode.DOES_NOT_MATCH_OLD_PASSWORD);
		Preconditions.validate(!oldPassword.equals(password), ErrorCode.CAN_NOT_ALLOWED_SAME_PASSWORD);

		user.changePassword(password);
	}

	// Pageable 인터페이스
	public Page<User> search(Pageable pageable, String keyword) {
		return userRepository.findAllByNameContaining(keyword, pageable);
	}

	public User detail(Long id) {
		return userRepository.findByIdOrThrow(id, ErrorCode.NOT_FOUND_USER);
	}

	public void update(Long id, String name, String email, String mobile) {
		var user = userRepository.findByIdOrThrow(id, ErrorCode.NOT_FOUND_USER);
		user.update(name, email, mobile);
	}

	public void delete(Long id) {
		// 삭제하기 전에 할일이 있다면
		// var user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
		// userRepository.delete(user); // select 쿼리와 delete 쿼리가 나감
		userRepository.deleteById(id); // 쿼리가 한번 발생하는 방식

		// 삭제에는 두 가지 개념 - softdelete, harddelete
	}

	public void getOrders(Long id) {
		var user = userRepository.findByIdOrThrow(id, ErrorCode.NOT_FOUND_USER);
		var orders = orderRepository.findAllByUserId(user.getId());

		var products = orders.stream()
			.flatMap(order -> order.getOrderProducts().stream())
			.map(orderProduct -> orderProduct.getProduct().getName()).toList();

		// var statuses = orders.stream()
		// 	.flatMap(order -> order.getOrderProducts().stream())
		// 	.map(orderProduct -> orderProduct.getProduct().getStatus()).toList();

		// N개의 주문이 있는데 N개의 주문엔 상품이 존재하는데 가지수가 1만개

		// Stream의 연산과정
		// 1. 스트림생성
		// 2. 중간연산 -> 여러번 가능 O
		// 3. 최종연산 -> 여러번 가능 X -> 재사용 불가능

		// N + 1 문제를 해결하는 방법
		// 1. fetch join 사용 -> JPQL 전용 -> 딱 1번 사용, 2번 사용하면 에러 발생
		// 2. @EntityGraph사용 -> JPA 표준 기능 -> 여러 번 사용 가능
		// 3. batch fetch size 옵션 사용 -> 전역설정 -> paging 동작 원리와 같아서 성능 이슈 있을 수 있음
		// 4. @BatchSize 어노테이션 사용 -> 특정 엔티티에만 적용 가능
		// 5. native query 사용해서 해결

		// N + 1 이 어디서 발생할까
		// Collection, stream, foreach

		// 연관관계를 아예 끊어버리는 방법을 쓰기도 함 -> 엔티티 자체를 느슨하게 결합시킴
	}
}