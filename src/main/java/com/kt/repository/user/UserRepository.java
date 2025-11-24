package com.kt.repository.user;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kt.common.exception.CustomException;
import com.kt.common.exception.ErrorCode;
import com.kt.domain.user.User;

import jakarta.validation.constraints.NotNull;

// <T, ID>
// T : Entity 클래스 => User
// ID : Entity 클래스의 PK 타입 => Long
public interface UserRepository extends JpaRepository<User, Long> {
	// 여기에 쿼리를 작성해야 함
	// JPA에서는 쿼리를 작성하는 3가지 방법 존재
	// 1. 네이티브 쿼리를 작성 (3)
	// 2. JPQL 작성 -> 네이티브 쿼리랑 같은데 Entity 기반 (2) - 너무 길어진 메소드 이름을 쿼리 작성으로 숨김
	// 3. querymethod 작성 -> 메소드 이름을 쿼리처럼 작성 (1 제일 많이 씀) - 길어지면 상당히 이상해보임
	// 찾는다 : findByXX , 존재하는가 : existsByXX, 삭제 : deleteByXX ....

	Boolean existsByLoginId(String loginId);

	Optional<User> findByLoginId(String loginId);

	@Query(value = """
SELECT exists (SELECT u FROM User u WHERE u.loginId = ?1)
""")
	Boolean existsByLoginIdJPQL(String loginId);

	Page<User> findAllByNameContaining(String name, Pageable pageable);

	@Query(value = """
	SELECT DISTINCT u FROM User u
		LEFT JOIN FETCH u.orders o
			WHERE u.id = :id

	""")
	@NotNull Optional<User> findById(@NotNull Long id);

	default User findByIdOrThrow(Long id, ErrorCode errorCode) {
		return findById(id).orElseThrow(() -> new CustomException(errorCode));
	}
}
