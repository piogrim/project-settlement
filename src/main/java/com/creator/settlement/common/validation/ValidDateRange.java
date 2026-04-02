package com.creator.settlement.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
public @interface ValidDateRange {

    String message() default "종료일은 시작일과 같거나 이후여야 합니다.";

    String startField();

    String endField();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
