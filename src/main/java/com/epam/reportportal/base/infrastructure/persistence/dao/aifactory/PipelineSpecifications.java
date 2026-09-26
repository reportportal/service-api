package com.epam.reportportal.base.infrastructure.persistence.dao.aifactory;

import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Search and filter predicates for the pipeline and iteration list endpoints. Iterations are searched
 * through the text derived from their attributes and filtered by exact attribute through their attribute
 * index, so nothing here knows which attributes exist.
 */
public final class PipelineSpecifications {

  private static final char ESCAPE = '\\';

  private PipelineSpecifications() {
  }

  /**
   * Pipelines of a project that match both given criteria; a blank criterion matches every pipeline.
   * <ul>
   *   <li>{@code search}: by name, or because one of their iterations matches (see {@link #iterationMatches});</li>
   *   <li>{@code name}: by name only, as a case-insensitive substring. Example: {@code "gener"} finds
   *   "Test case generation" and not a pipeline whose iteration only mentions "generated".</li>
   * </ul>
   */
  public static Specification<Pipeline> pipelinesOfProject(Long projectId, String search, String name) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("project").get("id"), projectId));
      var nameTerm = normalize(name);
      if (nameTerm != null) {
        predicates.add(contains(cb, root.get("name"), nameTerm));
      }
      var term = normalize(search);
      if (term != null) {
        var iterations = query.subquery(Long.class);
        var iteration = iterations.from(PipelineIteration.class);
        iterations.select(iteration.get("id"))
            .where(cb.equal(iteration.get("pipeline"), root), iterationMatches(iteration, cb, term));
        predicates.add(cb.or(contains(cb, root.get("name"), term), cb.exists(iterations)));
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    };
  }

  /**
   * Iterations of one pipeline that satisfy every given criterion; absent criteria are skipped.
   *
   * @param attributes exact {@code key -> value} filters, all of which must hold
   */
  public static Specification<PipelineIteration> iterationsOfPipeline(Long pipelineId, String search,
      Collection<StatusCategory> statuses, Map<String, String> attributes) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("pipeline").get("id"), pipelineId));
      var term = normalize(search);
      if (term != null) {
        predicates.add(iterationMatches(root, cb, term));
      }
      if (statuses != null && !statuses.isEmpty()) {
        predicates.add(root.get("statusCategory").in(statuses));
      }
      if (attributes != null) {
        attributes.forEach((key, value) -> {
          if (StringUtils.hasText(key)) {
            var token = PipelineAttribute.indexToken(key, value);
            predicates.add(cb.like(root.get("attributeIndex"), "%" + escapeLike(token) + "%", ESCAPE));
          }
        });
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    };
  }

  private static Predicate iterationMatches(Root<PipelineIteration> iteration, CriteriaBuilder cb, String term) {
    var number = term.startsWith("#") ? term.substring(1) : term;
    var predicates = new ArrayList<Predicate>();
    predicates.add(contains(cb, iteration.get("searchText"), term));
    parseIterationNumber(number)
        .ifPresent(n -> predicates.add(cb.equal(iteration.get("iterationNumber"), n)));
    return cb.or(predicates.toArray(Predicate[]::new));
  }

  private static Optional<Integer> parseIterationNumber(String value) {
    if (value.isEmpty() || value.length() > 9 || !value.chars().allMatch(Character::isDigit)) {
      return Optional.empty();
    }
    return Optional.of(Integer.parseInt(value));
  }

  private static Predicate contains(CriteriaBuilder cb, Expression<String> column, String term) {
    return cb.like(cb.lower(column), "%" + escapeLike(term.toLowerCase()) + "%", ESCAPE);
  }

  private static String normalize(String search) {
    return StringUtils.hasText(search) ? search.trim() : null;
  }

  private static String escapeLike(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
