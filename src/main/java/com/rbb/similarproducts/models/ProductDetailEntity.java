package com.rbb.similarproducts.models;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductDetailEntity {

	private String id;

	private String name;

	private BigDecimal price;

	private Boolean availability;

}
