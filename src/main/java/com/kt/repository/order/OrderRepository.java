package com.kt.repository.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.order.Order;

import jakarta.validation.constraints.NotNull;

public interface OrderRepository extends JpaRepository<Order, Long> {
	// 1. 네이티브 쿼리
	// 2. JPQL로 작성
	// 3. 쿼리 메소드로 작성
	// 4. 조회할때는 동적쿼리를 작성하게 해줄 수 있는 querydsl 사용하자

	@NotNull
	@EntityGraph(attributePaths = {"orderProducts", "orderProduct.product"})
	List<Order> findAllByUserId(Long userId);

}
