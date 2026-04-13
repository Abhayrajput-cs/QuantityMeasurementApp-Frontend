
package com.example.service;

import com.example.dto.QuantityDTO;
import com.example.dto.QuantityInputDTO;
import com.example.entity.QuantityMeasurementEntity;
import com.example.repository.QuantityMeasurementRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.*;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

@Service
@Transactional
public class QuantityMeasurementServiceImpl implements IQuantityMeasurementService {

    @Autowired
    private QuantityMeasurementRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    // ================= USER =================

    private Long getLoggedInUserId() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes())
                        .getRequest();

        String userId = request.getHeader("X-User-Id");

        if (userId == null) {
            throw new RuntimeException("User ID not found in header");
        }

        return Long.parseLong(userId);
    }

    // ================= HISTORY CALL =================

    private void sendToHistory(QuantityMeasurementEntity entity) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", String.valueOf(getLoggedInUserId()));

            HttpEntity<QuantityMeasurementEntity> request =
                    new HttpEntity<>(entity, headers);

            restTemplate.postForObject(
                    "http://localhost:8083/api/v1/history/save",
                    request,
                    Void.class
            );

        } catch (Exception e) {
            System.out.println("History service down, skipping...");
        }
    }

    // ================= BASE ENTITY =================

    private QuantityMeasurementEntity createBaseEntity(QuantityInputDTO input) {
        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();

        entity.setThisValue(input.getThisQuantityDTO().getValue());
        entity.setThisUnit(input.getThisQuantityDTO().getUnit());
        entity.setThisMeasurementType(input.getThisQuantityDTO().getMeasurementType());

        entity.setThatValue(input.getThatQuantityDTO().getValue());
        entity.setThatUnit(input.getThatQuantityDTO().getUnit());
        entity.setThatMeasurementType(input.getThatQuantityDTO().getMeasurementType());

        entity.setCreatedAt(LocalDateTime.now());
        entity.setUserId(getLoggedInUserId());

        return entity;
    }

    // ================= COMMON HELPERS =================

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    // ---- LENGTH ----
    private double toMillimeter(double value, String unit) {
        switch (unit.toUpperCase()) {
            case "MILLIMETER": return value;
            case "CENTIMETER": return value * 10;
            case "METER": return value * 1000;
            case "KILOMETER": return value * 1_000_000;
            case "INCH": return value * 25.4;
            case "FEET": return value * 304.8;
            case "YARD": return value * 914.4;
            default: throw new RuntimeException("Invalid length unit");
        }
    }

    private double fromMillimeter(double value, String unit) {
        switch (unit.toUpperCase()) {
            case "MILLIMETER": return value;
            case "CENTIMETER": return value / 10;
            case "METER": return value / 1000;
            case "KILOMETER": return value / 1_000_000;
            case "INCH": return value / 25.4;
            case "FEET": return value / 304.8;
            case "YARD": return value / 914.4;
            default: throw new RuntimeException("Invalid length unit");
        }
    }

    // ---- TEMPERATURE (STANDARDIZED USING KELVIN) ----
    private double toKelvin(double value, String unit) {
        switch (unit.toUpperCase()) {
            case "CELSIUS": return value + 273.15;
            case "FAHRENHEIT": return (value - 32) * 5 / 9 + 273.15;
            case "KELVIN": return value;
            default: throw new RuntimeException("Invalid temperature unit");
        }
    }

    private double fromKelvin(double value, String unit) {
        switch (unit.toUpperCase()) {
            case "CELSIUS": return value - 273.15;
            case "FAHRENHEIT": return (value - 273.15) * 9 / 5 + 32;
            case "KELVIN": return value;
            default: throw new RuntimeException("Invalid temperature unit");
        }
    }

    // ---- VOLUME ----
    private double toMilliliter(double value, String unit) {
        switch (unit.toUpperCase()) {
            case "LITER": return value * 1000;
            case "MILLILITER": return value;
            case "CUBIC_METER": return value * 1_000_000;
            case "GALLON": return value * 3785.41;
            default: throw new RuntimeException("Invalid volume unit");
        }
    }

    private double fromMilliliter(double value, String unit) {
        switch (unit.toUpperCase()) {
            case "LITER": return value / 1000;
            case "MILLILITER": return value;
            case "CUBIC_METER": return value / 1_000_000;
            case "GALLON": return value / 3785.41;
            default: throw new RuntimeException("Invalid volume unit");
        }
    }

    // ---- WEIGHT ----
    private double toGrams(double value, String unit) {
        switch (unit.toUpperCase()) {
            case "MILLIGRAM": return value / 1000;
            case "GRAM": return value;
            case "KILOGRAM": return value * 1000;
            case "TON": return value * 1_000_000;
            case "OUNCE": return value * 28.3495;
            case "POUND": return value * 453.592;
            default: throw new RuntimeException("Invalid weight unit");
        }
    }

    private double fromGrams(double value, String unit) {
        switch (unit.toUpperCase()) {
            case "MILLIGRAM": return value * 1000;
            case "GRAM": return value;
            case "KILOGRAM": return value / 1000;
            case "TON": return value / 1_000_000;
            case "OUNCE": return value / 28.3495;
            case "POUND": return value / 453.592;
            default: throw new RuntimeException("Invalid weight unit");
        }
    }

    // ================= OPERATIONS =================

    @Override
    public QuantityMeasurementEntity add(QuantityInputDTO input) {

        QuantityMeasurementEntity entity = createBaseEntity(input);

        double a = input.getThisQuantityDTO().getValue();
        double b = input.getThatQuantityDTO().getValue();

        String type = input.getThisQuantityDTO().getMeasurementType();
        String unitA = input.getThisQuantityDTO().getUnit();
        String unitB = input.getThatQuantityDTO().getUnit();

        double result;

        switch (type.toUpperCase()) {
            case "LENGTH":
                result = fromMillimeter(
                        toMillimeter(a, unitA) + toMillimeter(b, unitB), unitA);
                break;

            case "TEMPERATURE":
                result = a + fromKelvin(toKelvin(b, unitB), unitA);
                break;

            case "VOLUME":
                result = fromMilliliter(
                        toMilliliter(a, unitA) + toMilliliter(b, unitB), unitA);
                break;

            case "WEIGHT":
                result = fromGrams(
                        toGrams(a, unitA) + toGrams(b, unitB), unitA);
                break;

            default:
                throw new RuntimeException("Unsupported type");
        }

        entity.setResultValue(round(result));
        entity.setResultUnit(unitA);
        entity.setOperation("ADD");

        QuantityMeasurementEntity saved = repository.save(entity);
        sendToHistory(saved);
        return saved;
    }

    @Override
    public QuantityMeasurementEntity subtract(QuantityInputDTO input) {

        QuantityMeasurementEntity entity = createBaseEntity(input);

        double a = input.getThisQuantityDTO().getValue();
        double b = input.getThatQuantityDTO().getValue();

        String type = input.getThisQuantityDTO().getMeasurementType();
        String unitA = input.getThisQuantityDTO().getUnit();
        String unitB = input.getThatQuantityDTO().getUnit();

        double result;

        switch (type.toUpperCase()) {
            case "LENGTH":
                result = a - fromMillimeter(toMillimeter(b, unitB), unitA);
                break;

            case "TEMPERATURE":
                result = a - fromKelvin(toKelvin(b, unitB), unitA);
                break;

            case "VOLUME":
                result = a - fromMilliliter(toMilliliter(b, unitB), unitA);
                break;

            case "WEIGHT":
                result = a - fromGrams(toGrams(b, unitB), unitA);
                break;

            default:
                throw new RuntimeException("Unsupported type");
        }

        entity.setResultValue(round(result));
        entity.setResultUnit(unitA);
        entity.setOperation("SUBTRACT");

        QuantityMeasurementEntity saved = repository.save(entity);
        sendToHistory(saved);
        return saved;
    }

    @Override
    public QuantityMeasurementEntity multiply(QuantityInputDTO input) {

        QuantityMeasurementEntity entity = createBaseEntity(input);

        double a = input.getThisQuantityDTO().getValue();
        double b = input.getThatQuantityDTO().getValue();

        entity.setResultValue(round(a * b));
        entity.setResultUnit("COMPOSITE");
        entity.setOperation("MULTIPLY");

        QuantityMeasurementEntity saved = repository.save(entity);
        sendToHistory(saved);
        return saved;
    }

    @Override
    public QuantityMeasurementEntity divide(QuantityInputDTO input) {

        double a = input.getThisQuantityDTO().getValue();
        double b = input.getThatQuantityDTO().getValue();

        if (b == 0) throw new ArithmeticException("Cannot divide by zero");

        QuantityMeasurementEntity entity = createBaseEntity(input);

        entity.setResultValue(round(a / b));
        entity.setResultUnit("RATIO");
        entity.setOperation("DIVIDE");

        QuantityMeasurementEntity saved = repository.save(entity);
        sendToHistory(saved);
        return saved;
    }

    @Override
    public QuantityMeasurementEntity convert(QuantityInputDTO input) {

        QuantityDTO from = input.getThisQuantityDTO();
        QuantityDTO to = input.getThatQuantityDTO();

        double value = from.getValue();
        String type = from.getMeasurementType();

        double result;

        switch (type.toUpperCase()) {
            case "LENGTH":
                result = fromMillimeter(toMillimeter(value, from.getUnit()), to.getUnit());
                break;

            case "TEMPERATURE":
                result = fromKelvin(toKelvin(value, from.getUnit()), to.getUnit());
                break;

            case "VOLUME":
                result = fromMilliliter(toMilliliter(value, from.getUnit()), to.getUnit());
                break;

            case "WEIGHT":
                result = fromGrams(toGrams(value, from.getUnit()), to.getUnit());
                break;

            default:
                throw new RuntimeException("Unsupported type");
        }

        QuantityMeasurementEntity entity = createBaseEntity(input);

        entity.setResultValue(round(result));
        entity.setResultUnit(to.getUnit());
        entity.setOperation("CONVERT");

        QuantityMeasurementEntity saved = repository.save(entity);
        sendToHistory(saved);
        return saved;
    }

    @Override
    public QuantityMeasurementEntity compare(QuantityInputDTO input) {

        QuantityMeasurementEntity entity = createBaseEntity(input);

        double a = input.getThisQuantityDTO().getValue();
        double b = input.getThatQuantityDTO().getValue();

        boolean equal = Math.abs(a - b) < 0.0001;

        entity.setResultValue(equal ? 1.0 : 0.0);
        entity.setResultUnit(equal ? "TRUE" : "FALSE");
        entity.setOperation("COMPARE");

        QuantityMeasurementEntity saved = repository.save(entity);
        sendToHistory(saved);
        return saved;
    }

    // ================= DELETE =================

    @Override
    public void delete(Long id) {
        repository.deleteByIdAndUserId(id, getLoggedInUserId());
    }

    public void deleteAllByUser() {
        repository.deleteByUserId(getLoggedInUserId());
    }

    public void deleteFiltered(String operation, String type) {

        Long userId = getLoggedInUserId();

        if (operation != null && type != null) {
            repository.deleteByOperationAndThisMeasurementTypeAndUserId(operation, type, userId);
        } else if (operation != null) {
            repository.deleteByOperationAndUserId(operation, userId);
        } else if (type != null) {
            repository.deleteByThisMeasurementTypeAndUserId(type, userId);
        } else {
            repository.deleteByUserId(userId);
        }
    }
}









