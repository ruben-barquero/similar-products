package com.rbb.similarproducts.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.rbb.similarproducts.models.ProductDetailEntity;
import com.rbb.similarproducts.openapi.model.ProductDetail;

@Mapper
public interface ProductDetailMapper {

	final ProductDetailMapper INSTANCE = Mappers.getMapper(ProductDetailMapper.class);
	 
    ProductDetail productDetailEntityToProductDetail(ProductDetailEntity productDetailEntity);
	
}
