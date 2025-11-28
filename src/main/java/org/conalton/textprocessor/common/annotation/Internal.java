package org.conalton.textprocessor.common.annotation;

import java.lang.annotation.*;

/** Marks an API as internal to a specific component or set of components. */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface Internal {

  /** Classes that are explicitly allowed to call or use this API. */
  Class<?>[] allowedBy() default {};
}
