package com.santosh.productservice.service;

import com.santosh.productservice.Repository.ProductRepository;
import com.santosh.productservice.dto.request.ProductRequest;
import com.santosh.productservice.dto.request.response.ProductResponse;
import com.santosh.productservice.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    private final ModelMapper modelMapper;

    public void createProduct(ProductRequest productDto) {
        Product product = Product.builder()
                .name(productDto.getName())
                .description(productDto.getDescription())
                .price(productDto.getPrice()).build();
        Product savedProduct = productRepository.insert(product);
        log.info("product {} is saved", savedProduct.getId());
    }

    public List<ProductResponse> getAllProduct() {
        List<Product> allProducts = productRepository.findAll();

        List<ProductResponse> allProductMap = allProducts.stream()
                .map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
        return allProductMap;
    }
}
