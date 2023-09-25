package com.epam.ta.reportportal.core.analyzer.pattern.handler;

import com.epam.ta.reportportal.core.analyzer.pattern.selector.PatternAnalysisSelector;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.PatternMatchedEvent;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItemPojo;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import com.epam.ta.reportportal.ws.converter.converters.PatternTemplateConverter;
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ItemsPatternAnalyzer {

  private final PatternTemplateRepository patternTemplateRepository;

  private final Map<PatternTemplateType, PatternAnalysisSelector> patternAnalysisSelectorMapping;

  private final TestItemRepository testItemRepository;

  private final MessageBus messageBus;

  public ItemsPatternAnalyzer(PatternTemplateRepository patternTemplateRepository,
      Map<PatternTemplateType, PatternAnalysisSelector> patternAnalysisSelectorMapping,
      TestItemRepository testItemRepository, MessageBus messageBus) {
    this.patternTemplateRepository = patternTemplateRepository;
    this.patternAnalysisSelectorMapping = patternAnalysisSelectorMapping;
    this.testItemRepository = testItemRepository;
    this.messageBus = messageBus;
  }

  public void analyzeItems(long projectId, long launchId, List<Long> itemIds) {
    patternTemplateRepository.findAllByProjectIdAndEnabled(
        projectId, true).forEach(pattern -> analyzeByPattern(pattern, launchId, itemIds));
  }

  private void analyzeByPattern(PatternTemplate pattern, Long launchId, List<Long> itemIds) {
    List<Long> filtered = filterAlreadyMatched(pattern, itemIds);
    PatternAnalysisSelector patternAnalysisSelector = patternAnalysisSelectorMapping.get(
        pattern.getTemplateType());
    List<Long> matchedIds = patternAnalysisSelector.selectItemsByPattern(launchId, filtered,
        pattern.getValue());
    if (!CollectionUtils.isEmpty(matchedIds)) {
      List<PatternTemplateTestItemPojo> patternTemplateTestItems = saveMatches(pattern, matchedIds);
      publishEvents(pattern, patternTemplateTestItems);
    }
  }

  private List<Long> filterAlreadyMatched(PatternTemplate pattern, List<Long> itemIds) {
    List<Long> alreadyMatched = patternTemplateRepository.findMatchedItemIdsIn(pattern.getId(),
        itemIds);
    return itemIds.stream().filter(id -> !alreadyMatched.contains(id))
        .collect(Collectors.toList());
  }

  private List<PatternTemplateTestItemPojo> saveMatches(PatternTemplate pattern,
      List<Long> matchedIds) {
    List<PatternTemplateTestItemPojo> patternTemplateTestItemPojos = convertToPojo(pattern,
        matchedIds);
    patternTemplateRepository.saveInBatch(patternTemplateTestItemPojos);
    return patternTemplateTestItemPojos;
  }

  private List<PatternTemplateTestItemPojo> convertToPojo(PatternTemplate patternTemplate,
      List<Long> itemIds) {
    return itemIds.stream()
        .map(itemId -> new PatternTemplateTestItemPojo(patternTemplate.getId(), itemId))
        .collect(Collectors.toList());
  }

  private void publishEvents(PatternTemplate patternTemplate,
      List<PatternTemplateTestItemPojo> patternTemplateTestItems) {
    final PatternTemplateActivityResource patternTemplateActivityResource = PatternTemplateConverter.TO_ACTIVITY_RESOURCE.apply(
        patternTemplate);
    patternTemplateTestItems.forEach(patternItem -> {
      Long testItemId = patternItem.getTestItemId();
      Optional<String> itemNameByItemId = testItemRepository.findItemNameByItemId(testItemId);
      PatternMatchedEvent patternMatchedEvent = new PatternMatchedEvent(
          itemNameByItemId.orElse(StringUtils.EMPTY),
          testItemId,
          patternTemplateActivityResource
      );
      messageBus.publishActivity(patternMatchedEvent);
    });
  }

}
