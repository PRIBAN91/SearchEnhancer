package com.enhancer.model;

/**
 * @author PRITAM. Created for Storing Bigram words in a list along with their
 *         perplexity score sorted in natural order.
 *
 */
public class BigramWord implements Comparable<BigramWord> {

	private String word;
	private Double prob;

	public BigramWord(String word, double prob) {
		this.word = word;
		this.prob = prob;
	}

	@Override
	public int compareTo(BigramWord o1) {
		if (this.prob.equals(o1.prob))
			return this.word.compareTo(o1.word);
		return (int) (this.prob.compareTo(o1.prob));
	}

	@Override
	public String toString() {
		return word;
	}

}