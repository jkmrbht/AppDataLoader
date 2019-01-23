package com.example.dataloader.repository;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

import java.util.stream.Stream;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;

import com.example.dataloader.entity.Product;

public interface ProductRepository extends CrudRepository<Product, Long> {
	@QueryHints(value=@QueryHint(name=HINT_FETCH_SIZE, value=""+10000))
	@Query(value="select p from Product p")
	Stream<Product> streamAllProductByQuery();
}
