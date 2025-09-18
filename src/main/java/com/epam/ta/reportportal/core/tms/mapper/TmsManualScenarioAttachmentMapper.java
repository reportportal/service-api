package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsAttachment;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioAttachmentRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioAttachmentRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting between TmsAttachment entity and manual scenario attachment DTOs.
 * Uses MapStruct for automatic mapping generation.
 */
@Mapper(config = CommonMapperConfig.class)
public interface TmsManualScenarioAttachmentMapper {

  /**
   * Converts attachment entity to response DTO.
   *
   * @param attachment the attachment entity
   * @return the attachment response DTO
   */
  @Mapping(target = "id", source = "id", numberFormat = "0")
  TmsManualScenarioAttachmentRS toResponse(TmsAttachment attachment);

  /**
   * Converts list of attachments to response DTOs.
   *
   * @param attachments the list of attachment entities
   * @return the list of attachment response DTOs
   */
  List<TmsManualScenarioAttachmentRS> toResponseList(List<TmsAttachment> attachments);
}
