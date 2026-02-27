package com.nxp.iemdm.generator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.hibernate.annotations.IdGeneratorType;
import org.hibernate.id.enhanced.Optimizer;

@IdGeneratorType(ExistingIdGenerator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface IemdmSequence {
  String name();

  int startWith() default 1;

  int incrementBy() default 50;

  Class<? extends Optimizer> optimizer() default Optimizer.class;
}
