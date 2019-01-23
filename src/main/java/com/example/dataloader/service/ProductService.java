package com.example.dataloader.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.dataloader.entity.Product;
import com.example.dataloader.repository.ProductRepository;

@Service
public class ProductService implements IProductService {

	private Logger log = LoggerFactory.getLogger(ProductService.class);
	
	@Autowired
	private ProductRepository productRepository;
	
	@PersistenceContext
	private EntityManager entityManager ;
	
	@Override
	@Transactional(readOnly=true)
	public List<Product> getAllProducts() {
		List<Product> productList = new ArrayList<>();
		productRepository.findAll().forEach(p->productList.add(p));;
		return productList;
	}

	@Override
	@Transactional(readOnly=true)
	public Product getProductById(long productId) {
		return productRepository.findById(productId).get();
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public boolean addProduct(Product product) {
		productRepository.save(product);
		return true;
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void updateProduct(Product product) {
		productRepository.save(product);

	}

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void deleteProduct(long productId) {
		productRepository.deleteById(productId);

	}

	@Override
	@Transactional(readOnly=true)
	public void generateProductDetailFile() {
		Queue<Product> productList = new ArrayBlockingQueue<>(100000);
		FileGeneratorThread fileGenerator = new FileGeneratorThread(productList, "allclient");
		new Thread(fileGenerator).start();
			try(Stream<Product> productStream = productRepository.streamAllProductByQuery()){
				synchronized (productList) {
					productStream.forEach(pr-> {
						if(productList.size()==100000) {
							try {
								notify();
								productList.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						productList.offer(pr);
						entityManager.detach(pr);
					}
				);
			}
		}
		fileGenerator.setIsDone(true);
	}
}

class FileGeneratorThread implements Runnable{
	private Queue<Product> queue ;
	private Path filePath ; 
	private volatile Boolean isDone =  false;
	private Logger log = LoggerFactory.getLogger(ProductService.class);
	public Boolean getIsDone() {
		return isDone;
	}
	public void setIsDone(Boolean isDone) {
		this.isDone = isDone;
	}


	public FileGeneratorThread(Queue<Product> queue , String fileName) {
		this.queue = queue;
		this.filePath = Paths.get("G:\\A-Workspace\\dataFile\\"+fileName+"_"+System.currentTimeMillis()+".txt");
	}
	
	
	@Override
	public void run() {
		List<CharSequence> charSequenceList = null;
		while(!isDone) {
			synchronized (queue) {
				charSequenceList = queue.stream().map(t -> t.toString()).collect(Collectors.toList());
				queue.clear();
				notify();
			}
			if(charSequenceList!=null && filePath!=null) {
				writeInFile(charSequenceList, filePath);
			}
		}
	}
	
	private void writeInFile(List<CharSequence> charSequenceList , Path path) {
		try {
			Files.write(path, charSequenceList, Charset.forName("UTF-8"), StandardOpenOption.WRITE,StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			log.info("Total lines of product write in file [{}]",charSequenceList.size());
		} catch (IOException e) {
			log.error("Exception occured during file writing."+ e.getMessage(),e);
		}
	}
}
