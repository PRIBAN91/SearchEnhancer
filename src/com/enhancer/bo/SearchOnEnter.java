package com.enhancer.bo;

import java.util.*;
import org.apache.commons.lang3.StringUtils;
import com.enhancer.model.Correctspell;
import com.enhancer.nlp.*;

/**
 * @author PRITAM. Created for internal logic of Enter press event.
 *
 */
public class SearchOnEnter {

	/**
	 * This method checks Damerau Lavenstein distance between searched string
	 * and list of suggestions
	 * 
	 * @param list
	 *            as Suggestion List,
	 * @param str
	 *            as Searched String,
	 * @param len
	 *            as Length of Searched String,
	 * @param lim
	 *            as the Number of Suggestions needed
	 *
	 */
	public List<String> findKeywordSuggestion(List<String> list, String str, int len, int lim) {
		SpellAutoCorrect luw = new SpellAutoCorrect();
		// Calculate the Edit Distance
		TreeSet<Correctspell> ts = luw.calculateEditDistance(list, str, len);
		int count = 0;
		list = new ArrayList<String>();
		for (Correctspell csp : ts) {
			list.add(csp.getS());
			count++;
			// Break when limit is reached
			if (count == lim)
				break;
		}
		return list;
	}

	/**
	 * This method checks Lavenstein distance between searched string and the
	 * complete list of products/items
	 * 
	 * @param list
	 *            as Suggestion List,
	 * @param arr
	 *            as String array for all products,
	 * @param str
	 *            as Searched String,
	 * @param len
	 *            as Length of Searched String,
	 * @param lim
	 *            as the Number of Suggestions needed
	 *
	 */
	public List<String> findUnkownKeyword(List<String> list, String arr[], String str, int len, int lim) {
		SpellAutoCorrect luw = new SpellAutoCorrect();
		// Calculate the Edit distance on the entire list of products
		TreeSet<Correctspell> ts = luw.calculateEditDistanceArr(arr, str, len, lim >> 1, 1200);
		int count = 0;
		for (Correctspell csp : ts) {
			list.add(csp.getS());
			count++;
			// Take the top suggestions
			if (count == lim)
				break;
		}
		return list;
	}

	/**
	 * This method updates the DynamicBigram and Trigram modeling for
	 * incrementing the frequency. This will calculate enhanced maximum
	 * likelihood estimate. This is only for definitive searches.
	 * 
	 * @param str
	 *            as Searched String
	 *
	 */
	public void updateNgrams(String str) {
		DynamicBigram db = DynamicBigram.getInstance();
		// Update dynamic bigram with user input
		db.continueTraining(str);
		if (StringUtils.countMatches(str, " ") >= 2) {
			DynamicTrigram dt = DynamicTrigram.getInstance();
			// Update the Trigram
			dt.continueTraining(str.split(" "));
		}
	}

	/**
	 * This method checks space content in Suggestion list.
	 * 
	 * @param list
	 *            as Suggestion list
	 *
	 */
	public boolean isSpacePresentInSugg(List<String> list) {
		for (String s : list)
			if (s.contains(" "))
				return true;
		return false;
	}

}
