package com.spassu.livros.microservice.infrastructure.persistence.repository;

import com.spassu.livros.microservice.infrastructure.persistence.entity.AutorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AutorJpaRepository extends JpaRepository<AutorEntity, Integer> {

    Set<AutorEntity> findAllByCodAuIn(Set<Integer> ids);
}
