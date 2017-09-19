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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.Parameter;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.google.common.base.Strings;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Generates the unique identifier for test item based
 * on Base64 encoding and includes information about project,
 * name of item's launch, full path of item's parent names,
 * item name and parameters.
 *
 * @author Pavel_Bortnik
 */
@Service
public class TestItemUniqueIdGenerator implements UniqueIdGenerator {

    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private static final String SECRET = "auto:";

    private static final String COLLECTION = "generationCheckpoint";
    private static final String CHECKPOINT_ID = "checkpoint";
    private static final String CHECKPOINT_ITEM_ID = "item_id";
    private static final int BATCH_SIZE = 1000;

    @Autowired
    private TestItemRepository testItemRepository;

    @Autowired
    private LaunchRepository launchRepository;

    @Autowired
    private MongoOperations mongoOperations;

    @EventListener
    public void onContextRefresh(ContextRefreshedEvent event) {
        if (mongoOperations.collectionExists(COLLECTION)) {
            Executors.newSingleThreadExecutor().execute(this::generateForAll);
        }
    }

    @Override
    public String generate(TestItem testItem) {
        String forEncoding = prepareForEncoding(testItem);
        return ENCODER.encodeToString(forEncoding.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean validate(String encoded) {
        return !Strings.isNullOrEmpty(encoded) && new String(DECODER.decode(encoded), StandardCharsets.UTF_8).startsWith(SECRET);
    }

    @Override
    public void generateForAll() {
        String checkPoint = getLastCheckPoint();
        try (CloseableIterator<TestItem> itemIterator = getItemIterator(checkPoint)) {
            List<TestItem> testItems = new ArrayList<>(BATCH_SIZE);
            while (itemIterator.hasNext()) {
                TestItem next = itemIterator.next();
                if (next != null) {
                    String uniqueId = generate(next);
                    next.setUniqueId(uniqueId);
                    if (checkPoint == null) {
                        checkPoint = next.getId();
                    }
                    testItems.add(next);
                    if (testItems.size() == BATCH_SIZE || !itemIterator.hasNext()) {
                        createCheckpoint(checkPoint);
                        testItemRepository.save(testItems);
                        testItems = new ArrayList<>(BATCH_SIZE);
                        checkPoint = null;
                    }
                }

            }
        }
        mongoOperations.getCollection(COLLECTION).drop();
    }

    private void createCheckpoint(String checkPoint) {
        BasicDBObject checkpoint = new BasicDBObject("_id", CHECKPOINT_ID).append(CHECKPOINT_ITEM_ID, checkPoint);
        mongoOperations.getCollection(COLLECTION).save(checkpoint);
    }

    private CloseableIterator<TestItem> getItemIterator(String checkPoint) {
        Sort sort = new Sort(Sort.Direction.ASC, "_id");
        Query query = new Query().with(sort).noCursorTimeout();
        query.addCriteria(Criteria.where("uniqueId").exists(false));
        if (checkPoint != null) {
            query.addCriteria(Criteria.where("_id").gte(new ObjectId(checkPoint)));
        }
        return mongoOperations.stream(query, TestItem.class);
    }

    private String getLastCheckPoint() {
        DBObject object = mongoOperations.getCollection(COLLECTION).findOne(new BasicDBObject("_id", CHECKPOINT_ID));
        return object == null ? null : (String) object.get(CHECKPOINT_ITEM_ID);
    }

    private String prepareForEncoding(TestItem testItem) {
        Launch launch = launchRepository.findOne(testItem.getLaunchRef());
        String launchName = launch.getName();
        String projectName = launch.getProjectRef();
        List<String> pathNames = getPathNames(testItem.getPath());
        String itemName = testItem.getName();
        List<Parameter> parameters = Optional.ofNullable(testItem.getParameters()).orElse(Collections.emptyList());
        StringJoiner joiner = new StringJoiner(";");
        joiner.add(SECRET).add(projectName).add(launchName);
        if (!CollectionUtils.isEmpty(pathNames)) {
            joiner.add(pathNames.stream().collect(Collectors.joining(",")));
        }
        joiner.add(itemName);
        if (!parameters.isEmpty()) {
            joiner.add(parameters.stream().map(parameter ->
                    (!Strings.isNullOrEmpty(parameter.getKey()) ?
                            parameter.getKey() + "=" : "") + parameter.getValue()).collect(Collectors.joining(",")));
        }
        return joiner.toString();
    }

    private List<String> getPathNames(List<String> path) {
        Map<String, String> names = testItemRepository.findPathNames(path);
        return path.stream().map(names::get).collect(Collectors.toList());
    }
}
