package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}