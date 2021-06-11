package com.rbb.similarproducts.services;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.rbb.similarproducts.mappers.ProductDetailMapper;
import com.rbb.similarproducts.openapi.model.ProductDetail;
import com.rbb.similarproducts.repositories.ProductRepositoryWebClient;

import reactor.core.scheduler.Schedulers;

@Service
@ConditionalOnProperty(name = "serviceType", havingValue = "WEB_CLIENT")
public class ProductServiceWebClient implements ProductService {

	@Autowired
	private ProductRepositoryWebClient productRepositoryWebClient;

	public Set<ProductDetail> getProductSimilar(final String productId) {
		return this.productRepositoryWebClient.getSimilarProductIds(productId).parallel().runOn(Schedulers.parallel())
				.flatMap(this.productRepositoryWebClient::getProductDetail)
				.map(ProductDetailMapper.INSTANCE::productDetailEntityToProductDetail).sequential()
				.collect(Collectors.toCollection(LinkedHashSet::new)).block();
	}

}
