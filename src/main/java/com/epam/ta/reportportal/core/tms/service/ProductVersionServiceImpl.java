package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsProductVersion;
import com.epam.ta.reportportal.core.tms.db.repository.ProductVersionRepository;
import com.epam.ta.reportportal.core.tms.dto.ProductVersionRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsProductVersionRS;
import com.epam.ta.reportportal.core.tms.mapper.TmsProductVersionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductVersionServiceImpl implements ProductVersionService {

  private static final String VERSION_NOT_FOUND_BY_ID = "Product Version with id: %d";

  private final TmsProductVersionMapper productVersionMapper;
  private final ProductVersionRepository productVersionRepository;

  @Override
  @Transactional
  public TmsProductVersionRS create(long projectId, final ProductVersionRQ inputDto) {
    final var productVersion = new TmsProductVersion(null,
        inputDto.version(), inputDto.documentation(), projectId);
    return productVersionMapper.convert(productVersionRepository.save(productVersion));
  }

  @Override
  @Transactional
  public TmsProductVersionRS update(long projectId, final Long productVersionId,
      final ProductVersionRQ inputDto) {
    final var productVersion = new TmsProductVersion(productVersionId,
        inputDto.version(),
        inputDto.documentation(),
        projectId);
    return productVersionMapper.convert(productVersionRepository.save(productVersion));
  }

  @Override
  public TmsProductVersionRS patch(long projectId, Long productVersionId, ProductVersionRQ t) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Transactional
  public void delete(long projectId, Long id) {
    productVersionRepository.deleteByIdAndProjectId(id, projectId);
  }

  @Override
  public TmsProductVersionRS getById(long projectId, Long id) {
    return productVersionRepository.findByProjectIdAndId(projectId, id)
        .map(productVersionMapper::convert)
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, VERSION_NOT_FOUND_BY_ID.formatted(id)));
  }
}
