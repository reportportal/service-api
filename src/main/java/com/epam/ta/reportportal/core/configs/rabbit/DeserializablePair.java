/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.configs.rabbit;

/**
 * Jackson's crazy to deserialize class w/o default constructor
 * To avoid mixins, we will create our own pair container
 *
 * @author Konstantin Antipin
 */
public class DeserializablePair<L, R> {
	private L left;

	private R right;

	public DeserializablePair() {}

	private DeserializablePair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	public static <L, R> DeserializablePair<L, R> of(L left, R right) {
		return new DeserializablePair(left, right);
	}
}
