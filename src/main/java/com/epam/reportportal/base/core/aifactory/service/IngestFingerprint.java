package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Stable SHA-256 digest of an ingest request, used to tell a genuine retry (same
 * {@code Idempotency-Key}, same body) from a key reused for a different payload. Map entries are
 * ordered by key so the digest does not depend on the order a client happened to serialize them in.
 */
final class IngestFingerprint {

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .findAndRegisterModules()
      .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  private IngestFingerprint() {
  }

  static String of(PipelineIterationRQ rq) {
    try {
      var digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(MAPPER.writeValueAsBytes(rq)));
    } catch (JsonProcessingException | NoSuchAlgorithmException e) {
      throw new IllegalStateException("Cannot fingerprint ingest request", e);
    }
  }

  /**
   * Size of the payload once serialized, for the size limit on reports.
   */
  static long sizeInBytes(Object payload) {
    try {
      return MAPPER.writeValueAsBytes(payload).length;
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Cannot measure report payload", e);
    }
  }
}
