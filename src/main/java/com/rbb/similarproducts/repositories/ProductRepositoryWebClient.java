package com.rbb.similarproducts.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;

import com.rbb.similarproducts.exceptions.NotFoundException;
import com.rbb.similarproducts.exceptions.ServiceException;
import com.rbb.similarproducts.models.ProductDetailEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ProductRepositoryWebClient {

	@Value("${existingApis.similarProducts.path}")
	private String similarProductsApiPath;
	@Value("${existingApis.similarProducts.message.notFound}")
	private String similarProductsApiNotFoundMsg;
	@Value("${existingApis.similarProducts.message.error}")
	private String similarProductsApiErrorMsg;

	@Value("${existingApis.productDetail.path}")
	private String productDetailApiPath;
	@Value("${existingApis.productDetail.message.notFound}")
	private String productDetailApiNotFoundMsg;
	@Value("${existingApis.productDetail.message.error}")
	private String productDetailApiErrorMsg;

	@Autowired
	private WebClient webClient;

	public Flux<Integer> getSimilarProductIds(final String productId) {
		return this.webClient.get().uri(this.similarProductsApiPath, productId).retrieve().bodyToFlux(Integer.class)
				.onErrorMap(NotFound.class,
						ex -> new NotFoundException(String.format(this.similarProductsApiNotFoundMsg, productId), ex))
				.onErrorMap(WebClientResponseException.class, ex -> new ServiceException(
						String.format(this.similarProductsApiErrorMsg, productId, ex.getMessage()), ex));
	}

	public Mono<ProductDetailEntity> getProductDetail(final Integer productId) {
		return this.webClient.get().uri(this.productDetailApiPath, productId).retrieve()
				.bodyToMono(ProductDetailEntity.class)
				.onErrorMap(NotFound.class,
						ex -> new NotFoundException(String.format(this.productDetailApiNotFoundMsg, productId), ex))
				.onErrorMap(WebClientResponseException.class, ex -> new ServiceException(
						String.format(this.productDetailApiErrorMsg, productId, ex.getMessage()), ex));
	}

}
