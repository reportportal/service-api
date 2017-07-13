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

package com.epam.ta.reportportal.migration;

import com.mongodb.DB;
import com.mongodb.Mongo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Andrey_Ivanov1 on 06-Jun-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ChangeSets_3_0Test {

    @InjectMocks
    private ChangeSets_3_0 changeSets30 = new ChangeSets_3_0();
    @Mock
    private Mongo mongo = new Mongo("host");
    @Mock
    private DB db;
    @Mock
    private MongoTemplate mongoTemplate = new MongoTemplate(mongo, "databaseName");

    @Before
    public void setUp () {
        when(mongoTemplate.getDb()).thenReturn(db);
    }

    @Test
    public void useStepBasedCalcStrategyTest() throws IOException {
        changeSets30.useStepBasedCalcStrategy(mongoTemplate);
        verify(mongoTemplate.getDb(), times(1)).doEval(anyString());
    }

    @Test
    public void useUpdatedClassesForSettingsTest() throws IOException {
        changeSets30.useUpdatedClassesForSettings(mongoTemplate);
        verify(mongoTemplate.getDb(), times(1)).doEval(anyString());
    }

    @Test
    public void dropFavoritesCollectionTest() throws IOException {
        changeSets30.dropFavoritesCollection(mongoTemplate);
        verify(mongoTemplate.getDb(), times(1)).doEval(anyString());
    }

}