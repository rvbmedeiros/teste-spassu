package com.spassu.livros.microservice.infrastructure.persistence.repository;

import com.spassu.livros.microservice.infrastructure.persistence.entity.AssuntoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AssuntoJpaRepository extends JpaRepository<AssuntoEntity, Integer> {

    Set<AssuntoEntity> findAllByCodAsIn(Set<Integer> ids);
}
