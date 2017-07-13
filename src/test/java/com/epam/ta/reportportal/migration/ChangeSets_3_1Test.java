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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Created by Andrey_Ivanov1 on 06-Jun-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ChangeSets_3_1Test {

    @Mock
    private Mongo mongo = new Mongo("host");
    @Mock
    private DB db;
    @Mock
    private MongoTemplate mongoTemplate = new MongoTemplate(mongo, "databaseName");
    @InjectMocks
    private ChangeSets_3_1 changeSets31 = new ChangeSets_3_1();

    @Test
    public void removeDebugFieldTest() throws IOException {
        when(mongoTemplate.getDb()).thenReturn(db);
        changeSets31.removeDebugField(mongoTemplate);
        verify(mongoTemplate.getDb(), times(1)).doEval(anyString());
    }

}