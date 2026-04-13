package com.example.controller;

import com.example.dto.*;


import com.example.entity.QuantityMeasurementEntity;
import com.example.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/quantities")

public class QuantityMeasurementController {

    @Autowired
    private IQuantityMeasurementService service;

    @PostMapping("/compare")
    public QuantityMeasurementEntity compare(@RequestBody QuantityInputDTO input) {
        return service.compare(input);
    }

    @PostMapping("/convert")
    public QuantityMeasurementEntity convert(@RequestBody QuantityInputDTO input) {
        return service.convert(input);
    }

    @PostMapping("/add")
    public QuantityMeasurementEntity add(@RequestBody QuantityInputDTO input) {
        return service.add(input);
    }
    @PostMapping("/divide")
    public QuantityMeasurementEntity divide(@RequestBody QuantityInputDTO input) {
        return service.divide(input);
    }
    @PostMapping("/multiply")
    public QuantityMeasurementEntity multiply(@RequestBody QuantityInputDTO input) {
        return service.multiply(input);
    }
    @PostMapping("/subtract")
    public QuantityMeasurementEntity subtract(@RequestBody QuantityInputDTO input) {
        return service.subtract(input);
    }

//    @GetMapping("/history/operation/{operation}")
//    public List<QuantityMeasurementEntity> getHistory(@PathVariable String operation) {
//        return service.getHistoryByOperation(operation);
//    }
    
   


        // CREATE (already exists via operations)

//    @GetMapping("/all")
//    public List<QuantityMeasurementEntity> getAll(
//            @RequestParam(required = false) String operation,
//            @RequestParam(required = false) String type) {
//
//        return service.getFiltered(operation, type);
//    }

//        @GetMapping("/{id}")
//        public QuantityMeasurementEntity getById(@PathVariable Long id) {
//            return service.getById(id);
//        }

        @DeleteMapping("/{id}")
        public ResponseEntity<?> delete(@PathVariable Long id) {

            System.out.println("CONTROLLER DELETE HIT ID:" + id);

            service.delete(id);

            return ResponseEntity.ok(Map.of("message", "deleted"));
        }
        @DeleteMapping("/delete-all")
        public ResponseEntity<?> deleteAll() {
            service.deleteAllByUser();

            return ResponseEntity.ok().body(
                Map.of("message", "All history deleted successfully")
            );
        }
        @DeleteMapping("/delete-filtered")
        public ResponseEntity<?> deleteFiltered(
                @RequestParam(required = false) String operation,
                @RequestParam(required = false) String type) {

            service.deleteFiltered(operation, type); 

            return ResponseEntity.ok(Map.of("message", "Filtered records deleted successfully")
            );
        }

    }
