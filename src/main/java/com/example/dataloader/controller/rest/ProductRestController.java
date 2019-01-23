package com.example.dataloader.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.dataloader.service.IProductService;

@RestController
@RequestMapping("product")
public class ProductRestController {
	
	@Autowired
	private IProductService productService ;
	
	@RequestMapping(value="productDetails", method=RequestMethod.GET)
	public String generateProductDetailFile() {
		productService.generateProductDetailFile();
		return "Generated file";
	}

}
