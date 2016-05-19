package com.enhancer.nlp;

import java.util.regex.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author PRITAM. Created as a singleton class for keeping it in server memory.
 *         This class is for Bigram modeling.
 *
 */
public class Bigram {

	private static Bigram singleton = new Bigram();
	public Set<String> samples;
	public ConcurrentHashMap<String, HashMap<String, Double>> bigramCounts;
	public ConcurrentHashMap<String, Double> unigramCounts;
	// The number of bigrams that occur x times
	public ConcurrentHashMap<Double, Double> numberOfBigramsWithCount;
	// The size of the training set (# non-distinct words)
	public long numTrainingBigrams;
	// To indicate start of a string
	public final String START = ":S";

	/**
	 * Private constructor for Singleton class, so that it can never be
	 * instantiated.
	 */
	private Bigram() {
	}

	/**
	 * For getting the instance of already existing Singleton class.
	 */
	public static Bigram getInstance() {
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
	public void initializeTraining(Set<String> samples) {
		this.samples = samples;
		this.bigramCounts = new ConcurrentHashMap<String, HashMap<String, Double>>();
		this.unigramCounts = new ConcurrentHashMap<String, Double>();
		this.numberOfBigramsWithCount = new ConcurrentHashMap<Double, Double>();
		this.numTrainingBigrams = 0;
		// Continue training as long as there are records in the training set
		for (String sample : samples) {
			training(sample);
		}
		// Calculating the Good Turing counts for all bigrams
		makeGoodTuringCounts();
		System.out.println("Good turing count done");
	}

	/**
	 * This method is used to update perplexity after every 10 minutes based on
	 * user search requests
	 * 
	 * @param bigramCounts
	 *            as the map with updated Bigram counts
	 * @param unigramCounts
	 *            as the map with updated Unigram counts
	 * @param numberOfBigramsWithCount
	 *            as the update number of Bigrams with certain count
	 * @param totBigrams
	 *            as the update total number of Bigrams
	 * 
	 */
	public void updatePerplexity(ConcurrentHashMap<String, HashMap<String, Double>> bigramCounts,
			ConcurrentHashMap<String, Double> unigramCounts, ConcurrentHashMap<Double, Double> numberOfBigramsWithCount,
			long totBigrams) {
		// Since by default parameter passing in constructor of a map created is
		// essentially a shallow copy, a deep copy is made externally
		this.bigramCounts = deepCopy(bigramCounts);
		this.unigramCounts = new ConcurrentHashMap<>(unigramCounts);
		this.numberOfBigramsWithCount = new ConcurrentHashMap<>(numberOfBigramsWithCount);
		this.numTrainingBigrams = totBigrams;
		makeGoodTuringCounts();
		System.out.println("Updating Good Turing count for Bigrams at : " + new Date());
	}

	/**
	 * This method is used for initial training of Bigram which each sentence in
	 * the Product/Item set.
	 * 
	 * @param sentence
	 *            individual sentence for Bigram training
	 * 
	 */
	public void training(String sentence) {
		// Regex to match words (starting with optional apostrophe) or any
		// punctuation (except '(' and ')')
		String regexp = "('?\\w+|\\p{Punct}&&[^()])";
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(sentence);
		// Originally set to beginning-of-sentence marker
		String previousWord = START;
		// Get each word of sentence
		while (matcher.find()) {
			double unigramCount = 0.0;
			// Set unigram counts (for word1)
			if (unigramCounts.containsKey(previousWord)) {
				unigramCount = unigramCounts.get(previousWord);
			}
			unigramCounts.put(previousWord, unigramCount + 1.0);
			// Get the new match (word2)
			String match = matcher.group();
			HashMap<String, Double> innerCounts;
			// Get access to (or create) the count map for word1.
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
			// Increment size of the training set for gt-smoothing
			numTrainingBigrams++;
			innerCounts.put(match, count + 1.0);
			// Set Bigram counts
			bigramCounts.put(previousWord, innerCounts);
			// Increment the number of bigrams with the new count for
			// gt-smoothing
			if (!numberOfBigramsWithCount.containsKey(count + 1.0)) {
				numberOfBigramsWithCount.putIfAbsent(count + 1.0, 1.0);
			} else {
				numberOfBigramsWithCount.put(count + 1.0, numberOfBigramsWithCount.get(count + 1.0) + 1.0);
			}
			// Update previousWord
			previousWord = match;
		}
	}

	/**
	 * This method gets the Bigram count for a pair of words.
	 * 
	 * @param word1
	 *            as the first word
	 * @param word2
	 *            as the second word occurring after the first one
	 * 
	 */
	public double getBigramCount(String word1, String word2) {
		if (bigramCounts.containsKey(word1))
			if (bigramCounts.get(word1).containsKey(word2))
				return bigramCounts.get(word1).get(word2);
			else
				return 0.0;
		else
			return 0.0;
	}

	/**
	 * This method calculates the perplexity of searched string.
	 * 
	 * @param searchStr
	 *            as searched string
	 * 
	 */
	public double perplexity(String searchStr) {
		float product = 1;
		int wordCount = 0;
		Stack<Double> products = new Stack<Double>();
		// Regex for splitting the searched string
		String regexp = "('?\\w+|\\p{Punct}&&[^()])";
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(searchStr);
		// Originally set to beginning-of-sentence marker
		String previousWord = START;
		// For each word
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

	/**
	 * This method calculates the Good Turing smoothed probability of two words.
	 * 
	 * @param word1
	 *            as first word
	 * @param word2
	 *            as the second word occurring after the first one
	 * 
	 */
	public double goodTuringSmoothedProbability(String word1, String word2) {
		// If this Bigram has occurred, return Good Turing probability
		double gtcount = getBigramCount(word1, word2);
		if (gtcount > 0.0)
			return gtcount / unigramCounts.get(word1);
		// Otherwise, return N1/N
		return numberOfBigramsWithCount.get(1.0) / numTrainingBigrams;
	}

	/**
	 * This method calculates the Good Turing counts based in Bigram counts.
	 */
	public void makeGoodTuringCounts() {
		// Generate Good Turing counts
		for (String word1 : bigramCounts.keySet()) {
			HashMap<String, Double> innerMap = bigramCounts.get(word1);
			double unigramCount = 0;
			for (String word2 : innerMap.keySet()) {
				double count = innerMap.get(word2);
				if (!numberOfBigramsWithCount.containsKey(count + 1)) {
					numberOfBigramsWithCount.putIfAbsent(count + 1, 0.0);
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

	/**
	 * This method makes a deep copy of the internal map within a map.
	 * 
	 * @param original
	 *            as the original ConcurrentHashmap
	 * 
	 */
	public <K1, K2, V> ConcurrentHashMap<K1, HashMap<K2, V>> deepCopy(ConcurrentHashMap<K1, HashMap<K2, V>> original) {
		ConcurrentHashMap<K1, HashMap<K2, V>> copy = new ConcurrentHashMap<K1, HashMap<K2, V>>();
		// Read each record and put it the the newly created map
		for (Entry<K1, HashMap<K2, V>> entry : original.entrySet()) {
			copy.put(entry.getKey(), new HashMap<K2, V>(entry.getValue()));
		}
		return copy;
	}

}
