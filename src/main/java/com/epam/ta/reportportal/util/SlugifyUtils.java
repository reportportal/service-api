package com.epam.ta.reportportal.util;

import com.github.slugify.Slugify;

public class SlugifyUtils {

  private SlugifyUtils() {
  }

  public static String slugify(String inputStr) {
    var slg = Slugify.builder().build();
    return slg.slugify(inputStr);
  }

}
