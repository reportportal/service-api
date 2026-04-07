package com.epam.ta.reportportal.core.launch.changes;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaunchModifiedMessage implements Serializable {

  private Long launchId;
}
