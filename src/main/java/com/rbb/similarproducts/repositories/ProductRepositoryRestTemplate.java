package com.rbb.similarproducts.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.rbb.similarproducts.exceptions.NotFoundException;
import com.rbb.similarproducts.exceptions.ServiceException;
import com.rbb.similarproducts.models.ProductDetailEntity;

@Repository
public class ProductRepositoryRestTemplate {

	@Value("${existingApis.server.url}${existingApis.similarProducts.path}")
	private String similarProductsApiUrl;
	@Value("${existingApis.similarProducts.message.notFound}")
	private String similarProductsApiNotFoundMsg;
	@Value("${existingApis.similarProducts.message.error}")
	private String similarProductsApiErrorMsg;

	@Value("${existingApis.server.url}${existingApis.productDetail.path}")
	private String productDetailApiUrl;
	@Value("${existingApis.productDetail.message.notFound}")
	private String productDetailApiNotFoundMsg;
	@Value("${existingApis.productDetail.message.error}")
	private String productDetailApiErrorMsg;

	@Autowired
	private RestTemplate restTemplate;

	public List<Integer> getSimilarProductIds(final String productId) {
		try {
			return this.restTemplate.exchange(this.similarProductsApiUrl, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<Integer>>() {
					}, productId).getBody();
		} catch (final NotFound ex) {
			throw new NotFoundException(String.format(this.similarProductsApiNotFoundMsg, productId), ex);
		} catch (final RestClientException ex) {
			throw new ServiceException(String.format(this.similarProductsApiErrorMsg, productId, ex.getMessage()), ex);
		}
	}

	public ProductDetailEntity getProductDetail(final Integer productId) {
		try {
			return this.restTemplate.getForObject(this.productDetailApiUrl, ProductDetailEntity.class, productId);
		} catch (final NotFound ex) {
			throw new NotFoundException(String.format(this.productDetailApiNotFoundMsg, productId), ex);
		} catch (final RestClientException ex) {
			throw new ServiceException(String.format(this.productDetailApiErrorMsg, productId, ex.getMessage()), ex);
		}
	}

}
