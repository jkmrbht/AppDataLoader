package com.example.dataloader.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="product")
public class Product implements Serializable {
	 	@Id
	    @GeneratedValue(strategy = GenerationType.AUTO)
	    private Long id;

	    @Version
	    private Integer version;

	    @Column(name="product_Id")
	    private String productId;
	    
	    @Column(name="description")
	    private String description;
	    
	    @Column(name="image_url")
	    private String imageUrl;
	    
	    @Column(name="price")
	    private BigDecimal price;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Integer getVersion() {
			return version;
		}

		public void setVersion(Integer version) {
			this.version = version;
		}

		public String getProductId() {
			return productId;
		}

		public void setProductId(String productId) {
			this.productId = productId;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getImageUrl() {
			return imageUrl;
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}

		public BigDecimal getPrice() {
			return price;
		}

		public void setPrice(BigDecimal price) {
			this.price = price;
		}

		@Override
		public String toString() {
			return "Product [id=" + id + ", version=" + version + ", productId=" + productId + ", description="
					+ description + ", imageUrl=" + imageUrl + ", price=" + price + "]";
		}
}
