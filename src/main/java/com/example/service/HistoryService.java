package com.example.service;

import com.example.entity.QuantityMeasurementEntity;
import com.example.repository.HistoryRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class HistoryService {

    @Autowired
    private HistoryRepository repository;

    public Long getLoggedInUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");

        if (userId == null) {
            throw new RuntimeException("User ID missing");
        }

        return Long.parseLong(userId);
    }

    public List<QuantityMeasurementEntity> getAll(HttpServletRequest request) {
        return repository.findByUserId(getLoggedInUserId(request));
    }

    public List<QuantityMeasurementEntity> getFiltered(String operation, String type, HttpServletRequest request) {

        Long userId = getLoggedInUserId(request);

        if (operation != null && type != null) {
            return repository.findByOperationAndThisMeasurementTypeAndUserId(operation, type, userId);
        }

        if (operation != null) {
            return repository.findByOperationAndUserId(operation, userId);
        }

        if (type != null) {
            return repository.findByThisMeasurementTypeAndUserId(type, userId);
        }

        return repository.findByUserId(userId);
    }
}