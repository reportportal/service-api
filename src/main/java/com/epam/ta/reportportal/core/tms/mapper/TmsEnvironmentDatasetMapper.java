package com.epam.ta.reportportal.core.tms.mapper;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.epam.ta.reportportal.core.tms.db.entity.TmsDataset;
import com.epam.ta.reportportal.core.tms.db.entity.TmsEnvironmentDataset;
import com.epam.ta.reportportal.core.tms.dto.TmsEnvironmentDatasetRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsEnvironmentDatasetRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsEnvironmentDatasetMapper {

  public Set<TmsEnvironmentDataset> convertToEnvironmentDatasets(
      TmsDataset tmsDataset, Collection<TmsEnvironmentDatasetRQ> environmentDatasetRQs) {
    if (isEmpty(environmentDatasetRQs)) {
      return null;
    }
    return environmentDatasetRQs
        .stream()
        .map(environmentDatasetRQ -> convertToEnvironmentDataset(tmsDataset, environmentDatasetRQ))
        .collect(Collectors.toSet());
  }

  @Mapping(target = "dataset", source = "tmsDataset")
  @Mapping(target = "environment.id", source = "environmentDatasetRQ.environmentId")
  @Mapping(target = "datasetType", source = "environmentDatasetRQ.datasetType")
  @Mapping(target = "id.datasetId", source = "tmsDataset.id")
  @Mapping(target = "id.environmentId", source = "environmentDatasetRQ.environmentId")
  public abstract TmsEnvironmentDataset convertToEnvironmentDataset(TmsDataset tmsDataset,
      TmsEnvironmentDatasetRQ environmentDatasetRQ);

  @Mapping(target = "environmentId", source = "id.environmentId")
  public abstract TmsEnvironmentDatasetRS convertToTmsEnvironmentDatasetRS(
      TmsEnvironmentDataset tmsEnvironmentDataset);
}
