package com.epam.ta.reportportal.ws.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.InstanceRole;
import com.epam.reportportal.api.model.NewUserRequest;
import com.epam.reportportal.api.model.NewUserRequest.AccountTypeEnum;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/db/user/user-fill.sql")
public class GeneratedUserControllerTest extends BaseMvcTest {

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserRepository userRepository;

  @Test
  void createdUserByIdentityProvider() throws Exception {
    NewUserRequest rq = new NewUserRequest();
    rq.setFullName("Test User");
    rq.setEmail("test@test.com");
    rq.setInstanceRole(InstanceRole.USER);
    rq.setActive(true);
    rq.setAccountType(AccountTypeEnum.SCIM);

    mockMvc.perform(post("/users").with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(rq)))
        .andExpect(status().isCreated())
        .andReturn();

    var user = userRepository.findByEmail(rq.getEmail());
    assertTrue(user.isPresent());
    assertEquals(UserType.SCIM, user.get().getUserType());
    assertNull(user.get().getPassword());
  }

}
