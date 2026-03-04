package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import com.epam.reportportal.base.core.tms.dto.UploadAttachmentRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import java.time.Duration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsAttachmentMapper {

  @Value("${rp.tms.attachment.ttl:PT24H}")
  protected Duration ttl;

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "fileName", source = "file.originalFilename")
  @Mapping(target = "fileType", source = "file.contentType")
  @Mapping(target = "fileSize", source = "file.size")
  @Mapping(target = "pathToFile", source = "fileId")
  @Mapping(target = "expiresAt", expression = "java(java.time.Instant.now().plus(ttl))")
  public abstract TmsAttachment convertToAttachment(String fileId, MultipartFile file);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "fileSize", source = "fileSize")
  @Mapping(target = "fileType", source = "fileType")
  @Mapping(target = "fileName", source = "fileName")
  public abstract UploadAttachmentRS convertToUploadAttachmentRS(TmsAttachment attachment);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "expiresAt", ignore = true) // Note: TTL is not copied, making duplicated attachments permanent
  @Mapping(target = "fileName", source = "originalAttachment.fileName")
  @Mapping(target = "fileType", source = "originalAttachment.fileType")
  @Mapping(target = "fileSize", source = "originalAttachment.fileSize")
  @Mapping(target = "pathToFile", source = "newFileId")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "steps", ignore = true)
  @Mapping(target = "textManualScenarios", ignore = true)
  @Mapping(target = "manualScenarioPreconditions", ignore = true)
  public abstract TmsAttachment duplicateAttachment(TmsAttachment originalAttachment,
      String newFileId);
}
