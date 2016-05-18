package com.enhancer.model;

/**
 * @author PRITAM. Created for storing Words and their corresponding Edit
 *         Distance in their natural sorted order.
 *
 */
public class Correctspell implements Comparable<Correctspell> {

	private String s;
	private int dist;

	public Correctspell(String s, int dist) {
		this.s = s;
		this.dist = dist;
	}

	public String getS() {
		return s;
	}

	public int getDist() {
		return dist;
	}

	@Override
	public int compareTo(Correctspell c) {
		return (int) ((this.dist - c.dist) == 0 ? this.s.compareTo(c.s) : (this.dist - c.dist));
	}

}
