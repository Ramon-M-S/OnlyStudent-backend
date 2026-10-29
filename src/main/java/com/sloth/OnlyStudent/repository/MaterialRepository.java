package com.sloth.OnlyStudent.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sloth.OnlyStudent.entities.Material;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long>{

	List<Material> findByTurmaCodigo(Long codigo, Pageable pageable);
	
	boolean existsByNameAndTurmaCodigo(String name, Long turmaCodigo);

	@Modifying
	@Query("DELETE FROM Material m WHERE m.id = :id")
	int deleteMaterialById(@Param("id") Long id);
	
}
