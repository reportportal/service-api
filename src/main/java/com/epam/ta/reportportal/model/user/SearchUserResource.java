package com.epam.ta.reportportal.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class SearchUserResource {

  @JsonProperty(value = "id", required = true)
  private Long id;

  @JsonProperty
  private UUID uuid;

  @JsonProperty
  private String externalId;

  @JsonProperty
  private boolean active;

  @JsonProperty(value = "login", required = true)
  private String login;

  @JsonProperty(value = "email", required = true)
  private String email;

  @JsonProperty(value = "fullName")
  private String fullName;

}