//package com.example.service;
//import com.example.dto.QuantityDTO;
//
//
//import com.example.dto.QuantityInputDTO;
//import com.example.entity.QuantityMeasurementEntity;
//import com.example.repository.QuantityMeasurementRepository;
//import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//import jakarta.servlet.http.HttpServletRequest;
//@Service
//@Transactional
//public class QuantityMeasurementServiceImpl implements IQuantityMeasurementService {
//
//    @Autowired
//    private QuantityMeasurementRepository repository;
//    @Autowired
//    private RestTemplate restTemplate;
//
////    private UserRepository userRepository;
//
//    // ---------------- USER ----------------
//
//    private void sendToHistory(QuantityMeasurementEntity entity) {
//        try {
//            restTemplate.postForObject(
//                "http://localhost:8083/api/v1/history/save",
//                entity,
//                Void.class
//            );
//        } catch (Exception e) {
//            System.out.println("History service down, skipping...");
//        }
//    }
//
//    public Long getLoggedInUserId() {
//
//        HttpServletRequest request =
//            ((ServletRequestAttributes) RequestContextHolder
//                .getRequestAttributes())
//                .getRequest();
//
//        String userId = request.getHeader("X-User-Id");
//
//        if (userId == null) {
//            throw new RuntimeException("User ID not found in header");
//        }
//
//        return Long.parseLong(userId);
//    }
//
//    // ---------------- BASE ENTITY ----------------
//
//    private QuantityMeasurementEntity createBaseEntity(QuantityInputDTO input) {
//        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();
//
//        entity.setThisValue(input.getThisQuantityDTO().getValue());
//        entity.setThisUnit(input.getThisQuantityDTO().getUnit());
//        entity.setThisMeasurementType(input.getThisQuantityDTO().getMeasurementType());
//
//        entity.setThatValue(input.getThatQuantityDTO().getValue());
//        entity.setThatUnit(input.getThatQuantityDTO().getUnit());
//        entity.setThatMeasurementType(input.getThatQuantityDTO().getMeasurementType());
//        entity.setCreatedAt(LocalDateTime.now()); 
//        entity.setUserId(getLoggedInUserId()); 
//
//        return entity;
//    }
//
//    // ---------------- LENGTH ----------------
//
//
// // ---------------- LENGTH ----------------
//
//    private double toMillimeter(double value, String unit) {
//        switch (unit.toUpperCase()) {
//
//            case "MILLIMETER":
//            case "MILLIMETERS":
//                return value;
//
//            case "CENTIMETER":
//            case "CENTIMETERS":
//                return value * 10;
//
//            case "METER":
//            case "METERS":
//                return value * 1000;
//
//            case "KILOMETER":
//            case "KILOMETERS":
//                return value * 1_000_000;
//
//            case "INCH":
//            case "INCHES":
//                return value * 25.4;
//
//            case "FEET":
//            case "FOOT":
//                return value * 304.8;
//
//            case "YARD":
//            case "YARDS":
//                return value * 914.4;
//
//            default:
//                throw new RuntimeException("Invalid length unit: " + unit);
//        }
//    }
//
//    private double fromMillimeter(double value, String unit) {
//        switch (unit.toUpperCase()) {
//
//            case "MILLIMETER":
//            case "MILLIMETERS":
//                return value;
//
//            case "CENTIMETER":
//            case "CENTIMETERS":
//                return value / 10;
//
//            case "METER":
//            case "METERS":
//                return value / 1000;
//
//            case "KILOMETER":
//            case "KILOMETERS":
//                return value / 1_000_000;
//
//            case "INCH":
//            case "INCHES":
//                return value / 25.4;
//
//            case "FEET":
//            case "FOOT":
//                return value / 304.8;
//
//            case "YARD":
//            case "YARDS":
//                return value / 914.4;
//
//            default:
//                throw new RuntimeException("Invalid length unit: " + unit);
//        }
//    }
//
//    private double convertTemperature(double value, String from, String to) {
//
//        double kelvin;
//
//        switch (from.toUpperCase()) {
//            case "CELSIUS":
//                kelvin = value + 273.15;
//                break;
//
//            case "FAHRENHEIT":
//                kelvin = (value - 32) * 5 / 9 + 273.15;
//                break;
//
//            case "KELVIN":
//                kelvin = value;
//                break;
//
//            default:
//                throw new RuntimeException("Invalid temperature unit");
//        }
//
//        switch (to.toUpperCase()) {
//            case "CELSIUS":
//                return kelvin - 273.15;
//
//            case "FAHRENHEIT":
//                return (kelvin - 273.15) * 9 / 5 + 32;
//
//            case "KELVIN":
//                return kelvin;
//
//            default:
//                throw new RuntimeException("Invalid temperature unit");
//        }
//    }
//
//    // ---------------- VOLUME ----------------
//
//    private double toMilliliter(double value, String unit) {
//        switch (unit.toUpperCase()) {
//            case "LITER": 
//                return value * 1000;
//
//            case "MILLILITER": 
//                return value;
//
//            case "CUBIC_METER": 
//                return value * 1_000_000;
//
//            case "GALLON": 
//                return value * 3785.41; // ADDED
//
//            default: 
//                throw new RuntimeException("Invalid volume unit: " + unit);
//        }
//    }
//
//    private double fromMilliliter(double value, String unit) {
//        switch (unit.toUpperCase()) {
//            case "LITER": 
//                return value / 1000;
//
//            case "MILLILITER": 
//                return value;
//
//            case "CUBIC_METER": 
//                return value / 1_000_000;
//
//            case "GALLON": 
//                return value / 3785.41; //  ADDED
//
//            default: 
//                throw new RuntimeException("Invalid volume unit: " + unit);
//        }
//    }
//    
// // ---------------- WEIGHT ----------------
//
//    private double toGrams(double value, String unit) {
//        switch (unit.toUpperCase()) {
//
//            case "MILLIGRAM": return value / 1000;
//
//            case "GRAM": return value;
//
//            case "KILOGRAM": return value * 1000;
//
//            case "TON": return value * 1_000_000;
//
//            case "OUNCE": return value * 28.3495;
//
//            case "POUND": return value * 453.592;
//
//            default: throw new RuntimeException("Invalid weight unit: " + unit);
//        }
//    }
//
//    private double fromGrams(double value, String unit) {
//        switch (unit.toUpperCase()) {
//
//            case "MILLIGRAM": return value * 1000;
//
//            case "GRAM": return value;
//
//            case "KILOGRAM": return value / 1000;
//
//            case "TON": return value / 1_000_000;
//
//            case "OUNCE": return value / 28.3495;
//
//            case "POUND": return value / 453.592;
//
//            default: throw new RuntimeException("Invalid weight unit: " + unit);
//        }
//    }
//
//    // ---------------- ADD ----------------
//    
//    
//    @Override
//    public QuantityMeasurementEntity add(QuantityInputDTO input) {
//
//        QuantityMeasurementEntity entity = createBaseEntity(input);
//        String type = input.getThisQuantityDTO().getMeasurementType();
//
//        double a = input.getThisQuantityDTO().getValue();
//        double b = input.getThatQuantityDTO().getValue();
//        String unit = input.getThisQuantityDTO().getUnit();
//        String unitB = input.getThatQuantityDTO().getUnit();
//
//        double result = 0;
//
//        if ("LENGTH".equalsIgnoreCase(type)) {
//            result = toMillimeter(a, unit) + toMillimeter(b, unitB);
//            result = fromMillimeter(result, unit);
//        }
//
//        else if ("TEMPERATURE".equalsIgnoreCase(type)) {
//            // ✅ FIX: convert B to same unit, then add directly
//            double bConverted = convertTemperature(b, unitB, unit);
//            result = a + bConverted;
//        }
//
//        else if ("VOLUME".equalsIgnoreCase(type)) {
//            result = toMilliliter(a, unit) + toMilliliter(b, unitB);
//            result = fromMilliliter(result, unit);
//        }
//
//        else if ("WEIGHT".equalsIgnoreCase(type)) {
//            result = toGrams(a, unit) + toGrams(b, unitB);
//            result = fromGrams(result, unit);
//        }
//
//        entity.setResultValue(round(result));
//        entity.setResultUnit(unit);
//        entity.setOperation("ADD");
//
////        return repository.save(entity);
//        QuantityMeasurementEntity saved = repository.save(entity);
//        sendToHistory(saved);
//        return saved;
//    }
//    private double toKelvin(double value, String unit)
//    {
//    	switch (unit.toUpperCase()) {
//    	case "CELSIUS":return value + 273.15;
//    	case "FAHRENHEIT": return (value - 32) * 5 / 9 + 273.15;
//    	case "KELVIN": return value;
//    	default: throw new RuntimeException("Invalid temperature unit");
//    	}
//    	}
//    private double fromKelvin(double value, String unit) 
//    {
//    	switch (unit.toUpperCase())
//    	{
//    	case "CELSIUS": return value - 273.15;
//    	case "FAHRENHEIT": return (value - 273.15) * 9 / 5 + 32; 
//    	case "KELVIN": return value; 
//    	default: throw new RuntimeException("Invalid temperature unit"); 
//    	} 
//    	}
////
////    @Override
////    public QuantityMeasurementEntity add(QuantityInputDTO input) {
////
////        QuantityMeasurementEntity entity = createBaseEntity(input);
////        String type = input.getThisQuantityDTO().getMeasurementType();
////
////        double a = input.getThisQuantityDTO().getValue();
////        double b = input.getThatQuantityDTO().getValue();
////        String unit = input.getThisQuantityDTO().getUnit();
////
////        double result = 0;
////
////        if ("LENGTH".equalsIgnoreCase(type)) {
////            result = toMillimeter(a, unit) + toMillimeter(b, input.getThatQuantityDTO().getUnit());
////            result = fromMillimeter(result, unit);
////        }
////
////        else if ("TEMPERATURE".equalsIgnoreCase(type)) {
////            result = toKelvin(a, unit) + toKelvin(b, input.getThatQuantityDTO().getUnit());
////            result = fromKelvin(result, unit);
////        }
////
////        else if ("VOLUME".equalsIgnoreCase(type)) {
////            result = toMilliliter(a, unit) + toMilliliter(b, input.getThatQuantityDTO().getUnit());
////            result = fromMilliliter(result, unit);
////        }
////
////        else if ("WEIGHT".equalsIgnoreCase(type)) {
////            result = toGrams(a, unit) + toGrams(b, input.getThatQuantityDTO().getUnit());
////            result = fromGrams(result, unit);
////        }
////
////        entity.setResultValue(round(result));
////        entity.setResultUnit(unit);
////        entity.setOperation("ADD");
////
////        return repository.save(entity);
////    }
//
//    // ---------------- SUBTRACT ----------------
//
//    @Override
//    public QuantityMeasurementEntity subtract(QuantityInputDTO input) {
//
//        QuantityMeasurementEntity entity = createBaseEntity(input);
//
//        String type = input.getThisQuantityDTO().getMeasurementType();
//
//        double a = input.getThisQuantityDTO().getValue();
//        double b = input.getThatQuantityDTO().getValue();
//
//        String unitA = input.getThisQuantityDTO().getUnit();
//        String unitB = input.getThatQuantityDTO().getUnit();
//
//        double result = 0;
//
//        if ("LENGTH".equalsIgnoreCase(type)) {
//
//            double bConverted = fromMillimeter(
//                    toMillimeter(b, unitB),
//                    unitA
//            );
//
//            result = a - bConverted;
//        }
//
//        else if ("TEMPERATURE".equalsIgnoreCase(type)) {
//
//            // 🔥 FIX: convert B → unitA
//            double bConverted = convertTemperature(b, unitB, unitA);
//
//            result = a - bConverted;
//        }
//
//        else if ("VOLUME".equalsIgnoreCase(type)) {
//
//            double bConverted = fromMilliliter(
//                    toMilliliter(b, unitB),
//                    unitA
//            );
//
//            result = a - bConverted;
//        }
//
//        else if ("WEIGHT".equalsIgnoreCase(type)) {
//
//            double bConverted = fromGrams(
//                    toGrams(b, unitB),
//                    unitA
//            );
//
//            result = a - bConverted;
//        }
//
//        entity.setResultValue(round(result));
//        entity.setResultUnit(unitA);
//        entity.setOperation("SUBTRACT");
//
////        return repository.save(entity);
//        QuantityMeasurementEntity saved = repository.save(entity);
//        sendToHistory(saved);
//        return saved;
//    }
//
//    // ---------------- MULTIPLY ----------------
//
//    @Override
//    public QuantityMeasurementEntity multiply(QuantityInputDTO input) {
//
//        QuantityMeasurementEntity entity = createBaseEntity(input);
//
//        String type = input.getThisQuantityDTO().getMeasurementType();
//
//        double a = input.getThisQuantityDTO().getValue();
//        double b = input.getThatQuantityDTO().getValue();
//
//        String unitA = input.getThisQuantityDTO().getUnit();
//        String unitB = input.getThatQuantityDTO().getUnit();
//
//        double result = 0;
//
//        if ("LENGTH".equalsIgnoreCase(type)) {
//            double convertedB = fromMillimeter(toMillimeter(b, unitB), unitA);
//            result = a * convertedB;
//        }
//
//        else if ("TEMPERATURE".equalsIgnoreCase(type)) {
//            double convertedB = fromKelvin(toKelvin(b, unitB), unitA);
//            result = a * convertedB;
//        }
//
//        else if ("VOLUME".equalsIgnoreCase(type)) {
//            double convertedB = fromMilliliter(toMilliliter(b, unitB), unitA);
//            result = a * convertedB;
//        }
//
//        else if ("WEIGHT".equalsIgnoreCase(type)) {
//            double convertedB = fromGrams(toGrams(b, unitB), unitA);
//            result = a * convertedB;
//        }
//
//        entity.setResultValue(round(result));
//        entity.setResultUnit(unitA);
//        entity.setOperation("MULTIPLY");
//
////        return repository.save(entity);
//        QuantityMeasurementEntity saved = repository.save(entity);
//        sendToHistory(saved);
//        return saved;
//    }
//
//
//    
//    @Override
//    public QuantityMeasurementEntity divide(QuantityInputDTO input) {
//
//        QuantityMeasurementEntity entity = createBaseEntity(input);
//
//        double a = input.getThisQuantityDTO().getValue();
//        double b = input.getThatQuantityDTO().getValue();
//
//        if (b == 0) throw new ArithmeticException("Cannot divide by zero");
//
//        double result = a / b;
//
//        entity.setResultValue(round(result));
//        entity.setResultUnit("RATIO");
//        entity.setOperation("DIVIDE");
//
////        return repository.save(entity);
//        QuantityMeasurementEntity saved = repository.save(entity);
//        sendToHistory(saved);
//        return saved;
//    }
//    // ---------------- CONVERT ----------------
//    @Override
//    public QuantityMeasurementEntity convert(QuantityInputDTO input) {
//
//        //  Step 1: Validate input
//        if (input == null || input.getThisQuantityDTO() == null) {
//            throw new RuntimeException("Input or thisQuantityDTO cannot be null");
//        }
//
//        if (input.getThatQuantityDTO() == null || input.getThatQuantityDTO().getUnit() == null) {
//            throw new RuntimeException("Target unit (thatQuantityDTO.unit) is required");
//        }
//
//        //  Step 2: Extract safely
//        QuantityDTO fromDTO = input.getThisQuantityDTO();
//        QuantityDTO toDTO = input.getThatQuantityDTO();
//
//        if (fromDTO.getValue() == null) {
//            throw new RuntimeException("Source value is required for conversion");
//        }
//
//        double value = fromDTO.getValue();
//        String fromUnit = fromDTO.getUnit();
//        String toUnit = toDTO.getUnit();
//        String type = fromDTO.getMeasurementType();
//
//        double result = 0;
//
//        // Step 3: Conversion logic
//        if ("TEMPERATURE".equalsIgnoreCase(type)) {
//            result = fromKelvin(toKelvin(value, fromUnit), toUnit);
//        }
//
//        else if ("LENGTH".equalsIgnoreCase(type)) {
//            result = fromMillimeter(toMillimeter(value, fromUnit), toUnit);
//        }
//
//        else if ("VOLUME".equalsIgnoreCase(type)) {
//            result = fromMilliliter(toMilliliter(value, fromUnit), toUnit);
//        }
//
//        else if ("WEIGHT".equalsIgnoreCase(type)) {
//            result = fromGrams(toGrams(value, fromUnit), toUnit);
//        }
//
//        else {
//            throw new RuntimeException("Unsupported measurement type: " + type);
//        }
//
//        // Step 4: Create entity AFTER validation (important)
//        QuantityMeasurementEntity entity = createBaseEntity(input);
//
//        
//    
//        entity.setResultValue(round(result));
//        entity.setResultUnit(toUnit);
//        entity.setOperation("CONVERT");
//        
//
////        return repository.save(entity);
//        QuantityMeasurementEntity saved = repository.save(entity);
//        sendToHistory(saved);
//        return saved;
//    }
//
////    @Override
////    public List<QuantityMeasurementEntity> getHistoryByOperation(String operation) {
////
////        return repository.findByOperationAndUserId(
////                operation.toUpperCase(),
////                getLoggedInUserId()
////        );
////    }
//
//    // ---------------- COMPARE ----------------
//
//    @Override
//    public QuantityMeasurementEntity compare(QuantityInputDTO input) {
//
//        QuantityMeasurementEntity entity = createBaseEntity(input);
//
//        String type = input.getThisQuantityDTO().getMeasurementType();
//
//        double a = input.getThisQuantityDTO().getValue();
//        double b = input.getThatQuantityDTO().getValue();
//
//        boolean isEqual = false;
//
//        // LENGTH
//        if ("LENGTH".equalsIgnoreCase(type)) {
//            double val1 = toMillimeter(a, input.getThisQuantityDTO().getUnit());
//            double val2 = toMillimeter(b, input.getThatQuantityDTO().getUnit());
//            isEqual = Math.abs(val1 - val2) < 0.0001;
//        }
//
//        // TEMPERATURE
//        else if ("TEMPERATURE".equalsIgnoreCase(type)) {
//            double val1 = toKelvin(a, input.getThisQuantityDTO().getUnit());
//            double val2 = toKelvin(b, input.getThatQuantityDTO().getUnit());
//            isEqual = Math.abs(val1 - val2) < 0.0001;
//        }
//
//        // VOLUME
//        else if ("VOLUME".equalsIgnoreCase(type)) {
//            double val1 = toMilliliter(a, input.getThisQuantityDTO().getUnit());
//            double val2 = toMilliliter(b, input.getThatQuantityDTO().getUnit());
//            isEqual = Math.abs(val1 - val2) < 0.0001;
//        }
//
//        // WEIGHT
//        else if ("WEIGHT".equalsIgnoreCase(type)) {
//            double val1 = toGrams(a, input.getThisQuantityDTO().getUnit());
//            double val2 = toGrams(b, input.getThatQuantityDTO().getUnit());
//            isEqual = Math.abs(val1 - val2) < 0.0001;
//        }
//
//        // FINAL OUTPUT FORMAT
//        entity.setResultValue(isEqual ? 1.0 : 0.0);
//        entity.setResultUnit(isEqual ? "TRUE" : "FALSE");
//        entity.setOperation("COMPARE");
//
////        return repository.save(entity);
//        QuantityMeasurementEntity saved = repository.save(entity);
//        sendToHistory(saved);
//        return saved;
//    }
//
//    // ---------------- GET ALL ----------------
////
////    @Override
////    public List<QuantityMeasurementEntity> getAll() {
////
////        return repository.findByUserId(getLoggedInUserId());
////    }
//
//    // ---------------- GET BY ID ----------------
//
////    @Override
////    public QuantityMeasurementEntity getById(Long id) {
////
////        return repository.findById(id)
////                .filter(q -> q.getUserId().equals(getLoggedInUserId()))
////                .orElseThrow(() -> new RuntimeException("Not found or unauthorized"));
////    }
//    // ---------------- DELETE ----------------
//    @Override
//    public void delete(Long id) {
//
//        repository.forceDelete(id);
//    }
//    
//    
//    @Transactional
//    public void deleteAllByUser() {
//         // your existing logic
//        repository.deleteByUserId(getLoggedInUserId());
//    }
//    
//    
//    //rounding off to three places
//    private double round(double value) {
//        return Math.round(value * 1000.0) / 1000.0;
//    }
//    @Transactional
//    public void deleteFiltered(String operation, String type) {
//
//        Long userId = getLoggedInUserId();  // ✅ GET HERE
//
//        if (operation != null && !operation.isEmpty() &&
//            type != null && !type.isEmpty()) {
//
//            repository.deleteByOperationAndThisMeasurementTypeAndUserId(
//                    operation, type, userId);
//            return;
//        }
//
//        if (operation != null && !operation.isEmpty()) {
//            repository.deleteByOperationAndUserId(operation, userId);
//            return;
//        }
//
//        if (type != null && !type.isEmpty()) {
//            repository.deleteByThisMeasurementTypeAndUserId(type, userId);
//            return;
//        }
//
//        repository.deleteByUserId(userId);
//    }
//
////    @Override
////    public List<QuantityMeasurementEntity> getFiltered(String operation, String type) {
////
////        Long userId = getCurrentUserId();
////
////        // ✅ No filters → return all
////        if ((operation == null || operation.isEmpty()) &&
////            (type == null || type.isEmpty())) {
////
////            return repository.findByUserId(userId);
////        }
////
////        // ✅ Only operation
////        if (operation != null && !operation.isEmpty() &&
////            (type == null || type.isEmpty())) {
////
////            return repository.findByOperationAndUserId(operation, userId);
////        }
////
////        // ✅ Only type
////        if ((operation == null || operation.isEmpty()) &&
////            type != null && !type.isEmpty()) {
////
////            return repository.findByThisMeasurementTypeAndUserId(type, userId);
////        }
////
////        // ✅ Both filters
////        return repository.findByOperationAndThisMeasurementTypeAndUserId(
////                operation, type, userId);
////    }
//
//private Long getCurrentUserId() {
//    return getLoggedInUserId(); // ✅ FIXED
//}
//
//
//
//}
//
