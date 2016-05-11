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
	public ConcurrentHashMap<Double, Double> numberOfBigramsWithCount;
	public int numTrainingBigrams;
	public final String START = ":S";

	private DynamicBigram() {
	}

	public static DynamicBigram getInstance() {
		return singleton;
	}

	public void initializeTraining(Set<String> samples) {
		this.samples = samples;
		this.bigramCounts = new ConcurrentHashMap<>();
		this.unigramCounts = new ConcurrentHashMap<>();
		this.secondWordMap = new ConcurrentHashMap<>();
		this.numberOfBigramsWithCount = new ConcurrentHashMap<>();
		this.numTrainingBigrams = 0;

		for (String sample : samples) {
			continueTraining(sample);
		}
		makeGoodTuringCounts();
		System.out.println("After making good turing count.");
	}

	public synchronized void continueTraining(String searchString) {
		String regexp = "('?\\w+|\\p{Punct}&&[^()])";
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(searchString);
		String previousWord = START;
		int counter = 0;
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
				// Decrement the number of bigrams with old count for
				// gt-smoothing
				numberOfBigramsWithCount.put(count, numberOfBigramsWithCount.get(count) - 1.0);
			}

			// Add to the size of the training set for gt-smoothing
			numTrainingBigrams++;
			counter++;
			innerCounts.put(match, count + 1.0);
			bigramCounts.put(previousWord, innerCounts);

			if (counter == 2) {
				if (secondWordMap.containsKey(match)) {
					HashSet<String> set = secondWordMap.get(match);
					set.add(previousWord);
					secondWordMap.put(match, set);
				} else {
					secondWordMap.put(match, new HashSet<String>());
				}
			}

			// Increment the number of bigrams with the new count for
			// gt-smoothing
			if (!numberOfBigramsWithCount.containsKey(count + 1.0)) {
				numberOfBigramsWithCount.put(count + 1.0, 1.0);
			} else {
				numberOfBigramsWithCount.put(count + 1.0, numberOfBigramsWithCount.get(count + 1.0) + 1.0);
			}
			previousWord = match;
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

	public double perplexity(String searchStr) {
		float product = 1;
		int wordCount = 0;
		Stack<Double> products = new Stack<Double>();
		String regexp = "('?\\w+|\\p{Punct}&&[^()])";
		Pattern pattern = Pattern.compile(regexp);

		Matcher matcher = pattern.matcher(searchStr);
		String previousWord = START;
		while (matcher.find()) {
			String match = matcher.group();

			products.add(goodTuringSmoothedProbability(previousWord, match));
			wordCount++;

			// Update previousWord
			previousWord = match;
		}

		// computing the necessary exponent
		double power = 1.0 / wordCount;

		// computing perplexity based on probabilities
		while (!products.empty()) {
			product *= Math.pow(products.pop(), power);
		}
		return 1 / product;
	}

	public double goodTuringSmoothedProbability(String word1, String word2) {

		// If this bigram has occurred, return good turing probability
		double gtcount = getBigramCount(word1, word2);
		if (gtcount > 0.0)
			return gtcount / unigramCounts.get(word1);

		// Otherwise, return N1/N
		return numberOfBigramsWithCount.get(1.0) / numTrainingBigrams;
	}

	public void makeGoodTuringCounts() {
		// Generate good turing counts
		for (String word1 : bigramCounts.keySet()) {
			HashMap<String, Double> innerMap = bigramCounts.get(word1);
			double unigramCount = 0;
			for (String word2 : innerMap.keySet()) {
				double count = innerMap.get(word2);
				if (!numberOfBigramsWithCount.containsKey(count + 1)) {
					numberOfBigramsWithCount.put(count + 1, 0.0);
				}
				// c* = (c+1) * N(c+1) / N(c)
				double newCount = (count + 1) * (numberOfBigramsWithCount.get(count + 1.0))
						/ (numberOfBigramsWithCount.get(count));
				innerMap.put(word2, newCount);
				unigramCount += newCount;
			}
			unigramCounts.put(word1, unigramCount);
		}
	}

}
