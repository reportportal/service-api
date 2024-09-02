package com.epam.ta.reportportal.util;

import com.github.slugify.Slugify;

public class SlugifyUtils {

  private SlugifyUtils() {
  }

  public static String slugify(String inputStr) {
    var slug = Slugify.builder()
        .underscoreSeparator(false)
        .build();
    return slug.slugify(inputStr);
  }

}
