package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.model.TestCase;
import com.epam.ta.reportportal.core.tms.dto.TestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.TestCaseVersionRS;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TestCaseMapper implements DtoMapper<TestCase, TestCaseRS> {

    public TestCaseRS convert(final TestCase testCase) {
        return new TestCaseRS(testCase.getId(),
                testCase.getName(),
                testCase.getDescription(),
                testCase.getVersions()
                        .stream()
                        .map(model -> new TestCaseVersionRS(model.getId(),
                                model.getName(),
                                model.isDefault(),
                                model.isDraft(),
                                null))
                        .collect(Collectors.toSet()));
    }

}
