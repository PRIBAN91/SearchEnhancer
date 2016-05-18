package com.enhancer.model;

/**
 * @author PRITAM. Created for Storing Trigram words in a list along with their
 *         probability score sorted in descending order.
 *
 */
public class TrigramWord implements Comparable<TrigramWord> {

	private String word;
	private Double prob;

	public TrigramWord(String word, double prob) {
		this.word = word;
		this.prob = prob;
	}

	@Override
	public int compareTo(TrigramWord o1) {
		if (this.prob.equals(o1.prob))
			return this.word.compareTo(o1.word);
		return (int) (o1.prob.compareTo(this.prob));
	}

	@Override
	public String toString() {
		return word;
	}

}