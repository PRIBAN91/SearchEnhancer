package com.enhancer.model;

public class CorrectSpelling implements Comparable<CorrectSpelling> {

	private String str;
	private double dist;

	public CorrectSpelling(String str, double dist) {
		this.str = str;
		this.dist = dist;
	}

	public String getStr() {
		return str;
	}

	public double getDist() {
		return dist;
	}

	@Override
	public int compareTo(CorrectSpelling c) {
		if (this.dist < c.dist)
			return -1;
		else if (c.dist < this.dist)
			return 1;
		else
			return (this.str.compareTo(c.str));
	}

}
