/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;

/**
 * Created by Andrey_Ivanov1 on 31-May-17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class JobExecutorDelegateTest {

    @Configuration
    static class JobExecutorDelegateTestContextConfiguration {
        @Bean
        public JobExecutorDelegate jobExecutorDelegate() {
            return new JobExecutorDelegate();
        }
        @Bean
        public SelfCancalableJob selfCancalableJob() {
            return mock(SelfCancalableJob.class);
        }
        @Bean
        public TaskScheduler taskScheduler() {
            return mock(TaskScheduler.class);
        }
    }

    @Autowired
    private JobExecutorDelegate jobExecutorDelegate;
    @Autowired
    private SelfCancalableJob selfCancalableJob;
    @Autowired
    private TaskScheduler taskScheduler;

    @Test
    public void submitJobTest() {
        jobExecutorDelegate.submitJob(selfCancalableJob);
        verify(taskScheduler, times(1))
                .schedule(selfCancalableJob, selfCancalableJob);
    }

}