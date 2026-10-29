package com.sloth.OnlyStudent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sloth.OnlyStudent.entities.Aviso;

@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    @Modifying
    @Query("DELETE FROM Aviso a WHERE a.id = :id")
    int deleteAvisoById(@Param("id") Long id);
}
