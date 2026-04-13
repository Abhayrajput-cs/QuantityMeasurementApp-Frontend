package com.example.controller;

import com.example.entity.QuantityMeasurementEntity;
import com.example.repository.HistoryRepository;
import com.example.service.HistoryService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/history")
@CrossOrigin(origins = "http://localhost:3000") 
public class HistoryController {

    @Autowired
    private HistoryService service;
    @Autowired
    private HistoryRepository repository;

    @GetMapping("/all")
    public List<QuantityMeasurementEntity> getAll(HttpServletRequest request) {
        return service.getAll(request);
    }

//    @GetMapping("/filter")
//    public List<QuantityMeasurementEntity> getFiltered(
//            @RequestParam(required = false) String operation,
//            @RequestParam(required = false) String type,
//            HttpServletRequest request) {
//
//        return service.getFiltered(operation, type, request);
//    }
    @GetMapping("/filter")
    public List<QuantityMeasurementEntity> getFiltered(
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String type,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        System.out.println("OP: " + operation);
        System.out.println("TYPE: " + type);
        System.out.println("USER: " + userId);

        if (userId == null) {
            throw new RuntimeException("Missing X-User-Id header");
        }

        return repository.findByUserId(Long.parseLong(userId));
    }
    
    
}