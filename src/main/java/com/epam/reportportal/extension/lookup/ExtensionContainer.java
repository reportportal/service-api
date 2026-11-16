package com.epam.reportportal.extension.lookup;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
@Deprecated(forRemoval = true)
public class ExtensionContainer<T> {

  private final Map<String, T> extensions;

  public ExtensionContainer() {
    this.extensions = new LinkedHashMap<>();
  }

  public void add(String key, T extension) {
    extensions.put(key, extension);
  }

  public void remove(String key) {
    extensions.remove(key);
  }

  public T get(String key) {
    return extensions.get(key);
  }

}
