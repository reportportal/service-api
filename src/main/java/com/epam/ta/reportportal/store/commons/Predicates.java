/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.store.commons;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author dzmitry_kavalets
 */
public class Predicates {

	private Predicates() {
		//statics only
	}

	public static <T> Predicate<T> notNull() {
		return Objects::nonNull;
	}

	public static <T> Predicate<T> isNull() {
		return Objects::isNull;
	}

	public static <T> Predicate<T> equalTo(T target) {
		return (target == null) ? Predicates.isNull() : t -> t.equals(target);
	}

	public static <T> Predicate<T> not(Predicate<T> predicate) {
		return item -> !predicate.test(item);
	}

	public static <T> Predicate<T> in(Collection<? extends T> target) {
		return target::contains;
	}

	public static <T> Predicate<T> alwaysFalse() {
		return t -> false;
	}

	public static <T> Predicate<T> and(List<? extends Predicate<? super T>> components) {
		return t -> components.stream().allMatch(predicate -> predicate.test(t));
	}

	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> or(Predicate<? super T>... components) {
		return t -> Stream.of(components).anyMatch(predicate -> predicate.test(t));
	}

	public static <T> Predicate<T> or(Iterable<? extends Predicate<? super T>> components) {
		return t -> StreamSupport.stream(components.spliterator(), false).anyMatch(predicate -> predicate.test(t));
	}

	public static Predicate<Optional<?>> isPresent() {
		return Optional::isPresent;
	}
}
