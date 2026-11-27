package com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums;

/**
 * Who made a lifecycle change.
 */
public enum TmsLifecycleActorType {
  /** A person; the event carries the user. */
  USER,
  /** The Auto-Ready rule of the pipeline. */
  AUTO_READY,
  /** The AI agent (a fix round). */
  AGENT,
  /** Something without a person behind it, e.g. an upload by the pipeline's API key. */
  SYSTEM
}
