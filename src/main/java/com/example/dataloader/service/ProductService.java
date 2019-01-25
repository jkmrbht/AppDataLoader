package com.example.dataloader.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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
	
	private ProductRepository productRepository2;
	
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
		log.info("Main thread - start execution.");
		Map<Integer,Queue<Product>> productMap = new ConcurrentHashMap<>();
		int MAX_SIZE = 100000;
		Queue<Product> productList = new ArrayBlockingQueue<>(MAX_SIZE);
		FileGeneratorThread fileGenerator = new FileGeneratorThread(productMap, "allclient");
		new Thread(fileGenerator).start();
		log.info("Main thread - Created and started new thread.");
			try(Stream<Product> productStream = productRepository.streamAllProductByQuery()){
				log.info("Main thread - Stream result received. Process will start.");
				//synchronized (productList) {
					log.info("Main thread - Inside synchronized block. Before for each loop.");
					productStream.forEach(pr-> {
						productList.offer(pr);
						entityManager.detach(pr);
						if(productList.size()==MAX_SIZE) {
							Queue<Product> newQueue = new ArrayBlockingQueue<>(MAX_SIZE);
							newQueue.addAll(productList);
							productMap.put(productMap.size()+1, newQueue);
							log.info("Main thread - Collected product count :"+MAX_SIZE +" Current product map size - "+ productMap.size());
							productList.clear();
						}
						//log.info("Main thread - post adding to queue and detaching same record from entity manager One record to the queue.");
					}
				);
			//}
			log.info("Main thread - Outsize synechronized block.");	
		}
		log.info("Main thread - Execution over. Notify main thread to complete his task, finish.");	
		fileGenerator.setIsDone(true);
		log.info("Main thread - execution complete execution.");
	}
	
}

class FileGeneratorThread implements Runnable{
	Map<Integer,Queue<Product>> productMap;
	private Path filePath ; 
	private volatile Boolean isDone =  false;
	private Logger log = LoggerFactory.getLogger(ProductService.class);
	public Boolean getIsDone() {
		return isDone;
	}
	public void setIsDone(Boolean isDone) {
		this.isDone = isDone;
	}


	public FileGeneratorThread(Map<Integer,Queue<Product>> productMap , String fileName) {
		this.productMap = productMap;
		this.filePath = Paths.get("G:\\A-Workspace\\dataFile\\"+fileName+"_"+System.currentTimeMillis()+".txt");
	}
	
	
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		List<CharSequence> charSequenceList = null;
		int count = 0 ;
		
		while(productMap.size()> count || !getIsDone()) {
			log.info("Worker/File generator thread - Queue size would be :"+ productMap.size() +" Count value :"+ count);
			if(productMap.size()>count) {
				Queue<Product> queue = productMap.get(++count);
				charSequenceList  = queue.stream().parallel().map(p->p.toString()).collect(Collectors.toList());
				productMap.put(count, new ArrayBlockingQueue<>(1));
				log.info("Worker/File generator thread - Pre Writing file"+ charSequenceList.size() +"& Count value :"+ count);
				writeInFile(charSequenceList, filePath);
				log.info("Worker/File generator thread - Post Writing file"+ charSequenceList.size() +"& Count value :"+ count);
			}else {
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					log.error("Exception occured during file writing.");
				}
			}
		}
		long exitTime = System.currentTimeMillis();
		log.info("Time taken to generate file :"+ (exitTime - startTime)+" MS");
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
