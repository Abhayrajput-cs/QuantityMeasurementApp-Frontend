package com.example.service;

import com.example.dto.*;

import com.example.entity.QuantityMeasurementEntity;

import java.util.List;

public interface IQuantityMeasurementService {


    QuantityMeasurementEntity convert(QuantityInputDTO input);

    QuantityMeasurementEntity add(QuantityInputDTO input);

//    List<QuantityMeasurementEntity> getHistoryByOperation(String operation);


	QuantityMeasurementEntity compare(QuantityInputDTO input);

	QuantityMeasurementEntity subtract(QuantityInputDTO input);

	QuantityMeasurementEntity multiply(QuantityInputDTO input);

	QuantityMeasurementEntity divide(QuantityInputDTO input);
//	List<QuantityMeasurementEntity> getAll();
//
//	QuantityMeasurementEntity getById(Long id);


	void delete(Long id);

	void deleteAllByUser();

	void deleteFiltered(String operation, String type);

//	List<QuantityMeasurementEntity> getFiltered(String operation, String type);
	

	
}