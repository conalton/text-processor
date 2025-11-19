package org.conalton.textprocessor.infrastructure.persistence.constraints.impl;

import static org.conalton.textprocessor.infrastructure.persistence.constraints.ConstraintViolationClassifier.BEAN_NAME;

import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.conalton.textprocessor.infrastructure.persistence.constraints.ConstraintType;
import org.conalton.textprocessor.infrastructure.persistence.constraints.ConstraintViolation;
import org.conalton.textprocessor.infrastructure.persistence.constraints.ConstraintViolationClassifier;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component(BEAN_NAME)
public class MySqlConstraintViolationClassifier extends ConstraintViolationClassifier {
  private static final String PRIMARY_KEY_NAME = "PRIMARY";
  private static final String SQL_STATE_INTEGRITY_CONSTRAINT = "23000";
  private static final int ERROR_CODE_DUPLICATE_KEY = 1062;
  private static final Pattern MYSQL_KEY_PATTERN = Pattern.compile("for key '([^']+)'");

  @Override
  public Optional<ConstraintViolation> classify(DataIntegrityViolationException ex) {
    Throwable cause = ex.getCause();
    if (!(cause instanceof ConstraintViolationException cve)) {
      return Optional.empty();
    }

    SQLException sqlEx = cve.getSQLException();

    if (!isDuplicateKey(sqlEx)) {
      return Optional.empty();
    }

    String constraintName = cve.getConstraintName();
    if (constraintName == null) {
      constraintName = extractConstraintName(sqlEx);
    }

    if (constraintName != null) {
      String normalized = constraintName.substring(constraintName.lastIndexOf('.') + 1);
      if (PRIMARY_KEY_NAME.equalsIgnoreCase(normalized)) {
        return Optional.of(new ConstraintViolation(ConstraintType.PRIMARY_KEY, normalized));
      }
    }

    return Optional.empty();
  }

  private boolean isDuplicateKey(SQLException ex) {
    if (ex == null) {
      return false;
    }

    return SQL_STATE_INTEGRITY_CONSTRAINT.equals(ex.getSQLState())
        && ex.getErrorCode() == ERROR_CODE_DUPLICATE_KEY;
  }

  private String extractConstraintName(SQLException sqlEx) {
    if (sqlEx == null || sqlEx.getMessage() == null) {
      return null;
    }

    Matcher matcher = MYSQL_KEY_PATTERN.matcher(sqlEx.getMessage());
    if (matcher.find()) {
      String fullName = matcher.group(1);
      return fullName.substring(fullName.lastIndexOf('.') + 1);
    }

    return null;
  }
}
