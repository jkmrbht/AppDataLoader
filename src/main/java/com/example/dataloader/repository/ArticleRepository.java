package com.example.dataloader.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.dataloader.entity.Article;

public interface ArticleRepository extends CrudRepository<Article, Long>  {
    List<Article> findByTitle(String title);
    List<Article> findDistinctByCategory(String category);
    List<Article> findByTitleAndCategory(String title, String category);
}
