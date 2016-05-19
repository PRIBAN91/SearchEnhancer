package com.enhancer.nlp;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author PRITAM. Created as a singleton class for keeping it in server memory.
 *         This class is for Trigram modeling. Same as Bigram Modeling, but
 *         without Good Turing smoothing.
 *
 */
public class DynamicTrigram {

	private static DynamicTrigram singleton = new DynamicTrigram();
	public Set<String> samples;
	public ConcurrentHashMap<String, HashMap<String, Double>> trigramCounts;
	public ConcurrentHashMap<String, Double> bigramCounts;

	/**
	 * Private constructor for Singleton class, so that it can never be
	 * instantiated.
	 */
	private DynamicTrigram() {
	}

	/**
	 * For getting the instance of already existing Singleton class.
	 */
	public static DynamicTrigram getInstance() {
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
		this.trigramCounts = new ConcurrentHashMap<String, HashMap<String, Double>>();
		this.bigramCounts = new ConcurrentHashMap<String, Double>();
		// Continue training as long as there are records in the training set
		for (String sample : samples) {
			continueTraining(sample.split(" "));
		}
	}

	/**
	 * This method is used for initial and continuous training of Trigram.
	 * Initial training is done with the initial Product list. Continued
	 * training is done with the help of user search frequencies.
	 * 
	 * @param sentence
	 *            individual sentence for Bigram training
	 * 
	 */
	public synchronized void continueTraining(String words[]) {
		int len = words.length;
		// Loop till string length - 2
		for (int i = 0; i < len - 2; i++) {
			// Concatenate first two words separated by a space
			String previousWord = words[i] + " " + words[i + 1];
			String word = words[i + 2];
			// Set bigram count (of concatenated words)
			double bigramCount = 0.0;
			if (bigramCounts.containsKey(previousWord)) {
				bigramCount = bigramCounts.get(previousWord);
			}
			bigramCounts.put(previousWord, bigramCount + 1.0);
			// Set trigram counts
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
	}

	/**
	 * Only unsmoothed probability is calculated in case of a Trigram, as the
	 * search narrows down to only a few items as more than two words comes in
	 * the searched string.
	 * 
	 * @param word1
	 *            as the first and second word concatenated by a space
	 * @param word2
	 *            as the third word after first and second
	 * 
	 */
	public double unsmoothedProbability(String word1, String word2) {
		return getTrigramCount(word1, word2) / bigramCounts.get(word1);
	}

	/**
	 * This method gets the Trigram count for a pair of words.
	 * 
	 * @param word1
	 *            as the first and second word concatenated by a space
	 * @param word2
	 *            as the third word after first and second
	 * 
	 */
	public double getTrigramCount(String word1, String word2) {
		if (trigramCounts.containsKey(word1))
			if (trigramCounts.get(word1).containsKey(word2))
				return trigramCounts.get(word1).get(word2);
			else
				return 0.0;
		else
			return 0.0;
	}

}
