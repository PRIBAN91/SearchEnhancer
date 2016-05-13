package com.enhancer.nlp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;
import java.util.*;
import java.util.Map.Entry;

public class Bigram {

	private static Bigram singleton = new Bigram();
	public Set<String> samples;
	public ConcurrentHashMap<String, HashMap<String, Double>> bigramCounts;
	public ConcurrentHashMap<String, Double> unigramCounts;
	public ConcurrentHashMap<String, HashSet<String>> secondWordMap;
	public ConcurrentHashMap<Double, Double> numberOfBigramsWithCount;
	public long numTrainingBigrams;
	public final String START = ":S";

	private Bigram() {
	}

	public static Bigram getInstance() {
		return singleton;
	}

	public void initializeTraining(Set<String> samples) {
		this.samples = samples;
		this.bigramCounts = new ConcurrentHashMap<String, HashMap<String, Double>>();
		this.unigramCounts = new ConcurrentHashMap<String, Double>();
		this.numberOfBigramsWithCount = new ConcurrentHashMap<Double, Double>();
		this.numTrainingBigrams = 0;

		for (String sample : samples) {
			training(sample);
		}
		makeGoodTuringCounts();
		System.out.println("Good turing count done");
	}

	public void updatePerplexity(ConcurrentHashMap<String, HashMap<String, Double>> bigramCounts,
			ConcurrentHashMap<String, Double> unigramCounts, ConcurrentHashMap<Double, Double> numberOfBigramsWithCount,
			long totBigrams) {

		this.bigramCounts = deepCopy(bigramCounts);
		this.unigramCounts = new ConcurrentHashMap<>(unigramCounts);
		this.numberOfBigramsWithCount = new ConcurrentHashMap<>(numberOfBigramsWithCount);
		this.numTrainingBigrams = totBigrams;

		makeGoodTuringCounts();
		System.out.println("Updating Good Turing count for Bigrams at : " + new Date());
	}

	public void training(String searchString) {
		String regexp = "('?\\w+|\\p{Punct}&&[^()])";
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(searchString);
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
				// Decrement the number of bigrams with old count for
				// gt-smoothing
				numberOfBigramsWithCount.put(count, numberOfBigramsWithCount.get(count) - 1.0);
			}

			// Add to the size of the training set for gt-smoothing
			numTrainingBigrams++;
			innerCounts.put(match, count + 1.0);
			bigramCounts.put(previousWord, innerCounts);

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

	public double getBigramCount(String word1, String word2) {
		if (bigramCounts.containsKey(word1))
			if (bigramCounts.get(word1).containsKey(word2))
				return bigramCounts.get(word1).get(word2);
			else
				return 0.0;
		else
			return 0.0;
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

	public <K1, K2, V> ConcurrentHashMap<K1, HashMap<K2, V>> deepCopy(ConcurrentHashMap<K1, HashMap<K2, V>> original) {
		ConcurrentHashMap<K1, HashMap<K2, V>> copy = new ConcurrentHashMap<K1, HashMap<K2, V>>();
		for (Entry<K1, HashMap<K2, V>> entry : original.entrySet()) {
			copy.put(entry.getKey(), new HashMap<K2, V>(entry.getValue()));
		}
		return copy;
	}

}
