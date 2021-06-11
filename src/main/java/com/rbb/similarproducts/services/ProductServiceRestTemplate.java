package com.rbb.similarproducts.services;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.rbb.similarproducts.mappers.ProductDetailMapper;
import com.rbb.similarproducts.openapi.model.ProductDetail;
import com.rbb.similarproducts.repositories.ProductRepositoryRestTemplate;

@Service
@ConditionalOnProperty(name = "serviceType", havingValue = "REST_TEMPLATE")
public class ProductServiceRestTemplate implements ProductService {
	
	@Autowired
	private ProductRepositoryRestTemplate productRepositoryRestTemplate;
	
	public Set<ProductDetail> getProductSimilar(final String productId) {
		return this.productRepositoryRestTemplate.getSimilarProductIds(productId).parallelStream()
				.map(this.productRepositoryRestTemplate::getProductDetail)
				.map(ProductDetailMapper.INSTANCE::productDetailEntityToProductDetail)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

}
