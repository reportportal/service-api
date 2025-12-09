package com.epam.reportportal.core.integration.plugin.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.integration.plugin.binary.PluginFilesProvider;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import jakarta.activation.FileTypeMap;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PluginFilesProviderTest {

  private static final String BASE_DIRECTORY = "/base/plugins";
  private static final String FOLDER_QUALIFIER = "configs";
  private static final String VALID_PLUGIN_NAME = "my-plugin";

  @Mock
  private IntegrationTypeRepository integrationTypeRepository;

  @Mock
  private FileTypeMap fileTypeResolver;

  @InjectMocks
  private PluginFilesProvider pluginFilesProvider;

  @BeforeEach
  void setUp() {
    pluginFilesProvider = new PluginFilesProvider(
        BASE_DIRECTORY, FOLDER_QUALIFIER, fileTypeResolver, integrationTypeRepository
    );

    IntegrationType mockIntegrationType = new IntegrationType();
    mockIntegrationType.setName(VALID_PLUGIN_NAME);
    when(integrationTypeRepository.findByName(VALID_PLUGIN_NAME)).thenReturn(
        Optional.of(mockIntegrationType));
  }

  @Test
  void shouldThrowExceptionForRelativePathTraversal() {
    // given
    String maliciousFileName = "../../etc/passwd";

    // when
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> pluginFilesProvider.load(VALID_PLUGIN_NAME, maliciousFileName));

    // then
    assertEquals(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, exception.getErrorType());
    assertTrue(exception.getMessage().contains("Invalid or unsafe file name"));
  }

  @Test
  void shouldThrowExceptionForAbsoluteUnixPath() {
    // given
    String maliciousFileName = "/etc/passwd";

    // when
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> pluginFilesProvider.load(VALID_PLUGIN_NAME, maliciousFileName));

    // then
    assertEquals(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, exception.getErrorType());
    assertTrue(exception.getMessage().contains("Invalid or unsafe file name"));
  }

  @Test
  void shouldThrowExceptionWhenFileNotFound() {
    // given
    String nonExistentFileName = "non-existent-file.txt";

    // when
    ReportPortalException exception = assertThrows(ReportPortalException.class, () ->
        pluginFilesProvider.load(VALID_PLUGIN_NAME, nonExistentFileName));

    // then
    assertEquals(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, exception.getErrorType());
    assertTrue(exception.getMessage().contains(nonExistentFileName));
  }
}
