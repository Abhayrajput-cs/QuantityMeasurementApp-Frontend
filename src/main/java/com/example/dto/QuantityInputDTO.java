package com.example.dto;

import lombok.Data;

@Data
public class QuantityInputDTO {

    private QuantityDTO thisQuantityDTO;
    private QuantityDTO thatQuantityDTO;
	public QuantityDTO getThisQuantityDTO() {
		return thisQuantityDTO;
	}
	public void setThisQuantityDTO(QuantityDTO thisQuantityDTO) {
		this.thisQuantityDTO = thisQuantityDTO;
	}
	public QuantityDTO getThatQuantityDTO() {
		return thatQuantityDTO;
	}
	public void setThatQuantityDTO(QuantityDTO thatQuantityDTO) {
		this.thatQuantityDTO = thatQuantityDTO;
	}
}