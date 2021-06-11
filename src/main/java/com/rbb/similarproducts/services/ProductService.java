package com.rbb.similarproducts.services;

import java.util.Set;

import com.rbb.similarproducts.openapi.model.ProductDetail;

public interface ProductService {

	Set<ProductDetail> getProductSimilar(String productId);
	
}
