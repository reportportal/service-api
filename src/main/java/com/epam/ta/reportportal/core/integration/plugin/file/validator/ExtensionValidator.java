/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.plugin.file.validator;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.ws.model.ErrorType;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.Ordered;
import org.springframework.web.multipart.MultipartFile;

/**
 * Validator that allows only predefined file extensions.
 *
 * @author <a href="mailto:budaevqwerty@gmail.com">Ivan Budayeu</a>
 */
public class ExtensionValidator implements FileValidator, Ordered {

  private final Set<String> allowedExtensions;

  public ExtensionValidator(Set<String> allowedExtensions) {
    this.allowedExtensions = allowedExtensions;
  }

  /**
   * Resolve and validate file type.
   *
   * @param pluginFile uploaded plugin
   */
  @Override
  public void validate(MultipartFile pluginFile) {
    String resolvedExtension = FilenameUtils.getExtension(pluginFile.getName());
    BusinessRule.expect(resolvedExtension, allowedExtensions::contains)
        .verify(ErrorType.PLUGIN_UPLOAD_ERROR,
            Suppliers.formattedSupplier("Unsupported plugin file extension = '{}'",
                resolvedExtension).get()
        );
  }

  @Override
  public int getOrder() {
    return 1;
  }
}