package com.epam.ta.reportportal.ws.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.reporting.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.reporting.Issue;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class TicketsValidationTest {

  private static Validator validator;

  @BeforeAll
  public static void init() {
    Locale.setDefault(Locale.US);
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  public void finishIncorrectTicket() {
    FinishTestItemRQ finishTestItem = getFinishTestItem();

    Issue issue = new Issue();
    issue.setIssueType("ti001");

    Set<Issue.ExternalSystemIssue> tickets = new HashSet<>();
    tickets.add(new Issue.ExternalSystemIssue());
    issue.setExternalSystemIssues(tickets);

    finishTestItem.setIssue(issue);

    Set<ConstraintViolation<FinishTestItemRQ>> validate = validator.validate(finishTestItem);

    assertEquals(validate.size(), 4);
    validate.forEach(it -> assertEquals(it.getMessage(), "must not be blank"));
  }

  @Test
  public void finishWithCorrectTicket() {
    FinishTestItemRQ finishTestItem = getFinishTestItem();

    Issue issue = new Issue();
    issue.setIssueType("ti001");

    Set<Issue.ExternalSystemIssue> tickets = new HashSet<>();
    Issue.ExternalSystemIssue ticket = new Issue.ExternalSystemIssue();
    ticket.setTicketId("1234");
    ticket.setBtsUrl("btsUrl");
    ticket.setBtsProject("btsProject");
    ticket.setUrl("btsProject");
    tickets.add(ticket);
    issue.setExternalSystemIssues(tickets);

    finishTestItem.setIssue(issue);

    Set<ConstraintViolation<FinishTestItemRQ>> validate = validator.validate(finishTestItem);

    assertTrue(validate.isEmpty());
  }

  @Test
  public void linkEmptyCollections() {
    final LinkExternalIssueRQ linkExternalIssueRQ = new LinkExternalIssueRQ();
    linkExternalIssueRQ.setTestItemIds(new ArrayList<>());
    linkExternalIssueRQ.setIssues(new ArrayList<>());
    Set<ConstraintViolation<LinkExternalIssueRQ>> validate =
        validator.validate(linkExternalIssueRQ);

    assertEquals(validate.size(), 2);
    validate.forEach(it -> assertEquals(it.getMessage(), "must not be empty"));
  }

  @Test
  public void linkIncorrectTicket() {
    LinkExternalIssueRQ linkExternalIssueRQ = new LinkExternalIssueRQ();
    linkExternalIssueRQ.setTestItemIds(Lists.newArrayList(1L));

    Issue.ExternalSystemIssue ticket = new Issue.ExternalSystemIssue();
    linkExternalIssueRQ.setIssues(Lists.newArrayList(ticket));

    Set<ConstraintViolation<LinkExternalIssueRQ>> validate =
        validator.validate(linkExternalIssueRQ);

    System.out.println();

    assertEquals(validate.size(), 4);
    validate.forEach(it -> assertEquals(it.getMessage(), "must not be blank"));
  }

  @Test
  public void linkWithCorrectTicket() {
    LinkExternalIssueRQ linkExternalIssueRQ = new LinkExternalIssueRQ();
    linkExternalIssueRQ.setTestItemIds(Lists.newArrayList(1L));

    Issue.ExternalSystemIssue ticket = new Issue.ExternalSystemIssue();
    ticket.setTicketId("1234");
    ticket.setBtsUrl("btsUrl");
    ticket.setBtsProject("btsProject");
    ticket.setUrl("btsProject");
    linkExternalIssueRQ.setIssues(Lists.newArrayList(ticket));

    Set<ConstraintViolation<LinkExternalIssueRQ>> validate =
        validator.validate(linkExternalIssueRQ);

    assertTrue(validate.isEmpty());
  }

  private FinishTestItemRQ getFinishTestItem() {
    FinishTestItemRQ finishTestItemRQ = new FinishTestItemRQ();
    finishTestItemRQ.setStatus("PASSED");
    finishTestItemRQ.setEndTime(new Date());
    return finishTestItemRQ;
  }

}
