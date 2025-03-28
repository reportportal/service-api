package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsDataset;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsDatasetParserFactory;
import java.util.Collection;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

@Mapper(config = CommonMapperConfig.class, uses = TmsEnvironmentDatasetMapper.class)
public abstract class TmsDatasetMapper {

  @Autowired
  private TmsDatasetParserFactory tmsDatasetParserFactory;

  @Mapping(target = "project.id", source = "projectId")
  @Mapping(target = "data", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  @Mapping(target = "environmentDatasets", ignore = true)
  public abstract TmsDataset convertFromRQ(Long projectId, TmsDatasetRQ tmsDatasetRQ);

  public abstract TmsDatasetRS convertToRS(TmsDataset tmsDataset);

  public abstract List<TmsDatasetRS> convertToRS(Collection<TmsDataset> tmsDatasets);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL, nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION)
  @Mapping(target = "id", ignore = true)
  public abstract void update(@MappingTarget TmsDataset targetDataset, TmsDataset dataset);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "id", ignore = true)
  public abstract void patch(@MappingTarget TmsDataset existingDataset,
      TmsDataset dataset);

  public List<TmsDatasetRQ> convertToRQ(MultipartFile file) {
    return tmsDatasetParserFactory.getParser(file).parse(file);
  }
}
