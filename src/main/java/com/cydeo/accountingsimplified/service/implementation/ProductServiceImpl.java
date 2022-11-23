package com.cydeo.accountingsimplified.service.implementation;

import com.cydeo.accountingsimplified.dto.ProductDto;
import com.cydeo.accountingsimplified.entity.Category;
import com.cydeo.accountingsimplified.entity.Company;
import com.cydeo.accountingsimplified.entity.Product;
import com.cydeo.accountingsimplified.mapper.MapperUtil;
import com.cydeo.accountingsimplified.repository.ProductRepository;
import com.cydeo.accountingsimplified.service.ProductService;
import com.cydeo.accountingsimplified.service.SecurityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final SecurityService securityService;
    private final MapperUtil mapperUtil;

    public ProductServiceImpl(ProductRepository productRepository,
                              SecurityService securityService,
                              MapperUtil mapperUtil) {
        this.productRepository = productRepository;
        this.securityService = securityService;
        this.mapperUtil = mapperUtil;
    }

    @Override
    public ProductDto findProductById(Long productId) {
        Product product = productRepository.findById(productId).get();
        return mapperUtil.convert(product, new ProductDto());
    }

    @Override
    public List<ProductDto> getAllProducts() throws Exception {
        Company company = mapperUtil.convert(securityService.getLoggedInUser().getCompany(), new Company());
        return productRepository.findAllByCategoryCompany(company)
                .stream()
                .map(each -> mapperUtil.convert(each, new ProductDto()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getProductsOfCompany() {
        Company company = mapperUtil.convert(securityService.getLoggedInUser().getCompany(), new Company());
        return productRepository.findAllByCategoryCompany(company)
                .stream()
                .map(each -> mapperUtil.convert(each, new ProductDto()))
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto save(ProductDto productDto) {
        Product product = mapperUtil.convert(productDto, new Product());
        product.setQuantityInStock(0);
        return mapperUtil.convert(productRepository.save(product), new ProductDto());
    }

    @Override
    public ProductDto update(Long productId, ProductDto productDto) {
        Product product = productRepository.findById(productId).get();
        Category category = mapperUtil.convert(productDto.getCategory(), new Category());
        product.setCategory(category);
        product.setName(productDto.getName());
        product.setLowLimitAlert(productDto.getLowLimitAlert());
        product.setProductUnit(productDto.getProductUnit());
        final int quantityInStock = productDto.getQuantityInStock() == null ? product.getQuantityInStock() : productDto.getQuantityInStock();
        product.setQuantityInStock(quantityInStock);
        return mapperUtil.convert(productRepository.save(product), productDto);
    }

    @Override
    public void delete(Long productId) {
        Product product = productRepository.findById(productId).get();
        product.setIsDeleted(true);
        productRepository.save(product);
    }

    @Override
    public List<ProductDto> findAllProductsWithCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(product -> mapperUtil.convert(product, new ProductDto()))
                .collect(Collectors.toList());

    }

    @Override
    public boolean isProductNameExist(ProductDto productDto) {
        Company actualCompany = mapperUtil.convert(securityService.getLoggedInUser().getCompany(), new Company());
        Product existingProduct = productRepository.findByNameAndCategoryCompany(productDto.getName(), actualCompany);
        if (existingProduct == null) return false;
        return !existingProduct.getId().equals(productDto.getId());
    }


}
