package com.rbb.similarproducts.controllers;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.rbb.similarproducts.exceptions.NotFoundException;
import com.rbb.similarproducts.exceptions.ServiceException;
import com.rbb.similarproducts.openapi.api.ProductApi;
import com.rbb.similarproducts.openapi.model.ProductDetail;
import com.rbb.similarproducts.services.ProductService;

@RestController
public class ProductController implements ProductApi {

	@Autowired
	private ProductService productService;
	
	@Override
	public ResponseEntity<Set<ProductDetail>> getProductSimilar(final String productId) {
		try {
			return ResponseEntity.ok(this.productService.getProductSimilar(productId));
		} catch (final NotFoundException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
		} catch (final ServiceException ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
		}
	}

}
