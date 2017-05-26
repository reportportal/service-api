package com.epam.ta.reportportal.core.imprt;

import com.epam.ta.reportportal.core.imprt.format.async.ImportLaunch;
import com.epam.ta.reportportal.core.imprt.format.async.ImportType;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;

@Service
public class ImportLaunchHandlerImpl implements ImportLaunchHandler {

    @Autowired
    private ImportLaunchFactoryImpl factory;

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public OperationCompletionRS importLaunch(String projectId, String userName, String format, MultipartFile file) {
        Project project = projectRepository.findOne(projectId);
        expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectId);

        ImportType type = ImportType.fromValue(format);
        expect(type, notNull()).verify(BAD_REQUEST_ERROR, type);

        ImportLaunch strategy = factory.getImportLaunch(type);
        String launch = strategy.importLaunch(projectId, userName, file);

        return new OperationCompletionRS("Launch with id = " + launch + " is successfully imported.");
    }
}
