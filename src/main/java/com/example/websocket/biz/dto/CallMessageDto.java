package com.example.websocket.biz.dto;

import com.example.websocket.biz.entity.CallMessageEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CallMessageDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor // 모든 필드를 인자로 받는 생성자 생성
    public static class CreateRequest {
        private String  name;
        private Integer price;
        private Integer stockQuantity;

        public CallMessageEntity toEntity() {
            return new CallMessageEntity(name, price, stockQuantity);
        }
    }

    @Getter
    @Builder
    public static class Response {
        private Long    id;
        private String  name;
        private Integer price;
        private Integer stockQuantity;

        public static Response from(CallMessageEntity entity) {
            return Response.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .price(entity.getPrice())
                    .stockQuantity(entity.getStockQuantity())
                    .build();
        }
    }
}
