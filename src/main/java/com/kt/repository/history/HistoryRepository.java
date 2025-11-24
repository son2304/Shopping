package com.kt.repository.history;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.history.History;

public interface HistoryRepository extends JpaRepository<History, Long> {
}
