package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.FINISH_LAUNCH_NOT_ALLOWED;

/**
 * @author Konstantin Antipin
 */
@Service
public class FinishLaunchApprovalStrategy {

    @Autowired
    private LaunchRepository launchRepository;

    public void verifyNoInProgressItems(Launch launch) {
        expect(launchRepository.hasItems(launch.getId()), equalTo(Boolean.TRUE))
                .verify(FINISH_LAUNCH_NOT_ALLOWED, formattedSupplier("Launch {} has no items", launch.getId()));

        expect(launchRepository.hasItemsWithStatusEqual(launch.getId(), StatusEnum.IN_PROGRESS), equalTo(Boolean.FALSE))
                .verify(FINISH_LAUNCH_NOT_ALLOWED, formattedSupplier("Launch {} has items in progress", launch.getId()));
    }
}
