package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.QuantityMeasurementEntity;

@Repository
public interface QuantityMeasurementRepository extends JpaRepository<QuantityMeasurementEntity, Long> {

    // ================= FETCH =================

//    List<QuantityMeasurementEntity> findByUserId(Long userId);
//
//    List<QuantityMeasurementEntity> findByOperation(String operation);
//
//    List<QuantityMeasurementEntity> findByThisMeasurementType(String measurementType);
//
//    List<QuantityMeasurementEntity> findByOperationAndUserId(String operation, Long userId);
//
//    List<QuantityMeasurementEntity> findByThisMeasurementTypeAndUserId(String type, Long userId);
//
//    List<QuantityMeasurementEntity> findByOperationAndThisMeasurementTypeAndUserId(
//            String operation, String type, Long userId);

    // ================= COUNT =================
//
//    long countByOperation(String operation);
//
//    long countByOperationAndUserId(String operation, Long userId);

    // ================= DELETE =================

    void deleteByUserId(Long userId);

    void deleteByOperationAndUserId(String operation, Long userId);

    void deleteByThisMeasurementTypeAndUserId(String type, Long userId);

    void deleteByOperationAndThisMeasurementTypeAndUserId(
            String operation, String type, Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM QuantityMeasurementEntity q WHERE q.id = :id AND q.userId = :userId")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM quantity_measurements WHERE id = :id", nativeQuery = true)
    void forceDelete(@Param("id") Long id);
}