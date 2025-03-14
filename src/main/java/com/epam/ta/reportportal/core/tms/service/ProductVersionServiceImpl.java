package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsMilestone;
import com.epam.ta.reportportal.core.tms.db.entity.TmsProductVersion;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.db.repository.ProductVersionRepository;
import com.epam.ta.reportportal.core.tms.dto.ProductVersionRQ;
import com.epam.ta.reportportal.core.tms.dto.ProductVersionRS;
import com.epam.ta.reportportal.core.tms.exception.NotFoundException;
import com.epam.ta.reportportal.core.tms.mapper.DtoMapper;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductVersionServiceImpl implements ProductVersionService {

  private static final String VERSION_NOT_FOUND_BY_ID = "Product Version cannot be found by id: {0}";

  private final DtoMapper<TmsProductVersion, ProductVersionRS> productVersionMapper;
  private final ProductVersionRepository productVersionRepository;

  @Override
  public ProductVersionRS create(long projectID, final ProductVersionRQ inputDto) {
//        final var testSuite = testSuiteRepository.findById(inputDto.testFolderId())
//                .orElseThrow(NotFoundException.supplier(TEST_SUITE_NOT_FOUND_BY_ID, inputDto.testFolderId())); // replace by getting default Test Suite
    final var productVersion = new TmsProductVersion(null,
        inputDto.version(),
        inputDto.documentation(),
        projectID,
        inputDto.testPlans().stream().map(it -> {
          var tp = new TmsTestPlan();
          tp.setId(it);
          return tp;
        }).collect(Collectors.toSet()),
        inputDto.milestones().stream().map(it -> {
          var milestone = new TmsMilestone();
          milestone.setId(it);
          return milestone;
        }).collect(Collectors.toSet()));

    return productVersionMapper.convert(productVersionRepository.save(productVersion));
  }

  @Override
  public ProductVersionRS update(long projectID, final Long productVersionID,
      final ProductVersionRQ inputDto) {
    final var productVersion = new TmsProductVersion(productVersionID,
        inputDto.version(),
        inputDto.documentation(),
        projectID,
        inputDto.testPlans().stream().map(it -> {
          var tp = new TmsTestPlan();
          tp.setId(it);
          return tp;
        }).collect(Collectors.toSet()),
        inputDto.milestones().stream().map(it -> {
          var milestone = new TmsMilestone();
          milestone.setId(it);
          return milestone;
        }).collect(Collectors.toSet()));

    return productVersionMapper.convert(productVersionRepository.save(productVersion));
  }

  @Override
  public ProductVersionRS patch(long projectId, Long aLong, ProductVersionRQ t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(long projectID, Long id) {
    productVersionRepository.deleteById(id);
  }

  @Override
  public ProductVersionRS getById(long projectID, Long id) {
    return productVersionMapper.convert(productVersionRepository.findById(id)
        .orElseThrow(NotFoundException.supplier(VERSION_NOT_FOUND_BY_ID, id)));
  }


}
