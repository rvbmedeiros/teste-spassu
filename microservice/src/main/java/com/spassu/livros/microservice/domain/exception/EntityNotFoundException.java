package com.spassu.livros.microservice.domain.exception;

public class EntityNotFoundException extends RuntimeException {

    private final String entityName;
    private final Object entityId;

    public EntityNotFoundException(String entityName, Object entityId) {
        super(String.format("%s with id '%s' not found", entityName, entityId));
        this.entityName = entityName;
        this.entityId = entityId;
    }

    public String getEntityName() { return entityName; }
    public Object getEntityId()   { return entityId; }
}
