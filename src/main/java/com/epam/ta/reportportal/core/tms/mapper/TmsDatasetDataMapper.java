package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsDatasetData;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetDataRQ;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsDatasetDataMapper {

  public abstract List<TmsDatasetData> convertToTmsDatasetData(
      Collection<TmsDatasetDataRQ> tmsDatasetDataRQs);
}
