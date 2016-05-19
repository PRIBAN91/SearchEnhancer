package com.enhancer.nlp;

import java.util.*;
import java.util.regex.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author PRITAM. Created as a singleton class for keeping it in server memory.
 *         This class is for Dynamic Bigram modeling. The frequency and counts
 *         are increase according to user searches.
 *
 */
public class DynamicBigram {

	private static DynamicBigram singleton = new DynamicBigram();
	public Set<String> samples;
	public ConcurrentHashMap<String, HashMap<String, Double>> bigramCount;
	public ConcurrentHashMap<String, Double> unigramCount;
	// The number of bigrams that occur x times
	public ConcurrentHashMap<Double, Double> numberOfBigramsWithCounts;
	// The size of the training set (# non-distinct words)
	public AtomicLong totBigrams;
	// To indicate start of a string
	public final String START = ":S";

	/**
	 * Private constructor for Singleton class, so that it can never be
	 * instantiated.
	 */
	private DynamicBigram() {
	}

	/**
	 * For getting the instance of already existing Singleton class.
	 */
	public static DynamicBigram getInstance() {
		return singleton;
	}

	/**
	 * This method is called on server startup for initializing training with
	 * training data set as initial corpus.
	 * 
	 * @param samples
	 *            as Product/Item data set
	 * 
	 */
	public synchronized void initializeTraining(Set<String> samples) {
		this.samples = samples;
		this.bigramCount = new ConcurrentHashMap<>();
		this.unigramCount = new ConcurrentHashMap<>();
		this.numberOfBigramsWithCounts = new ConcurrentHashMap<>();
		this.totBigrams = new AtomicLong(0);
		// For initial training set
		for (String sample : samples) {
			continueTraining(sample);
		}
	}

	/**
	 * This method is called to update the perplexity count in the main Bigram
	 * class after every 10 minutes.
	 * 
	 */
	public void updatePerplexity() {
		Bigram bigram = Bigram.getInstance();
		bigram.updatePerplexity(bigramCount, unigramCount, numberOfBigramsWithCounts, totBigrams.longValue());
	}

	/**
	 * This method is used to continue training with each successful user
	 * searches after initial training with the Product/Item set.
	 * 
	 * @param searchString
	 *            as the searched string
	 * 
	 */
	public synchronized void continueTraining(String searchString) {
		// Regex to match words (starting with optional apostrophe) or any
		// punctuation (except '(' and ')')
		String regexp = "('?\\w+|\\p{Punct}&&[^()])";
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(searchString);
		// Originally set to beginning-of-sentence marker
		String previousWord = START;
		// Get each word of sentence
		while (matcher.find()) {
			// Set unigram counts (for word1)
			double unigramCounter = 0.0;
			if (unigramCount.containsKey(previousWord)) {
				unigramCounter = unigramCount.get(previousWord);
			}
			unigramCount.put(previousWord, unigramCounter + 1.0);
			// Get the new match (word2)
			String match = matcher.group();
			HashMap<String, Double> innerCounts;
			// Get access to (or create) the count map for word1.
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
			// Set Bigram counts
			bigramCount.put(previousWord, innerCounts);
			// Increment the number of bigrams with the new count for
			// gt-smoothing
			if (!numberOfBigramsWithCounts.containsKey(count + 1.0)) {
				numberOfBigramsWithCounts.put(count + 1.0, 1.0);
			} else {
				numberOfBigramsWithCounts.put(count + 1.0, numberOfBigramsWithCounts.get(count + 1.0) + 1.0);
			}
			// Update previousWord
			previousWord = match;
		}
	}
}
