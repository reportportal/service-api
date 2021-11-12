/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.pipeline;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PipelineConstructor<T> {

	private final List<PipelinePartProvider<T>> providers;

	public PipelineConstructor(List<PipelinePartProvider<T>> providers) {
		this.providers = providers;
	}

	public List<PipelinePart> construct(T source) {
		return providers.stream().map(p -> p.provide(source)).collect(Collectors.toList());
	}
}
