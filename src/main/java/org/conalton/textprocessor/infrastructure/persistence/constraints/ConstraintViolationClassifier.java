package org.conalton.textprocessor.infrastructure.persistence.constraints;

import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;

public abstract class ConstraintViolationClassifier {
  public static final String BEAN_NAME = "constraintViolationClassifier";

  public abstract Optional<ConstraintViolation> classify(DataIntegrityViolationException ex);

  public boolean isPrimaryKeyViolation(DataIntegrityViolationException ex) {
    Optional<ConstraintViolation> violation = classify(ex);
    return violation.filter(v -> v.type() == ConstraintType.PRIMARY_KEY).isPresent();
  }
}
