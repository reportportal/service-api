package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;

/**
 * Outcome of an ingest call.
 *
 * @param iteration the created (or, on a replay, previously created) iteration
 * @param replayed  true when an earlier request with the same Idempotency-Key was answered again
 *                  instead of creating anything
 */
public record PipelineIterationIngestResult(PipelineIterationDetailRS iteration, boolean replayed) {
}
