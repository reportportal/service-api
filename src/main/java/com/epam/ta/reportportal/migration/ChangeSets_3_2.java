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

package com.epam.ta.reportportal.migration;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.DBObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Migration 3.2
 *
 * @author Pavel Bortnik
 */
@ChangeLog(order = "3.2")
public class ChangeSets_3_2 {

    private static final String HISTORY = "history";
    private static final String ID = "_id";
    private static final String COLLECTION = "activity";

    @ChangeSet(order = "3.2-1", id = "v3.2-Replace activities embedded collection 'history' with array", author = "pbortnik")
    public void replaceActivitesHistory(MongoTemplate mongoTemplate) {
        final Query q = new Query(Criteria.where(HISTORY).exists(true));
        q.fields().include(ID).include(HISTORY);
        mongoTemplate.stream(q, DBObject.class, COLLECTION).forEachRemaining(dbo -> {
            DBObject history = (DBObject) dbo.get(HISTORY);
            Update u = new Update();
            Map[] dbArray = new LinkedHashMap[history.keySet().size()];
            int i = 0;
            for (String key : history.keySet()) {
                DBObject o = (DBObject) history.get(key);
                Map<String, String> res = new HashMap<>(o.keySet().size() + 1);
                res.put("field", key);
                res.putAll(o.toMap());
                dbArray[i] = res;
                i++;
            }
            u.set(HISTORY, dbArray);
            mongoTemplate.updateFirst(Query.query(Criteria.where(ID).is(dbo.get(ID))), u, COLLECTION);
        });
    }
}
