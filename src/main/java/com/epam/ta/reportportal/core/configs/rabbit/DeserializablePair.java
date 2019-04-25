package com.epam.ta.reportportal.core.configs.rabbit;

/**
 * Jackson's crazy to deserialize class w/o default constructor
 * To avoid mixins, we will create our own pair container
 *
 * @author Konstantin Antipin
 */
public class DeserializablePair<L, R> {
	private L left = null;

	private R right = null;

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
