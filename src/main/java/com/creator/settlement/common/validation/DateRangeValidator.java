package com.creator.settlement.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String startField;
    private String endField;
    private String message;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startField = constraintAnnotation.startField();
        this.endField = constraintAnnotation.endField();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        FieldValues fieldValues = readFieldValues(value);
        if (fieldValues.hasEmptyBoundary()) {
            return true;
        }

        return isOrdered(fieldValues, context);
    }

    private FieldValues readFieldValues(Object value) {
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);
        return new FieldValues(
                beanWrapper.getPropertyValue(startField),
                beanWrapper.getPropertyValue(endField)
        );
    }

    private boolean isOrdered(FieldValues fieldValues, ConstraintValidatorContext context) {
        if (!(fieldValues.startValue() instanceof Comparable<?> comparableStart)) {
            return false;
        }

        try {
            @SuppressWarnings("unchecked")
            Comparable<Object> typedStart = (Comparable<Object>) comparableStart;
            boolean valid = typedStart.compareTo(fieldValues.endValue()) <= 0;
            if (!valid) {
                addViolation(context);
            }
            return valid;
        } catch (ClassCastException exception) {
            return false;
        }
    }

    private void addViolation(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(endField)
                .addConstraintViolation();
    }

    private record FieldValues(Object startValue, Object endValue) {

        private boolean hasEmptyBoundary() {
            return startValue == null || endValue == null;
        }
    }
}
