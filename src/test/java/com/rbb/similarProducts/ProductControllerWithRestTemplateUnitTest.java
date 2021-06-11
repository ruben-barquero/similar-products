package com.rbb.similarProducts;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbb.similarproducts.controllers.ProductController;
import com.rbb.similarproducts.mappers.ProductDetailMapper;
import com.rbb.similarproducts.models.ProductDetailEntity;
import com.rbb.similarproducts.openapi.model.ProductDetail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestConfig.class)
@TestPropertySource(properties = "serviceType=REST_TEMPLATE")
class ProductControllerWithRestTemplateUnitTest {

	@Autowired
	private ProductController productController;
	@Autowired
	private RestTemplate restTemplate;

	private MockRestServiceServer mockRestServiceServer;
	private ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	public void init() {
		this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
	}

	@Test
	void getProductSimilarOk() throws JsonProcessingException, URISyntaxException {
		final List<Integer> similarProductIds = List.of(Integer.valueOf(2), Integer.valueOf(3));
		this.mockRestServiceServer
				.expect(ExpectedCount.once(),
						MockRestRequestMatchers.requestTo(new URI("http://localhost:3001/product/1/similarids")))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET)).andRespond(
						MockRestResponseCreators.withSuccess(this.objectMapper.writeValueAsString(similarProductIds),
								MediaType.APPLICATION_JSON));

		final ProductDetailEntity productDetailEntity2 = new ProductDetailEntity();
		productDetailEntity2.setId("2");
		productDetailEntity2.setName("product2");
		productDetailEntity2.setPrice(BigDecimal.valueOf(2));
		productDetailEntity2.setAvailability(Boolean.TRUE);
		this.mockRestServiceServer
				.expect(ExpectedCount.once(),
						MockRestRequestMatchers.requestTo(new URI("http://localhost:3001/product/2")))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET)).andRespond(
						MockRestResponseCreators.withSuccess(this.objectMapper.writeValueAsString(productDetailEntity2),
								MediaType.APPLICATION_JSON));

		final ProductDetailEntity productDetailEntity3 = new ProductDetailEntity();
		productDetailEntity3.setId("3");
		productDetailEntity3.setName("product3");
		productDetailEntity3.setPrice(BigDecimal.valueOf(3));
		productDetailEntity3.setAvailability(Boolean.TRUE);
		this.mockRestServiceServer
				.expect(ExpectedCount.once(),
						MockRestRequestMatchers.requestTo(new URI("http://localhost:3001/product/3")))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET)).andRespond(
						MockRestResponseCreators.withSuccess(this.objectMapper.writeValueAsString(productDetailEntity3),
								MediaType.APPLICATION_JSON));

		final ResponseEntity<Set<ProductDetail>> productSimilar = this.productController.getProductSimilar("1");

		this.mockRestServiceServer.verify();

		final Set<ProductDetail> expectedResult = new LinkedHashSet<ProductDetail>(
				List.of(ProductDetailMapper.INSTANCE.productDetailEntityToProductDetail(productDetailEntity2),
						ProductDetailMapper.INSTANCE.productDetailEntityToProductDetail(productDetailEntity3)));

		Assert.assertEquals(expectedResult, productSimilar.getBody());
	}

	@Test
	void getProductSimilarNotFoundSimilarProductIds() throws JsonProcessingException, URISyntaxException {
		this.mockRestServiceServer
				.expect(ExpectedCount.once(),
						MockRestRequestMatchers.requestTo(new URI("http://localhost:3001/product/1/similarids")))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND));

		final ResponseStatusException responseStatusException = Assert.assertThrows(ResponseStatusException.class,
				() -> this.productController.getProductSimilar("1"));

		this.mockRestServiceServer.verify();

		Assert.assertEquals(HttpStatus.NOT_FOUND, responseStatusException.getStatus());
		Assert.assertEquals("No found similar products for the product 1", responseStatusException.getReason());
	}

	@Test
	void getProductSimilarNotFoundProductDetail() throws JsonProcessingException, URISyntaxException {
		final List<Integer> similarProductIds = List.of(Integer.valueOf(2));
		this.mockRestServiceServer
				.expect(ExpectedCount.once(),
						MockRestRequestMatchers.requestTo(new URI("http://localhost:3001/product/1/similarids")))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET)).andRespond(
						MockRestResponseCreators.withSuccess(this.objectMapper.writeValueAsString(similarProductIds),
								MediaType.APPLICATION_JSON));

		this.mockRestServiceServer
				.expect(ExpectedCount.once(),
						MockRestRequestMatchers.requestTo(new URI("http://localhost:3001/product/2")))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND));

		final ResponseStatusException responseStatusException = Assert.assertThrows(ResponseStatusException.class,
				() -> this.productController.getProductSimilar("1"));

		this.mockRestServiceServer.verify();

		Assert.assertEquals(HttpStatus.NOT_FOUND, responseStatusException.getStatus());
		Assert.assertEquals("Not found detail for the product 2", responseStatusException.getReason());
	}
	
	@Test
	void getProductSimilarErrorInSimilarProductIds() throws JsonProcessingException, URISyntaxException {
		this.mockRestServiceServer
				.expect(ExpectedCount.once(),
						MockRestRequestMatchers.requestTo(new URI("http://localhost:3001/product/1/similarids")))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withServerError());

		final ResponseStatusException responseStatusException = Assert.assertThrows(ResponseStatusException.class,
				() -> this.productController.getProductSimilar("1"));

		this.mockRestServiceServer.verify();

		Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseStatusException.getStatus());
		Assert.assertEquals("An error occurred while retrieving similar products for the product 1. 500 Internal Server Error: [no body]", responseStatusException.getReason());
	}

	@Test
	void getProductSimilarErrorInProductDetail() throws JsonProcessingException, URISyntaxException {
		final List<Integer> similarProductIds = List.of(Integer.valueOf(2));
		this.mockRestServiceServer
				.expect(ExpectedCount.once(),
						MockRestRequestMatchers.requestTo(new URI("http://localhost:3001/product/1/similarids")))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET)).andRespond(
						MockRestResponseCreators.withSuccess(this.objectMapper.writeValueAsString(similarProductIds),
								MediaType.APPLICATION_JSON));

		this.mockRestServiceServer
				.expect(ExpectedCount.once(),
						MockRestRequestMatchers.requestTo(new URI("http://localhost:3001/product/2")))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withServerError());

		final ResponseStatusException responseStatusException = Assert.assertThrows(ResponseStatusException.class,
				() -> this.productController.getProductSimilar("1"));

		this.mockRestServiceServer.verify();

		Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseStatusException.getStatus());
		Assert.assertEquals("An error occurred while retrieving detail for the product 2. 500 Internal Server Error: [no body]", responseStatusException.getReason());
	}

}