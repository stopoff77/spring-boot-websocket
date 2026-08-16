package com.example.websocket.biz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.websocket.biz.entity.CallMessageEntity;

public interface CallMessageRepository extends JpaRepository<CallMessageEntity, Long> {
    // 쿼리 메서드 기능: 상품명으로 검색
    List<CallMessageEntity> findByNameContaining(String name);
}
