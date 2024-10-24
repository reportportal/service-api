package com.epam.ta.reportportal.reporting.async.consumer;

import static com.epam.ta.reportportal.reporting.async.config.MessageHeaders.REQUEST_TIME;
import static com.epam.ta.reportportal.reporting.async.config.MessageHeaders.REQUEST_TYPE;

import java.time.Instant;
import java.util.Comparator;
import org.springframework.amqp.core.Message;

public class MessageComparator implements Comparator<Message> {

  @Override
  public int compare(Message o1, Message o2) {
    int typeComparison = compareTypes(getValue(o1, REQUEST_TYPE), getValue(o2, REQUEST_TYPE));
    if (typeComparison != 0) {
      return typeComparison;
    }
    return Long.valueOf(getValue(o1, REQUEST_TIME)).compareTo(Long.valueOf(getValue(o2, REQUEST_TIME)));
  }

  private String getValue(Message message, String field) {
    return (String) message.getMessageProperties().getHeaders().get(field);
  }

  private int compareTypes(String type1, String type2) {
    int order1 = typeOrder(type1);
    int order2 = typeOrder(type2);

    return Integer.compare(order1, order2);
  }

  private int typeOrder(String type) {
    return switch (type) {
      case "START_LAUNCH" -> 1;
      case "START_ITEM" -> 2;
      case "LOG" -> 3;
      case "FINISH_ITEM" -> 4;
      case "FINISH_LAUNCH" -> 5;
      default -> throw new IllegalArgumentException("Unknown type: " + type);
    };
  }
}
