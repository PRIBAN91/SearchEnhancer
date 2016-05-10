package com.enhancer.model;

public class WordProbability implements Comparable<WordProbability> {

	String word;
	Double prob;

	public WordProbability(String word, double prob) {
		this.word = word;
		this.prob = prob;
	}

	@Override
	public int compareTo(WordProbability o1) {
		if (this.prob.equals(o1.prob))
			return this.word.compareTo(o1.word);
		return (int) (o1.prob.compareTo(this.prob));
	}

	@Override
	public String toString() {
		return word;
	}

}