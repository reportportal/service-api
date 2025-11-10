package com.epam.reportportal.ws.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.reporting.StartLaunchRS;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class StartLaunchSerializerTest {

  private final ObjectMapper om = getObjectMapper();

  @Test
  public void testSerializerNotNull() throws JsonProcessingException {
    String json = om.writeValueAsString(getLaunchRs());
    assertEquals("{\"id\":\"1\"}", json, "Incorrect serialization result");
  }

  @Test
  public void testSerializerFull() throws JsonProcessingException {
    final StartLaunchRS startTestItem = getLaunchRs();
    startTestItem.setNumber(1L);
    String json = om.writeValueAsString(startTestItem);
    assertEquals("{\"id\":\"1\",\"number\":1}", json, "Incorrect serialization result");
  }

  private StartLaunchRS getLaunchRs() {
    StartLaunchRS rs = new StartLaunchRS();
    rs.setId("1");
    rs.setNumber(null);
    return rs;
  }

  private ObjectMapper getObjectMapper() {
    ObjectMapper om = new ObjectMapper();
    om.configure(SerializationFeature.INDENT_OUTPUT, false);
    return om;
  }

}
