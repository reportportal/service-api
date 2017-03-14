/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.entity.AuthType;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.item.issue.ExternalSystemType;
import com.epam.ta.reportportal.ws.model.externalsystem.CreateExternalSystemRQ;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.inject.Provider;

public class ExternalSystemBuilderTest extends BaseTest {

    @Autowired
    private Provider<ExternalSystemBuilder> provider;

    @Autowired
    private BasicTextEncryptor simpleEncryptor;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testBeanScope() {
        Assert.assertTrue("Test builder should be prototype bean because it's not stateless",
                applicationContext.isPrototype(applicationContext.getBeanNamesForType(ExternalSystemBuilder.class)[0]));
    }

    @Test
    public void testAddExternalSystem() throws Exception {
        ExternalSystem actual = provider.get()
                .addExternalSystem(getCreateExternalSystemRQ(), BuilderTestsConstants.PROJECT).build();
        ExternalSystem expected = getExternalSystem();
        validateSystems(expected, actual);
    }

    private CreateExternalSystemRQ getCreateExternalSystemRQ() {
        CreateExternalSystemRQ rq = new CreateExternalSystemRQ();
        rq.setExternalSystemType(ExternalSystemType.JIRA.name());
        rq.setUrl(BuilderTestsConstants.URL);
        rq.setExternalSystemAuth(AuthType.BASIC.name());
        rq.setProject(BuilderTestsConstants.PROJECT);
        rq.setUsername(BuilderTestsConstants.NAME);
        rq.setPassword(BuilderTestsConstants.PASSWORD);
        rq.setAccessKey(BuilderTestsConstants.ACCESS_KEY);
        rq.setDomain(BuilderTestsConstants.DOMAIN);
        return rq;
    }

    private ExternalSystem getExternalSystem() {
        ExternalSystem externalSystem = new ExternalSystem();
        externalSystem.setExternalSystemType(ExternalSystemType.JIRA);
        externalSystem.setUrl(BuilderTestsConstants.URL);
        externalSystem.setExternalSystemAuth(AuthType.BASIC);
        externalSystem.setProject(BuilderTestsConstants.PROJECT);
        externalSystem.setUsername(BuilderTestsConstants.NAME);
        externalSystem.setPassword(simpleEncryptor.encrypt(BuilderTestsConstants.PASSWORD));
        externalSystem.setAccessKey(BuilderTestsConstants.ACCESS_KEY);
        externalSystem.setDomain(BuilderTestsConstants.DOMAIN);
        externalSystem.setProjectRef(BuilderTestsConstants.PROJECT);
        return externalSystem;
    }

    private void validateSystems(ExternalSystem expectedValue, ExternalSystem actualValue) {
        Assert.assertEquals(expectedValue.getExternalSystemType(), actualValue.getExternalSystemType());
        Assert.assertEquals(expectedValue.getUrl(), actualValue.getUrl());
        Assert.assertEquals(expectedValue.getExternalSystemAuth(), actualValue.getExternalSystemAuth());
        Assert.assertEquals(expectedValue.getProject(), actualValue.getProject());
        Assert.assertEquals(expectedValue.getUsername(), actualValue.getUsername());
        Assert.assertEquals(simpleEncryptor.decrypt(expectedValue.getPassword())
                , simpleEncryptor.decrypt(actualValue.getPassword()));
        Assert.assertEquals(expectedValue.getAccessKey(), actualValue.getAccessKey());
        Assert.assertEquals(expectedValue.getDomain(), actualValue.getDomain());
        Assert.assertEquals(expectedValue.getProjectRef(), BuilderTestsConstants.PROJECT);
    }
}