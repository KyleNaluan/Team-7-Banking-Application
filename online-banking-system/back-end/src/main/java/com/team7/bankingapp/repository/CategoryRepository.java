package com.team7.bankingapp.repository;

import com.team7.bankingapp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByCategoryName(String categoryName);
}
