package com.example.websocket.biz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CALL_MESSAGE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 규약상 기본 생성자 필요
public class CallMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stockQuantity;

    public CallMessageEntity(String name, Integer price, Integer stockQuantity) {
        this.name          = name;
        this.price         = price;
        this.stockQuantity = stockQuantity;
    }

    // 비즈니스 로직 (재고 수정 등)
    public void updateInfo(String name, Integer price, Integer stockQuantity) {
        this.name          = name;
        this.price         = price;
        this.stockQuantity = stockQuantity;
    }
}
