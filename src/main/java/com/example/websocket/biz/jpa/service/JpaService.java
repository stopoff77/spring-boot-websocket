package com.example.websocket.biz.jpa.service;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.websocket.biz.dual.dto.CallMessageDto;
import com.example.websocket.biz.jpa.entity.CallMessageEntity;
import com.example.websocket.biz.jpa.repository.CallMessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaService {

    private final CallMessageRepository repository;

    @Transactional
    public Long createItem(CallMessageDto.CreateRequest request) {
        CallMessageEntity entity      = request.toEntity();
        CallMessageEntity savedEntity = repository.save(entity);

        String str = null;
        str.toString();

        return savedEntity.getId();
    }

    public CallMessageDto.Response getItem(Long id) {
        CallMessageEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다. id=" + id));
        return CallMessageDto.Response.from(entity);
    }

    public List<CallMessageDto.Response> getAllItems() {
        return repository.findAll().stream()
                .map(CallMessageDto.Response::from)
                .toList();
    }

    @Transactional
    public void updateItem(Long id, CallMessageDto.CreateRequest request) {
        CallMessageEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다. id=" + id));

        // Dirty Checking(변경 감지)에 의해 트랜잭션 종료 시 자동 update 쿼리 실행
        entity.updateInfo(request.getName(), request.getPrice(), request.getStockQuantity());
    }
}
