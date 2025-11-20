package org.conalton.textprocessor.infrastructure.persistence.constraints;

public record ConstraintViolation(ConstraintType type, String constraintName) {}
