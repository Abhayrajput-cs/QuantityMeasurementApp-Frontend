package com.example.dto;

public class HistoryRequestDTO {

    private Long userId;

    private Double thisValue;
    private String thisUnit;
    private String thisMeasurementType;

    private Double thatValue;
    private String thatUnit;
    private String thatMeasurementType;

    private String operation;

    private Double resultValue;
    private String resultUnit;
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Double getThisValue() {
		return thisValue;
	}
	public void setThisValue(Double thisValue) {
		this.thisValue = thisValue;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getResultUnit() {
		return resultUnit;
	}
	public void setResultUnit(String resultUnit) {
		this.resultUnit = resultUnit;
	}
	public Double getResultValue() {
		return resultValue;
	}
	public void setResultValue(Double resultValue) {
		this.resultValue = resultValue;
	}
	public String getThatUnit() {
		return thatUnit;
	}
	public void setThatUnit(String thatUnit) {
		this.thatUnit = thatUnit;
	}
	public String getThisUnit() {
		return thisUnit;
	}
	public void setThisUnit(String thisUnit) {
		this.thisUnit = thisUnit;
	}
	public String getThisMeasurementType() {
		return thisMeasurementType;
	}
	public void setThisMeasurementType(String thisMeasurementType) {
		this.thisMeasurementType = thisMeasurementType;
	}
	public String getThatMeasurementType() {
		return thatMeasurementType;
	}
	public void setThatMeasurementType(String thatMeasurementType) {
		this.thatMeasurementType = thatMeasurementType;
	}
	public Double getThatValue() {
		return thatValue;
	}
	public void setThatValue(Double thatValue) {
		this.thatValue = thatValue;
	}

    // getters and setters
}