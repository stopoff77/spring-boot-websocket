package com.example.websocket.biz.jpa.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.websocket.biz.dual.dto.CallMessageDto;
import com.example.websocket.biz.jpa.service.JpaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/jpa")
@RequiredArgsConstructor
public class JpaController {

    private final JpaService jpaService;

    @GetMapping("/create")
    public ResponseEntity<Long> createItem() {
        CallMessageDto.CreateRequest request = new CallMessageDto.CreateRequest("가나다", 1000, 20);
        Long                         id      = jpaService.createItem(request);
        return ResponseEntity.ok(id);
    }

    @PostMapping
    public ResponseEntity<Long> createItem(@RequestBody CallMessageDto.CreateRequest request) {
        Long id = jpaService.createItem(request);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CallMessageDto.Response> getItem(@PathVariable("id") Long id) {
        return ResponseEntity.ok(jpaService.getItem(id));
    }

    @GetMapping
    public ResponseEntity<List<CallMessageDto.Response>> getAllItems() {
        return ResponseEntity.ok(jpaService.getAllItems());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateItem(@PathVariable("id") Long id,
            @RequestBody CallMessageDto.CreateRequest request) {
        jpaService.updateItem(id, request);
        return ResponseEntity.ok().build();
    }
}
