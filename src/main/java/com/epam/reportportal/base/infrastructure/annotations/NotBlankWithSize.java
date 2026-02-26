/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.infrastructure.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:tatyana_gladysheva@epam.com">Tatyana Gladysheva</a>
 */
@Target({FIELD, METHOD})
@Documented
@Constraint(validatedBy = NotBlankWithSizeValidator.class)
@Retention(RUNTIME)
public @interface NotBlankWithSize {

  String message() default EMPTY;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  int min() default 0;

  int max() default Integer.MAX_VALUE;
}
