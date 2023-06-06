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

package com.epam.ta.reportportal.ws.converter;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.entity.item.PathName;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import javax.annotation.Nullable;
import org.springframework.stereotype.Component;

/**
 * @author Pavel Bortnik
 */
@Component
public class TestItemResourceAssembler extends PagedResourcesAssembler<TestItem, TestItemResource> {

  @Override
  public TestItemResource toResource(TestItem entity) {
    return TestItemConverter.TO_RESOURCE.apply(entity);
  }

  public TestItemResource toResource(TestItem entity, @Nullable PathName pathName) {
    TestItemResource resource = TestItemConverter.TO_RESOURCE.apply(entity);
    ofNullable(pathName).ifPresent(
        pn -> resource.setPathNames(TestItemConverter.PATH_NAME_TO_RESOURCE.apply(pn)));
    return resource;
  }
}
