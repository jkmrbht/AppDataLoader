package com.example.dataloader.service;

import java.util.List;

import com.example.dataloader.entity.Product;

public interface IProductService {
	 List<Product> getAllProducts();
	 Product getProductById(long productId);
     boolean addProduct(Product product);
     void updateProduct(Product product);
     void deleteProduct(long productId);
     public void generateProductDetailFile();
     
}
