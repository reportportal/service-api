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

package com.epam.reportportal.base.annotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.annotations.NotBlankStringCollection;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class NotBlankStringCollectionValidatorTest {

  @Test
  public void testCollectionWithBlankElement() {
    TestEntity testEntity = new TestEntity();
    ArrayList<String> collection = new ArrayList<String>();
    collection.add("   ");
    collection.add("test");
    testEntity.setCollection(collection);

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();
    Set<ConstraintViolation<TestEntity>> constraints = validator.validate(testEntity);

    boolean found = false;
    for (ConstraintViolation<?> cv : constraints) {
      if ("collection".equals(cv.getPropertyPath().iterator().next().getName()) && cv.getMessage()
          .contains("should not contain blank elements")) {
        found = true;
        break;
      }
    }

    assertTrue(found);
  }

  @Test
  public void testCollectionWithoutBlankElements() {
    TestEntity testEntity = new TestEntity();
    ArrayList<String> collection = new ArrayList<String>();
    collection.add("test1 ");
    collection.add(" test2");
    testEntity.setCollection(collection);

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();
    Set<ConstraintViolation<TestEntity>> constraints = validator.validate(testEntity);

    boolean found = false;
    for (ConstraintViolation<?> cv : constraints) {
      if ("collection".equals(cv.getPropertyPath().iterator().next().getName()) && cv.getMessage()
          .contains("should not contain blank elements")) {
        found = true;
        break;
      }
    }

    assertFalse(found);
  }

  @Test
  public void testNull() {
    TestEntity testEntity = new TestEntity();
    testEntity.setCollection(null);

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();
    Set<ConstraintViolation<TestEntity>> constraints = validator.validate(testEntity);

    boolean found = false;
    for (ConstraintViolation<?> cv : constraints) {
      if ("collection".equals(cv.getPropertyPath().iterator().next().getName()) && cv.getMessage()
          .contains("should not contain blank elements")) {
        found = true;
        break;
      }
    }

    assertFalse(found);
  }

  @Test
  public void testEmptyCollection() {
    TestEntity testEntity = new TestEntity();
    testEntity.setCollection(new ArrayList<String>());

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();
    Set<ConstraintViolation<TestEntity>> constraints = validator.validate(testEntity);

    boolean found = false;
    for (ConstraintViolation<?> cv : constraints) {
      if ("collection".equals(cv.getPropertyPath().iterator().next().getName()) && cv.getMessage()
          .contains("should not contain blank elements")) {
        found = true;
        break;
      }
    }

    assertFalse(found);
  }

  @Setter
  @Getter
  private static class TestEntity {

    @NotBlankStringCollection
    private List<String> collection;

  }
}
