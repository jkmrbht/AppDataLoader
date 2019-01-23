package com.example.dataloader.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.dataloader.entity.Article;
import com.example.dataloader.repository.ArticleRepository;


@Service
public class ArticleService implements IArticleService {
	@Autowired
	private ArticleRepository articleRepository;
	
	@PersistenceContext
	private EntityManager entityManager ;
	
	@Override
	@Transactional(readOnly=true)
	public Article getArticleById(long articleId) {
		Article obj = articleRepository.findById(articleId).get();
		return obj;
	}	
	@Override
	@Transactional(readOnly=true)
	public List<Article> getAllArticles(){
		List<Article> list = new ArrayList<>();
		articleRepository.findAll().forEach(e -> list.add(e));
		return list;
	}
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public synchronized boolean addArticle(Article article){
	   List<Article> list = articleRepository.findByTitleAndCategory(article.getTitle(), article.getCategory()); 	
       if (list.size() > 0) {
    	   return false;
       } else {
    	   articleRepository.save(article);
    	   return true;
       }
	}
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void updateArticle(Article article) {
		articleRepository.save(article);
	}
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void deleteArticle(int articleId) {
		articleRepository.delete(getArticleById(articleId));
	}
}
