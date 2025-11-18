package com.kt.service;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.CustomException;
import com.kt.common.ErrorCode;
import com.kt.common.Lock;
import com.kt.common.Preconditions;
import com.kt.domain.order.Order;
import com.kt.domain.order.Receiver;
import com.kt.domain.orderproduct.OrderProduct;
import com.kt.repository.orderproduct.OrderProductRepository;
import com.kt.repository.order.OrderRepository;
import com.kt.repository.product.ProductRepository;
import com.kt.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final OrderRepository orderRepository;
	private final OrderProductRepository orderProductRepository;
	private final RedissonClient redissonClient;

	// 주문 생성
	@Lock(key = Lock.Key.STOCK)
	public void create(
		Long userId,
		Long productId,
		String receiverName,
		String receiverAddress,
		String receiverMobile,
		Long quantity
	) {
		// getLock에서 문자열을 인자로 줘야함
		var rLock = redissonClient.getLock("stock");

		// 1. try catch finally
		// 2. 메소드 레벨에서 throws 한다.
		try{
			var available = rLock.tryLock(6L, 5L, TimeUnit.MILLISECONDS);

			// DB접근 전 여기에서 락 획득
			Preconditions.validate(available, ErrorCode.FAIL_ACQUIRED_LOCK);
			// var product = productRepository.findByIdPessimistic(productId).orElseThrow();
			var product = productRepository.findByIdOrThrow(productId);

			Preconditions.validate(product.canProvide(quantity), ErrorCode.NOT_ENOUGH_STOCK);

			var user = userRepository.findByIdOrThrow(userId, ErrorCode.NOT_FOUND_USER);

			var receiver = new Receiver(
				receiverName,
				receiverAddress,
				receiverMobile
			);

			var order = orderRepository.save(Order.create(receiver, user));

			var orderProduct = orderProductRepository.save(new OrderProduct(order, product, quantity));

			// 주문 생성 완료
			product.decreaseStock(quantity);

			product.mapToOrderProduct(orderProduct);
			order.mapToOrderProduct(orderProduct);

		} catch (InterruptedException e) {
			throw new CustomException(ErrorCode.ERROR_SYSTEM);
		}  finally {
			rLock.unlock();
		}

		// CustomException이 터질 때는 unlock을 못하고 있음

	}
}
