package com.enhancer.nlp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

public class DynamicTrigram {

	private static DynamicTrigram singleton = new DynamicTrigram();
	public Set<String> samples;
	public ConcurrentHashMap<String, HashMap<String, Double>> trigramCounts;
	public ConcurrentHashMap<String, Double> bigramCounts;

	private DynamicTrigram() {
	}

	public static DynamicTrigram getInstance() {
		return singleton;
	}

	public void initializeTraining(Set<String> samples) {
		this.samples = samples;
		this.trigramCounts = new ConcurrentHashMap<String, HashMap<String, Double>>();
		this.bigramCounts = new ConcurrentHashMap<String, Double>();

		for (String sample : samples) {
			continueTraining(sample.split(" "));
		}
	}

	public synchronized void continueTraining(String words[]) {
		try {
			int len = words.length;
			for (int i = 0; i < len - 2; i++) {
				String previousWord = words[i] + " " + words[i + 1];
				String word = words[i + 2];
				double bigramCount = 0.0;
				if (bigramCounts.containsKey(previousWord)) {
					bigramCount = bigramCounts.get(previousWord);
				}
				bigramCounts.put(previousWord, bigramCount + 1.0);
				HashMap<String, Double> innerCounts;
				if (trigramCounts.containsKey(previousWord)) {
					innerCounts = trigramCounts.get(previousWord);
				} else {
					innerCounts = new HashMap<String, Double>();
				}
				double count = 0.0;
				if (innerCounts.containsKey(word)) {
					count = innerCounts.get(word);
				}
				innerCounts.put(word, count + 1.0);
				trigramCounts.put(previousWord, innerCounts);
			}
		} catch (Exception e) {
			System.out.println("Exception in updating count of bigrams.");
			e.printStackTrace();
		}
	}

	public double unsmoothedProbability(String word1, String word2) {
		return getTrigramCount(word1, word2) / bigramCounts.get(word1);

	}

	public double getTrigramCount(String word1, String word2) {
		if (trigramCounts.containsKey(word1))
			if (trigramCounts.get(word1).containsKey(word2))
				return trigramCounts.get(word1).get(word2);
			else
				return 0.0;
		else
			return 0.0;
	}

	public void showCounts() {
		for (String word1 : trigramCounts.keySet()) {
			for (String word2 : trigramCounts.get(word1).keySet()) {
				System.out.println(word1 + " " + word2 + ": " + trigramCounts.get(word1).get(word2));
			}
		}
	}

}
