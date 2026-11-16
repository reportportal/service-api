package com.epam.reportportal.base.infrastructure.persistence.binary.tms;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.infrastructure.commons.Thumbnailator;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataEncoder;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.tms.TmsDataStore;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * TMS Attachment Data Store Service for managing TMS-specific attachments. Uses dedicated TMS DataStore bean to
 * separate TMS attachments from regular attachments.
 *
 * @author ReportPortal Team
 */
@Service("tmsAttachmentDataStoreService")
public class TmsAttachmentDataStoreService implements TmsDataStoreService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TmsAttachmentDataStoreService.class);

  private final TmsDataStore dataStore;
  private final DataEncoder dataEncoder;
  private final Thumbnailator thumbnailator;

  @Autowired
  public TmsAttachmentDataStoreService(
      TmsDataStore dataStore,
      DataEncoder dataEncoder,
      @Qualifier("attachmentThumbnailator") Thumbnailator thumbnailator) {
    this.dataEncoder = dataEncoder;
    this.dataStore = dataStore;
    this.thumbnailator = thumbnailator;
  }

  @Override
  public String saveThumbnail(String fileName, InputStream data) {
    try {
      return dataEncoder.encode(dataStore.save(fileName, thumbnailator.createThumbnail(data)));
    } catch (IOException e) {
      LOGGER.error("TMS thumbnail is not created for file [{}]. Error:\n{}", fileName, e);
    }
    return null;
  }

  @Override
  public String save(String fileName, InputStream data) {
    return dataEncoder.encode(dataStore.save(fileName, data));
  }

  @Override
  public void delete(String fileId) {
    dataStore.delete(dataEncoder.decode(fileId));
  }

  @Override
  public void deleteAll(List<String> fileIds, String bucketName) {
    dataStore.deleteAll(
        fileIds.stream().map(dataEncoder::decode).collect(Collectors.toList()), bucketName);
  }

  @Override
  public void deleteContainer(String containerName) {
    dataStore.deleteContainer(containerName);
  }

  @Override
  public Optional<InputStream> load(String fileId) {
    return ofNullable(dataStore.load(dataEncoder.decode(fileId)));
  }
}
