package com.enhancer.nlp;

import java.util.*;

/**
 * @author PRITAM. Created for breaking a word into multiple words.
 *
 */
public class WordBreak {

	/**
	 * This method checks character sequence amongst set of words in the present
	 * dictionary.
	 * 
	 * @param list
	 *            as Suggestion List,
	 * @param dict
	 *            as set of dictionary words,
	 * @param memory
	 *            as set for memoization,
	 * @param answer
	 *            as the correct broken word
	 * 
	 */
	public boolean find(List<String> list, String s, HashSet<String> dict, HashSet<String> memory, String answer) {
		if (s.length() == 0) {
			// System.out.println(answer);
			list.add(answer.trim());
			return true;
		} else if (memory.contains(s)) {
			return false;
		} else {
			int index = 0;
			String word = "";
			while (index < s.length()) {
				word += s.charAt(index);// add one char at a time
				// check if word already being solved
				if (dict.contains(word.toLowerCase())) {
					if (find(list, s.substring(index + 1), dict, memory, answer + word + " ")) {
						return true;
					} else {
						// System.out.println("backtrack");
						index++;
					}
				} else {
					index++;
				}
			}
			memory.add(s);// memoization for future;
			return false;
		}
	}

	/**
	 * This is where the processing starts.
	 * 
	 * @param list
	 *            as Suggestion List,
	 * @param word
	 *            as the Searched String
	 * 
	 */
	public List<String> wordBreakUtil(List<String> list, String word) {
		HashSet<String> dict = new HashSet<>();
		// Create a dictionary from list of suggestions
		createDictionary(dict, list);
		list = new ArrayList<>();
		HashSet<String> memo = new HashSet<>();
		// Try to find broken words
		find(list, word, dict, memo, "");
		// boolean result = find(list, word, dict, memo, "");
		// System.out.println(result);
		return list;

	}

	/**
	 * This method creates a dictionary set from list of suggestions.
	 * 
	 * @param dict
	 *            as dictionary set,
	 * @param list
	 *            as suggestion list
	 * 
	 */
	public void createDictionary(HashSet<String> dict, List<String> list) {
		for (String s : list) {
			String sarr[] = s.split(" ");
			for (String str : sarr)
				dict.add(str);
		}
	}
}