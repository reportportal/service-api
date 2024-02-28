/*
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.item.identity;

import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Generates the unique identifier for test item based on Base64 encoding and includes information
 * about project, name of item's launch, full path of item's parent names, item name and
 * parameters.
 *
 * @author Pavel_Bortnik
 */
@Service
public class TestItemUniqueIdGenerator implements UniqueIdGenerator {

  private static final long MAXIMUM_SIZE = 5000;

  private static final long EXPIRATION_SECONDS = 30;
  private static final String TRAIT = "auto:";

  private static long dbQueries = 0;

  private LoadingCache<Long, TestItem> itemsCache;

  private TestItemRepository testItemRepository;

  public TestItemUniqueIdGenerator(TestItemRepository testItemRepository) {
    this.testItemRepository = testItemRepository;
    itemsCache = CacheBuilder.newBuilder().maximumSize(MAXIMUM_SIZE)
        .expireAfterWrite(EXPIRATION_SECONDS, TimeUnit.SECONDS)
        .build(new CacheLoader<>() {
          @Override
          public TestItem load(Long id) {
            return testItemRepository.findById(id).orElse(null);
          }
        });
  }

  @Autowired
  public void setTestItemRepository(TestItemRepository testItemRepository) {
    this.testItemRepository = testItemRepository;
  }

  @Override
  public String generate(TestItem testItem, List<Long> parentIds, Launch launch) {
    String forEncoding = prepareForEncoding(testItem, parentIds, launch);
    return TRAIT + DigestUtils.md5Hex(forEncoding);
  }

  @Override
  public boolean validate(String encoded) {
    return !Strings.isNullOrEmpty(encoded) && encoded.startsWith(TRAIT);
  }

  private String prepareForEncoding(TestItem testItem, List<Long> parentIds, Launch launch) {
    Long projectId = launch.getProjectId();
    String launchName = launch.getName();
    List<String> pathNames = getPathNames(parentIds);
    String itemName = testItem.getName();
    StringJoiner joiner = new StringJoiner(";");
    joiner.add(projectId.toString()).add(launchName);
    if (!CollectionUtils.isEmpty(pathNames)) {
      joiner.add(String.join(";", pathNames));
    }
    joiner.add(itemName);
    Set<Parameter> parameters = testItem.getParameters();
    if (!CollectionUtils.isEmpty(parameters)) {
      joiner.add(parameters.stream()
          .map(parameter ->
              (!Strings.isNullOrEmpty(parameter.getKey()) ? parameter.getKey() + "=" : "")
                  + parameter.getValue())
          .collect(Collectors.joining(",")));
    }
    return joiner.toString();
  }

  private List<String> getPathNames(List<Long> parentIds) {
    List<TestItem> testItems = getTestItems(parentIds);
    return testItemRepository.findAllById(parentIds)
        .stream()
        .sorted(Comparator.comparingLong(TestItem::getItemId))
        .map(TestItem::getName)
        .collect(Collectors.toList());
  }

  private List<TestItem> getTestItems(List<Long> parentIds) {
    List<TestItem> testItems = new ArrayList<>();
    for(Long id: parentIds) {
      TestItem testItem = itemsCache.getIfPresent(id);
      System.out.println("TestItem id " + id);
      if (testItem == null) {
        dbQueries++;
        System.out.println("TestItemUniqueIdGenerator db queries: " + dbQueries);
        testItem = testItemRepository.getById(id);
        itemsCache.put(id, testItem);
        CacheStats stats = itemsCache.stats();
        System.out.println("Cache status: " + stats.toString());
      } else {
        System.out.println("Founded in cache");
      }
      testItems.add(testItem);
    }
    return testItems;
  }
}