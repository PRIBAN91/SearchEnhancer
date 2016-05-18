package com.enhancer.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author PRITAM. Created for Hibernate ORM mapping with table. You can change
 *         the table and column name here according to your wish.
 *
 */
@Entity
@Table(name = "wordlist")
public class Wordlist implements Comparable<Wordlist> {

	@Id
	private int sequence;
	private String words;

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public String getWords() {
		return words.toLowerCase().trim();
	}

	public void setWords(String words) {
		this.words = words;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + sequence;
		result = prime * result + ((words == null) ? 0 : words.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Wordlist other = (Wordlist) obj;
		if (sequence != other.sequence)
			return false;
		if (words == null) {
			if (other.words != null)
				return false;
		} else if (!words.equals(other.words))
			return false;
		return true;
	}

	@Override
	public int compareTo(Wordlist o) {
		return (int) (this.words.compareTo(o.words));
	}

}
