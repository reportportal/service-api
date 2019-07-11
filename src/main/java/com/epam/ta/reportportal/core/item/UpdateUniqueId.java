/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.item;

import org.springframework.stereotype.Service;

/**
 * Heavy update script for all test items
 *
 * @author Pavel Bortnik
 */
@Service
class UpdateUniqueId {
	//
	//	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateUniqueId.class);
	//
	//	private static final String COLLECTION = "generationCheckpoint";
	//	private static final String CHECKPOINT = "checkpoint";
	//	private static final String CHECKPOINT_ID = "testItemId";
	//
	//	private static final int BATCH_SIZE = 100;
	//
	//	private static final String SECRET = "auto:";
	//
	//	//launches cache
	//	private static final Cache<String, Launch> launchCache = Caffeine.newBuilder().maximumSize(200).build();
	//
	//	@Autowired
	//	private MongoOperations mongoOperations;
	//
	//	@Autowired
	//	private TestItemRepository testItemRepository;
	//
	//	private static final AtomicBoolean STARTED = new AtomicBoolean();
	//
	//	@EventListener
	//	public void onContextRefresh(ContextRefreshedEvent event) {
	//		if (STARTED.compareAndSet(false, true)) {
	//			if (mongoOperations.collectionExists(COLLECTION)) {
	//				Executors.newSingleThreadExecutor().execute(this::generateForAll);
	//			}
	//		}
	//	}
	//
	//	private void generateForAll() {
	//		long forUpdate = mongoOperations.count(testItemQuery(), TestItem.class);
	//		long update = 0;
	//		boolean isOk;
	//		String checkpoint = getLastCheckpoint();
	//		//potential endless loop
	//		do {
	//			try (CloseableIterator<TestItem> itemIterator = getItemIterator(checkpoint)) {
	//				List<TestItem> testItems = new ArrayList<>(BATCH_SIZE);
	//				int counter = 0;
	//				while (itemIterator.hasNext()) {
	//					TestItem testItem = itemIterator.next();
	//					if (testItem != null) {
	//						boolean isRemoved = removeIfInvalid(testItem);
	//						if (!isRemoved) {
	//							if (checkpoint == null) {
	//								checkpoint = testItem.getId();
	//							}
	//							String uniqueId = generate(testItem);
	//							testItem.setUniqueId(uniqueId);
	//							testItems.add(testItem);
	//							if (testItems.size() == BATCH_SIZE || !itemIterator.hasNext()) {
	//								createCheckpoint(checkpoint);
	//								updateTestItems(testItems);
	//								counter++;
	//								if (counter == 1000) {
	//									LOGGER.info("Generated uniqueId for " + update + " items. " + "It is " + ((update / (float) forUpdate)
	//											* 100) + "% done");
	//									counter = 0;
	//								}
	//								update += testItems.size();
	//								testItems = new ArrayList<>(BATCH_SIZE);
	//								checkpoint = null;
	//							}
	//						}
	//					}
	//				}
	//				isOk = true;
	//			} catch (Exception e) {
	//				LOGGER.warn("Potential endless loop in reason of: ", e);
	//				//continue generating uniqueId
	//				isOk = false;
	//			}
	//		} while (!isOk);
	//		STARTED.set(false);
	//		mongoOperations.getCollection(COLLECTION).drop();
	//		launchCache.cleanUp();
	//		LOGGER.info("Generating uniqueId is done!");
	//		indexUniqueIds();
	//
	//	}
	//
	//	private void indexUniqueIds() {
	//		mongoOperations.indexOps(mongoOperations.getCollectionName(TestItem.class))
	//				.ensureIndex(new Index().on("uniqueId", Sort.Direction.ASC));
	//	}
	//
	//	private void updateTestItems(List<TestItem> testItems) {
	//		BulkOperations bulk = mongoOperations.bulkOps(BulkOperations.BulkMode.UNORDERED, TestItem.class);
	//		testItems.forEach(it -> {
	//			Update update = new Update();
	//			update.set("uniqueId", it.getUniqueId());
	//			bulk.updateOne(query(where("_id").is(it.getId())), update);
	//		});
	//		bulk.execute();
	//	}
	//
	//	private boolean removeIfInvalid(TestItem item) {
	//		String launchRef = item.getLaunchRef();
	//		if (launchRef == null) {
	//			mongoOperations.remove(query(where("_id").is(item.getId())));
	//		}
	//
	//		Launch launch = mongoOperations.findOne(launchQuery(launchRef), Launch.class);
	//		if (launch == null) {
	//			testItemRepository.delete(item.getId());
	//			return true;
	//		} else {
	//			boolean exists = mongoOperations.exists(query(where("_id").is(launch.getProjectRef())), Project.class);
	//			if (!exists) {
	//				mongoOperations.remove(query(where("_id").is(launchRef)), Launch.class);
	//				return true;
	//			}
	//		}
	//		return false;
	//	}
	//
	//	public String generate(TestItem testItem) {
	//		String forEncoding = prepareForEncoding(testItem);
	//		return SECRET + DigestUtils.md5Hex(forEncoding);
	//	}
	//
	//	private String prepareForEncoding(TestItem testItem) {
	//		// using cache for launches
	//		Launch launch = launchCache.get(testItem.getLaunchRef(),
	//				k -> mongoOperations.findOne(launchQuery(testItem.getLaunchRef()), Launch.class)
	//		);
	//
	//		String launchName = launch.getName();
	//		String projectName = launch.getProjectRef();
	//
	//		List<String> pathNames = getPathNames(testItem.getPath());
	//		String itemName = testItem.getName();
	//		StringJoiner joiner = new StringJoiner(";");
	//		joiner.add(SECRET).add(projectName).add(launchName);
	//		if (!CollectionUtils.isEmpty(pathNames)) {
	//			joiner.add(pathNames.stream().collect(Collectors.joining(",")));
	//		}
	//		joiner.add(itemName);
	//		List<Parameter> parameters = testItem.getParameters();
	//		if (!CollectionUtils.isEmpty(parameters)) {
	//			joiner.add(parameters.stream()
	//					.map(parameter -> (!Strings.isNullOrEmpty(parameter.getKey()) ? parameter.getKey() + "=" : "") + parameter.getValue())
	//					.collect(Collectors.joining(",")));
	//		}
	//		return joiner.toString();
	//	}
	//
	//	private CloseableIterator<TestItem> getItemIterator(String checkpoint) {
	//		Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, "_id"));
	//		Query query = new Query().with(sort).noCursorTimeout();
	//		if (checkpoint != null) {
	//			query.addCriteria(where("_id").gte(new ObjectId(checkpoint)));
	//		}
	//		query.fields().include("name").include("path").include("launchRef").include("parameters");
	//		return mongoOperations.stream(query, TestItem.class);
	//	}
	//
	//	private List<String> getPathNames(List<String> path) {
	//		Map<String, String> names = testItemRepository.findPathNames(path);
	//		return path.stream().map(names::get).collect(Collectors.toList());
	//	}
	//
	//	private Query launchQuery(String launchId) {
	//		Query query = query((where("_id").is(launchId)));
	//		query.fields().include("name");
	//		query.fields().include("projectRef");
	//		return query;
	//	}
	//
	//	private Query testItemQuery() {
	//		Query query = new Query();
	//		query.fields().include("name").include("path").include("launchRef").include("parameters");
	//		return query;
	//	}
	//
	//	private String getLastCheckpoint() {
	//		DBObject checkpoint = mongoOperations.getCollection(COLLECTION).findOne(new BasicDBObject("_id", CHECKPOINT));
	//		return checkpoint == null ? null : (String) checkpoint.get(CHECKPOINT_ID);
	//	}
	//
	//	private void createCheckpoint(String logId) {
	//		BasicDBObject checkpoint = new BasicDBObject("_id", CHECKPOINT).append(CHECKPOINT_ID, logId);
	//		mongoOperations.getCollection(COLLECTION).save(checkpoint);
	//	}

}
