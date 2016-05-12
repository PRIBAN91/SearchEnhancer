package com.enhancer.nlp;

import java.util.*;
import java.util.regex.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DynamicBigram {

	private static DynamicBigram singleton = new DynamicBigram();

	public Set<String> samples;
	public ConcurrentHashMap<String, HashMap<String, Double>> bigramCounts;
	public ConcurrentHashMap<String, Double> unigramCounts;
	public ConcurrentHashMap<Double, Double> numberOfBigramsWithCount;
	public AtomicInteger totBigrams;
	public final String START = ":S";

	private DynamicBigram() {
	}

	public static DynamicBigram getInstance() {
		return singleton;
	}

	public synchronized void initializeTraining(Set<String> samples) {
		this.samples = samples;
		this.bigramCounts = new ConcurrentHashMap<>();
		this.unigramCounts = new ConcurrentHashMap<>();
		this.numberOfBigramsWithCount = new ConcurrentHashMap<>();
		this.totBigrams = new AtomicInteger(0);

		for (String sample : samples) {
			continueTraining(sample);
		}
	}

	public void updatePerplexity() {
		Bigram bigram = Bigram.getInstance();
		bigram.updatePerplexity(bigramCounts, unigramCounts, numberOfBigramsWithCount, totBigrams.intValue());
	}

	public synchronized void continueTraining(String searchString) {
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
			totBigrams.incrementAndGet();
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
}
