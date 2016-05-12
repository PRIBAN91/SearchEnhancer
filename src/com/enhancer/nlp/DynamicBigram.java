package com.enhancer.nlp;

import java.util.*;
import java.util.regex.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DynamicBigram {

	private static DynamicBigram singleton = new DynamicBigram();

	public Set<String> samples;
	public ConcurrentHashMap<String, HashMap<String, Double>> bigramCount;
	public ConcurrentHashMap<String, Double> unigramCount;
	public ConcurrentHashMap<Double, Double> numberOfBigramsWithCounts;
	public AtomicInteger totBigrams;
	public final String START = ":S";

	private DynamicBigram() {
	}

	public static DynamicBigram getInstance() {
		return singleton;
	}

	public synchronized void initializeTraining(Set<String> samples) {
		this.samples = samples;
		this.bigramCount = new ConcurrentHashMap<>();
		this.unigramCount = new ConcurrentHashMap<>();
		this.numberOfBigramsWithCounts = new ConcurrentHashMap<>();
		this.totBigrams = new AtomicInteger(0);

		for (String sample : samples) {
			continueTraining(sample);
		}
	}

	public void updatePerplexity() {
		Bigram bigram = Bigram.getInstance();
		bigram.updatePerplexity(bigramCount, unigramCount, numberOfBigramsWithCounts, totBigrams.intValue());
	}

	public synchronized void continueTraining(String searchString) {
		String regexp = "('?\\w+|\\p{Punct}&&[^()])";
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(searchString);
		String previousWord = START;
		while (matcher.find()) {
			double unigramCounter = 0.0;
			if (unigramCount.containsKey(previousWord)) {
				unigramCounter = unigramCount.get(previousWord);
			}
			unigramCount.put(previousWord, unigramCounter + 1.0);
			String match = matcher.group();
			HashMap<String, Double> innerCounts;
			if (bigramCount.containsKey(previousWord)) {
				innerCounts = bigramCount.get(previousWord);
			} else {
				innerCounts = new HashMap<String, Double>();
			}

			double count = 0.0;
			if (innerCounts.containsKey(match)) {
				count = innerCounts.get(match);
				// Decrement the number of bigrams with old count for
				// gt-smoothing
				numberOfBigramsWithCounts.put(count, numberOfBigramsWithCounts.get(count) - 1.0);
			}

			// Add to the size of the training set for gt-smoothing
			totBigrams.incrementAndGet();
			innerCounts.put(match, count + 1.0);
			bigramCount.put(previousWord, innerCounts);

			// Increment the number of bigrams with the new count for
			// gt-smoothing
			if (!numberOfBigramsWithCounts.containsKey(count + 1.0)) {
				numberOfBigramsWithCounts.put(count + 1.0, 1.0);
			} else {
				numberOfBigramsWithCounts.put(count + 1.0, numberOfBigramsWithCounts.get(count + 1.0) + 1.0);
			}
			previousWord = match;
		}
	}
}
