package com.enhancer.nlp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;
import java.util.*;

public class DynamicBigram {

	private static DynamicBigram singleton = new DynamicBigram();
	public Set<String> samples;
	public ConcurrentHashMap<String, HashMap<String, Double>> bigramCounts;
	public ConcurrentHashMap<String, Double> unigramCounts;
	public ConcurrentHashMap<String, HashSet<String>> secondWordMap;
	public final String START = ":S";

	private DynamicBigram() {
	}

	public static DynamicBigram getInstance() {
		return singleton;
	}

	public void initializeTraining(Set<String> samples) {
		this.samples = samples;
		this.bigramCounts = new ConcurrentHashMap<String, HashMap<String, Double>>();
		this.unigramCounts = new ConcurrentHashMap<String, Double>();
		this.secondWordMap = new ConcurrentHashMap<String, HashSet<String>>();

		for (String sample : samples) {
			continueTraining(sample);
		}
	}

	public synchronized void continueTraining(String searchStr) {
		try {
			String regexp = "('?\\w+|\\p{Punct}&&[^()])";
			Pattern pattern = Pattern.compile(regexp);
			Matcher matcher = pattern.matcher(searchStr);
			String previousWord = START;
			while (matcher.find()) {
				double unigramCount = 0.0;
				if (unigramCounts.containsKey(previousWord)) {
					unigramCount = unigramCounts.get(previousWord);
				}
				unigramCounts.put(previousWord, unigramCount + 1.0);
				String match = matcher.group();
				HashMap<String, Double> innerCounts;
				if (bigramCounts.containsKey(previousWord)) {
					innerCounts = bigramCounts.get(previousWord);
				} else {
					innerCounts = new HashMap<String, Double>();
				}
				double count = 0.0;
				if (innerCounts.containsKey(match)) {
					count = innerCounts.get(match);
				}
				innerCounts.put(match, count + 1.0);
				bigramCounts.put(previousWord, innerCounts);

				HashSet<String> set = new HashSet<>();
				if (!previousWord.equals(START)) {
					if (secondWordMap.containsKey(match)) {
						set = secondWordMap.get(match);
						set.add(previousWord);
						secondWordMap.put(match, set);
					} else {
						set.add(previousWord);
						secondWordMap.put(match, set);
					}
				}
				previousWord = match;
			}
		} catch (Exception e) {
			System.out.println("Exception in updating count of bigrams.");
			e.printStackTrace();
		}
	}

	public double unsmoothedProbability(String word1, String word2) {
		return getBigramCount(word1, word2) / unigramCounts.get(word1);

	}

	public double getBigramCount(String word1, String word2) {
		if (bigramCounts.containsKey(word1))
			if (bigramCounts.get(word1).containsKey(word2))
				return bigramCounts.get(word1).get(word2);
			else
				return 0.0;
		else
			return 0.0;
	}

	public double secondWordProbability(List<String> list, String word) {
		double sum = 0, tot = 0, prob = 0;
		HashSet<String> hs = secondWordMap.get(word);
		if (hs != null) {
			for (String str : hs) {
				sum += getBigramCount(str, word);
				tot += unigramCounts.get(str);
				list.add(str);
			}
			prob = sum / tot;
			System.out.println(word + " : " + prob);
		}
		return prob;
	}

	public void showCounts() {
		for (String word1 : bigramCounts.keySet()) {
			for (String word2 : bigramCounts.get(word1).keySet()) {
				System.out.println(word1 + " " + word2 + ": " + bigramCounts.get(word1).get(word2));
			}
		}
	}

}
