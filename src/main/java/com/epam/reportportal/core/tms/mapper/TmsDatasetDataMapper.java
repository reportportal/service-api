package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsDatasetData;
import com.epam.reportportal.core.tms.dto.TmsDatasetDataRQ;
import com.epam.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsDatasetDataMapper {

  public abstract List<TmsDatasetData> convertToTmsDatasetData(
      Collection<TmsDatasetDataRQ> tmsDatasetDataRQs);
}
