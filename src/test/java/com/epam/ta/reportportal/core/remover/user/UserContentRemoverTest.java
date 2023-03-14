/*
 * Copyright 2022 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.remover.user;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.entity.user.User;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
class UserContentRemoverTest {

  private final UserPhotoRemover userPhotoRemover = mock(UserPhotoRemover.class);
  private final UserWidgetRemover userWidgetRemover = mock(UserWidgetRemover.class);
  private final ContentRemover<User> userContentRemover = new UserContentRemover(
      List.of(userWidgetRemover, userPhotoRemover));

  @Test
  public void removeTest() {
    User user = mock(User.class);

    userContentRemover.remove(user);

    verify(userWidgetRemover, times(1)).remove(eq(user));
    verify(userPhotoRemover, times(1)).remove(eq(user));
  }

}
