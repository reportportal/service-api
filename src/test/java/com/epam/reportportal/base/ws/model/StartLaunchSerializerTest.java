package com.epam.reportportal.base.ws.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.base.reporting.StartLaunchRS;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.SerializationFeature;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class StartLaunchSerializerTest {

  private final JsonMapper om = getObjectMapper();

  @Test
  public void testSerializerNotNull() {
    String json = om.writeValueAsString(getLaunchRs());
    assertEquals("{\"id\":\"1\"}", json, "Incorrect serialization result");
  }

  @Test
  public void testSerializerFull() {
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

  private JsonMapper getObjectMapper() {
    return JsonMapper.builder()
        .disable(SerializationFeature.INDENT_OUTPUT)
        .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .build();
  }

}
