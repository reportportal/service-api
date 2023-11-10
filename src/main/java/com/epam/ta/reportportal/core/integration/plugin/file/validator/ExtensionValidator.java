package com.epam.ta.reportportal.core.integration.plugin.file.validator;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.Ordered;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
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