package com.rbb.similarProducts;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbb.similarproducts.controllers.ProductController;
import com.rbb.similarproducts.mappers.ProductDetailMapper;
import com.rbb.similarproducts.models.ProductDetailEntity;
import com.rbb.similarproducts.openapi.model.ProductDetail;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestConfig.class)
@TestPropertySource(properties = "serviceType=WEB_CLIENT")
class ProductControllerWithWebClientUnitTest {

	@Autowired
	private ProductController productController;

	public static MockWebServer mockBackEnd;

	private ObjectMapper objectMapper = new ObjectMapper();

	@DynamicPropertySource
	static void properties(final DynamicPropertyRegistry dynamicPropertyRegistry) throws IOException {
		dynamicPropertyRegistry.add("existingApis.server.url",
				() -> String.format("http://localhost:%s", mockBackEnd.getPort()));
	}

	@BeforeAll
	static void setUp() throws IOException {
		mockBackEnd = new MockWebServer();
		mockBackEnd.start();
	}

	@AfterAll
	static void tearDown() throws IOException {
		mockBackEnd.shutdown();
	}

	@Test
	void getProductSimilarOk() throws JsonProcessingException, URISyntaxException {
		final List<Integer> similarProductIds = List.of(Integer.valueOf(2), Integer.valueOf(3));

		final ProductDetailEntity productDetailEntity2 = new ProductDetailEntity();
		productDetailEntity2.setId("2");
		productDetailEntity2.setName("product2");
		productDetailEntity2.setPrice(BigDecimal.valueOf(2));
		productDetailEntity2.setAvailability(Boolean.TRUE);

		final ProductDetailEntity productDetailEntity3 = new ProductDetailEntity();
		productDetailEntity3.setId("3");
		productDetailEntity3.setName("product3");
		productDetailEntity3.setPrice(BigDecimal.valueOf(3));
		productDetailEntity3.setAvailability(Boolean.TRUE);

		mockBackEnd.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(final RecordedRequest recordedRequest) throws InterruptedException {
				try {
					final MockResponse mockResponse = new MockResponse();
					if (recordedRequest.getPath().endsWith("/product/1/similarids")) {
						mockResponse.setBody(objectMapper.writeValueAsString(similarProductIds))
								.addHeader("Content-Type", "application/json");
					} else if (recordedRequest.getPath().endsWith("/product/2")) {
						mockResponse.setBody(objectMapper.writeValueAsString(productDetailEntity2))
								.addHeader("Content-Type", "application/json");
					} else if (recordedRequest.getPath().endsWith("/product/3")) {
						mockResponse.setBody(objectMapper.writeValueAsString(productDetailEntity3))
								.addHeader("Content-Type", "application/json");
					} else {
						mockResponse.setResponseCode(404);
					}
					return mockResponse;
				} catch (JsonProcessingException e) {
					throw new InterruptedException(e.getMessage());
				}
			}
		});

		final ResponseEntity<Set<ProductDetail>> productSimilar = this.productController.getProductSimilar("1");

		final Set<ProductDetail> expectedResult = new LinkedHashSet<ProductDetail>(
				List.of(ProductDetailMapper.INSTANCE.productDetailEntityToProductDetail(productDetailEntity2),
						ProductDetailMapper.INSTANCE.productDetailEntityToProductDetail(productDetailEntity3)));

		Assert.assertEquals(expectedResult, productSimilar.getBody());
	}

	@Test
	void getProductSimilarNotFoundSimilarProductIds() throws JsonProcessingException, URISyntaxException {
		mockBackEnd.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(final RecordedRequest recordedRequest) throws InterruptedException {
				return new MockResponse().setResponseCode(404);
			}
		});
		
		final ResponseStatusException responseStatusException = Assert.assertThrows(ResponseStatusException.class,
				() -> this.productController.getProductSimilar("1"));
		
		Assert.assertEquals(HttpStatus.NOT_FOUND, responseStatusException.getStatus());
		Assert.assertEquals("No found similar products for the product 1", responseStatusException.getReason());
	}

	@Test
	void getProductSimilarNotFoundProductDetail() throws JsonProcessingException, URISyntaxException {
		final List<Integer> similarProductIds = List.of(Integer.valueOf(2));
		
		mockBackEnd.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(final RecordedRequest recordedRequest) throws InterruptedException {
				try {
					final MockResponse mockResponse = new MockResponse();
					if (recordedRequest.getPath().endsWith("/product/1/similarids")) {
						mockResponse.setBody(objectMapper.writeValueAsString(similarProductIds))
								.addHeader("Content-Type", "application/json");
					} else {
						mockResponse.setResponseCode(404);
					}
					return mockResponse;
				} catch (JsonProcessingException e) {
					throw new InterruptedException(e.getMessage());
				}
			}
		});
		
		final ResponseStatusException responseStatusException = Assert.assertThrows(ResponseStatusException.class,
				() -> this.productController.getProductSimilar("1"));

		Assert.assertEquals(HttpStatus.NOT_FOUND, responseStatusException.getStatus());
		Assert.assertEquals("Not found detail for the product 2", responseStatusException.getReason());
	}

	@Test
	void getProductSimilarErrorInSimilarProductIds() throws JsonProcessingException, URISyntaxException {
		mockBackEnd.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(final RecordedRequest recordedRequest) throws InterruptedException {
				return new MockResponse().setResponseCode(500);
			}
		});

		final ResponseStatusException responseStatusException = Assert.assertThrows(ResponseStatusException.class,
				() -> this.productController.getProductSimilar("1"));
		
		Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseStatusException.getStatus());
		Assert.assertTrue(StringUtils.startsWith(responseStatusException.getReason(), "An error occurred while retrieving similar products for the product 1."));
	}

	@Test
	void getProductSimilarErrorInProductDetail() throws JsonProcessingException, URISyntaxException {
		final List<Integer> similarProductIds = List.of(Integer.valueOf(2));
		
		mockBackEnd.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(final RecordedRequest recordedRequest) throws InterruptedException {
				try {
					final MockResponse mockResponse = new MockResponse();
					if (recordedRequest.getPath().endsWith("/product/1/similarids")) {
						mockResponse.setBody(objectMapper.writeValueAsString(similarProductIds))
								.addHeader("Content-Type", "application/json");
					} else {
						mockResponse.setResponseCode(500);
					}
					return mockResponse;
				} catch (JsonProcessingException e) {
					throw new InterruptedException(e.getMessage());
				}
			}
		});

		final ResponseStatusException responseStatusException = Assert.assertThrows(ResponseStatusException.class,
				() -> this.productController.getProductSimilar("1"));
		
		Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseStatusException.getStatus());
		Assert.assertTrue(StringUtils.startsWith(responseStatusException.getReason(), "An error occurred while retrieving detail for the product 2."));
	}

}