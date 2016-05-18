package com.enhancer.bo;

import java.util.*;
import com.enhancer.model.Correctspell;
import com.enhancer.nlp.Calculations;
import com.enhancer.nlp.SpellAutoCorrect;
import com.enhancer.util.Trie;

/**
 * @author PRITAM. Created for internal logic of each key press event.
 *
 */
public class SearchOnKeyup {

	/**
	 * This method checks Damerau Lavenstein distance between searched string
	 * and previous list of suggestions
	 * 
	 * @param list
	 *            as Suggestion List,
	 * @param prevSuggChk
	 *            as flag for previous suggestion check,
	 * @param searchStr
	 *            as the Searched String
	 * 
	 */
	public List<String> checkPrevSuggestion(List<String> list, boolean prevSuggChk, String searchStr) {
		// Check for whether the list is empty or previous keyup was checked
		if (!list.isEmpty() && !prevSuggChk) {
			System.out.println("In spell check of previous suggestions.");
			SpellAutoCorrect luw = new SpellAutoCorrect();
			// Check for Damerau Lavenstein distance on previous list
			TreeSet<Correctspell> ts = luw.calculateEditDistance(list, searchStr, searchStr.length());
			list = new ArrayList<String>();
			// Put in the list according to ascending order of distances
			for (Correctspell csp : ts)
				list.add(csp.getS());
			prevSuggChk = true;
		} else {
			prevSuggChk = false;
			list = new ArrayList<>();
		}
		return list;
	}

	/**
	 * This method checks contains method of String class over the complete list
	 * of products/items
	 * 
	 * @param list
	 *            as Suggestion List,
	 * @param arr
	 *            as String array for all products
	 * @param str
	 *            as Searched String,
	 * @param needed
	 *            as how many more suggestions needed
	 * 
	 */
	public int moreSuggestionNeeded(List<String> list, String sarr[], String str, int needed) {
		int count = 0;
		if (needed > 0) {
			HashSet<String> hs = new HashSet<>(list);
			System.out.println("More suggestions needed.");
			for (String s : sarr) {
				// Check if the searched string is present in any of the
				// products in the products list
				boolean flag = s.contains(str);
				if (flag) {
					if (!hs.contains(s)) {
						list.add(s);
						count++;
					}
				}
				// If the total number of needed suggestions are reached, break
				// the loop
				if (count == needed)
					break;
			}
		}
		return count;
	}

	/**
	 * This method is called when suggestion list is empty after all other
	 * method calls.
	 * 
	 * @param trie
	 *            as Trie of products
	 * @param list
	 *            as Suggestion List,
	 * @param sarr
	 *            as String array for all products,
	 * @param searchStr
	 *            as Searched String,
	 * @param prev
	 *            as Previous String,
	 * @param needed
	 *            as how many more suggestions needed
	 * @param len
	 *            as the length of Searched String
	 * @param lim
	 *            as the number suggestions needed
	 * 
	 */
	public List<String> desperateSearch(Trie trie, List<String> list, String sarr[], String prev, String searchStr,
			int len, int lim) {
		System.out.println("Desparate search!");
		// If there is any change in the middle of the string or the string
		// length is less than 3, check Lavenstein on product array
		if (prev.equals("") || !searchStr.startsWith(prev) || len <= 3)
			list = findUnkownKeywordArr(sarr, searchStr, len, lim);
		else {
			String trieWord = determineTrieWord(searchStr);
			list = trie.findCompletions(trieWord);
			// If list is not empty, check for Damerau Lavenstein
			if (!list.isEmpty())
				list = findUnkownKeyword(list, searchStr, len, lim);
			// If list is empty, check for Lavenstein over product array
			else
				list = findUnkownKeywordArr(sarr, searchStr, len, lim);
		}
		return list;
	}

	/**
	 * This method checks Lavenstein distance between searched string and the
	 * complete list of products/items
	 * 
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
	public List<String> findUnkownKeywordArr(String sarr[], String str, int len, int lim) {
		SpellAutoCorrect luw = new SpellAutoCorrect();
		int count = 0;
		// Check Lavenstein on Product array. Timeout added based on typing
		// speed of average human
		TreeSet<Correctspell> ts = luw.calculateEditDistanceArr(sarr, str, len, lim >> 2, 240);
		List<String> list = new ArrayList<>();
		for (Correctspell csp : ts) {
			list.add(csp.getS());
			count++;
			// Put the strings with lowest edit distances in the list
			if (count == lim) {
				break;
			}
		}
		return list;
	}

	/**
	 * This method checks Damerau Lavenstein distance between searched string
	 * and the complete list of products/items
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
	public List<String> findUnkownKeyword(List<String> list, String str, int len, int lim) {
		SpellAutoCorrect luw = new SpellAutoCorrect();
		int count = 0;
		// Check Damerau Lavenstein on shortened list received. Timeout is
		// lesser than above because of pre-processing logic
		TreeSet<Correctspell> ts = luw.calculateEditDistanceDam(list, str, len, lim, 200);
		list = new ArrayList<>();
		for (Correctspell csp : ts) {
			list.add(csp.getS());
			count++;
			// Put the strings with lowest edit distances in the list
			if (count == lim) {
				break;
			}
		}
		return list;
	}

	/**
	 * This method determines the probability start of the words for fetching
	 * suggestion list.
	 * 
	 * @param str
	 *            as Searched String
	 *
	 */
	public String determineTrieWord(String str) {
		Calculations calc = new Calculations();
		double compatibilityScore = calc.addProbability(str.charAt(0) - 96, str.charAt(1) - 96);
		// Heuristic logic based on NoisyChannel mode and common observation
		if (compatibilityScore > 0.2)
			return str.substring(0, 2);
		return str.substring(0, 1);
	}

}
