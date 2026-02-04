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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.dao.AttributeRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.infrastructure.persistence.entity.attribute.Attribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author Ivan Budaev
 */
@Sql("/db/fill/attributes/attributes-fill.sql")
class AttributeRepositoryTest extends BaseMvcTest {

  @Autowired
  private AttributeRepository attributeRepository;

  @Test
  void shouldFindWhenNameIsPresent() {

    //given
    String name = "present";

    //when
    Optional<Attribute> attribute = attributeRepository.findByName(name);

    //then
    assertTrue("Attribute should exists", attribute.isPresent());
  }

  @Test
  void shouldNotFindWhenNameIsNotPresent() {

    //given
    String name = "not present";

    //when
    Optional<Attribute> attribute = attributeRepository.findByName(name);

    //then
    assertFalse(attribute.isPresent(), "Attribute should not exists");
  }

  @Test
  void getDefaultProjectAttributesTest() {
    final Set<Attribute> defaultProjectAttributes = attributeRepository.getDefaultProjectAttributes();
    defaultProjectAttributes.forEach(it -> assertTrue(
        "Attribute should exists",
        ProjectAttributeEnum.findByAttributeName(it.getName()).isPresent()
    ));
  }

  @Test
  void findById() {
    final Long attrId = 100L;
    final String attrName = "present";

    final Optional<Attribute> attrOptional = attributeRepository.findById(attrId);
    assertTrue("Attribute should exists", attrOptional.isPresent());
    assertEquals(attrName, attrOptional.get().getName(), "Incorrect attribute name");
  }

  @Test
  void deleteById() {
    final Long attrId = 100L;
    attributeRepository.deleteById(attrId);
    assertEquals(16, attributeRepository.findAll().size());
  }
}
