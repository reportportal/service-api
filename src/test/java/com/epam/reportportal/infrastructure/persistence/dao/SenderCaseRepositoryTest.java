package com.epam.reportportal.infrastructure.persistence.dao;

import com.epam.reportportal.ws.BaseMvcTest;
import com.epam.reportportal.infrastructure.persistence.entity.project.email.LaunchAttributeRule;
import com.epam.reportportal.infrastructure.persistence.entity.project.email.SenderCase;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/db/fill/sendercase/sender-case-fill.sql")
public class SenderCaseRepositoryTest extends BaseMvcTest {

  @Autowired
  private SenderCaseRepository senderCaseRepository;

  @Test
  void findAllByProjectId() {
    final List<SenderCase> senderCases = senderCaseRepository.findAllByProjectId(1L);
    Assertions.assertFalse(senderCases.isEmpty());
  }

  @Test
  void findAllByProjectIdAndRuleNameIgnoreCase() {
    final Optional<SenderCase> senderCases = senderCaseRepository.findByProjectIdAndTypeAndRuleNameIgnoreCase(
        1L, "email","rule1");
    Assertions.assertTrue(senderCases.isPresent());
  }

  @Test
  void deleteRecipients() {
    final int removed = senderCaseRepository.deleteRecipients(1L, Collections.singleton("first"));
    Assertions.assertEquals(1, removed);

    final SenderCase updated = senderCaseRepository.findById(1L).get();
    Assertions.assertEquals(1, updated.getRecipients().size());
    Assertions.assertFalse(updated.getRecipients().contains("first"));
  }

  @Test
  void saveAttributeRules() {
    final Optional<SenderCase> senderCases = senderCaseRepository.findByProjectIdAndTypeAndRuleNameIgnoreCase(
        1L, "email","rule1");
    Assertions.assertTrue(senderCases.isPresent());

    final SenderCase senderCaseDb = senderCases.get();
    final SenderCase senderCase = new SenderCase();
    senderCase.setId(senderCaseDb.getId());
    senderCase.setRuleName(senderCaseDb.getRuleName());
    senderCase.setProject(senderCase.getProject());
    senderCase.setEnabled(senderCaseDb.isEnabled());
    senderCase.setLaunchNames(senderCaseDb.getLaunchNames());
    senderCase.setSendCase(senderCaseDb.getSendCase());
    senderCase.setRecipients(senderCaseDb.getRecipients());
    final LaunchAttributeRule attributeRule = new LaunchAttributeRule();
    attributeRule.setValue("v1");
    attributeRule.setSenderCase(senderCase);
    Set<LaunchAttributeRule> rules = new HashSet<>();
    rules.add(attributeRule);
    senderCase.setLaunchAttributeRules(rules);
    senderCaseRepository.save(senderCase);
    final SenderCase found = senderCaseRepository.findById(senderCaseDb.getId()).get();

    Assertions.assertFalse(found.getLaunchAttributeRules().isEmpty());
  }
}
