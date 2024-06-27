package com.springbootproject.mydreamshop.services;

import com.springbootproject.mydreamshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
