package com.kt.domain.product;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.util.Strings;

import com.kt.common.support.BaseEntity;
import com.kt.common.exception.ErrorCode;
import com.kt.common.support.Preconditions;
import com.kt.domain.orderproduct.OrderProduct;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Product extends BaseEntity {

	private String name;
	private Long price;
	private Long stock;
	@Enumerated(EnumType.STRING)
	private ProductStatus status = ProductStatus.ACTIVATED;

	// @Version
	// private Long version;

	@OneToMany(mappedBy = "product")
	private List<OrderProduct> orderProducts = new ArrayList<>();

	public Product(String name, Long price, Long stock) {
		Preconditions.validate(Strings.isNotBlank(name), ErrorCode.INVALID_PARAMETER);
		Preconditions.validate(price >= 0, ErrorCode.INVALID_PARAMETER);
		Preconditions.validate(stock >= 0, ErrorCode.INVALID_PARAMETER);
		this.name = name;
		this.price = price;
		this.stock = stock;
	}

	public void update(String name, Long price, Long stock) {
		this.name = name;
		this.price = price;
		this.stock = stock;
	}

	public void soldOut(){
		this.status = ProductStatus.SOLD_OUT;
	}

	public void inActivate(){
		this.status = ProductStatus.IN_ACTIVATED;
	}

	public void activate(){
		this.status = ProductStatus.ACTIVATED;
	}

	public void delete(){
		this.status = ProductStatus.DELETED;
	}

	public void decreaseStock(Long quantity){
		this.stock -= quantity;
	}

	public void increaseStock(Long quantity){
		this.stock += quantity;
	}

	public boolean canProvide(Long quantity){
		return this.stock >= quantity;
	}

	public void mapToOrderProduct(OrderProduct orderProduct){
		this.orderProducts.add(orderProduct);
	}

	// 생성
	// 수정 - 정보 수정, 상태 수정
	// 삭제
	// 조회(리스트, 단건)
	// 상태변경
	// 재고 수량 감소
	// 재고 수량 증가

}
