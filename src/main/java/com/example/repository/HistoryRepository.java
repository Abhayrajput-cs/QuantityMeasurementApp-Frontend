package com.example.repository;

import com.example.entity.QuantityMeasurementEntity;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.stereotype.Repository;
@Repository
public interface HistoryRepository extends JpaRepository<QuantityMeasurementEntity, Long> {

    List<QuantityMeasurementEntity> findByUserId(Long userId);

    List<QuantityMeasurementEntity> findByOperationAndUserId(String operation, Long userId);

    List<QuantityMeasurementEntity> findByThisMeasurementTypeAndUserId(String type, Long userId);

    List<QuantityMeasurementEntity> findByOperationAndThisMeasurementTypeAndUserId(
            String operation, String type, Long userId);
}