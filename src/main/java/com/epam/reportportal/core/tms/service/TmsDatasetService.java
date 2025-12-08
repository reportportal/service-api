package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.TmsDatasetRQ;
import com.epam.reportportal.core.tms.dto.TmsDatasetRS;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface TmsDatasetService extends CrudService<TmsDatasetRQ, TmsDatasetRS, Long> {

  List<TmsDatasetRS> getByProjectId(Long projectId);

  List<TmsDatasetRS> uploadFromFile(Long projectId, MultipartFile file);
}
