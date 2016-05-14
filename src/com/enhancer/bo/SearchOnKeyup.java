package com.enhancer.bo;

import java.util.*;
import com.enhancer.model.CorrectSpelling;
import com.enhancer.model.Correctspell;
import com.enhancer.nlp.Calculations;
import com.enhancer.nlp.SpellAutoCorrect;
import com.enhancer.util.Trie;

public class SearchOnKeyup {

	public List<String> checkPrevSuggestion(List<String> list, boolean prevSuggChk, String searchStr) {
		if (!list.isEmpty() && !prevSuggChk) {
			System.out.println("In spell check of previous suggestions.");
			list = oldSuggestion(list, searchStr, searchStr.length());
			prevSuggChk = true;
		} else {
			prevSuggChk = false;
			list = new ArrayList<>();
		}
		return list;
	}

	public List<String> oldSuggestion(List<String> list, String str, int len) {

		SpellAutoCorrect luw = new SpellAutoCorrect();
		TreeSet<Correctspell> ts = luw.calculateEditDistance(list, str, len);
		list = new ArrayList<String>();
		for (Correctspell csp : ts)
			list.add(csp.getS());
		return list;
	}

	public int moreSuggestionNeeded(List<String> list, String sarr[], String str, int needed) {

		int count = 0;
		if (needed > 0) {
			HashSet<String> hs = new HashSet<>(list);
			System.out.println("More suggestions needed.");
			for (String s : sarr) {
				boolean flag = s.contains(str);
				if (flag) {
					if (!hs.contains(s)) {
						list.add(s);
						count++;
					}
				}
				if (count == needed)
					break;
			}
		}
		return count;
	}

	public List<String> findUnkownKeywordWeighted(List<String> list, String str, int len, int lim) {

		SpellAutoCorrect luw = new SpellAutoCorrect();
		int count = 0;
		TreeSet<CorrectSpelling> ts = luw.calculateWeightedEditDist(list, str, len, lim >> 2, 3000);
		list = new ArrayList<>();
		for (CorrectSpelling csp : ts) {
			list.add(csp.getStr());
			count++;
			if (count == lim) {
				break;
			}
		}
		return list;
	}

	public List<String> findUnkownKeywordArr(String sarr[], String str, int len, int lim) {

		SpellAutoCorrect luw = new SpellAutoCorrect();
		int count = 0;
		TreeSet<Correctspell> ts = luw.calculateEditDistanceArr(sarr, str, len, lim >> 2, 240);
		List<String> list = new ArrayList<>();
		for (Correctspell csp : ts) {
			list.add(csp.getS());
			count++;
			if (count == lim) {
				break;
			}
		}
		return list;
	}

	public List<String> findUnkownKeyword(List<String> list, String str, int len, int lim) {

		SpellAutoCorrect luw = new SpellAutoCorrect();
		int count = 0;
		TreeSet<Correctspell> ts = luw.calculateEditDistanceDam(list, str, len, lim, 200);
		list = new ArrayList<>();
		for (Correctspell csp : ts) {
			list.add(csp.getS());
			count++;
			if (count == lim) {
				break;
			}
		}
		return list;
	}

	public String determineTrieWord(String str) {

		Calculations calc = new Calculations();
		double compatibilityScore = calc.addProbability(str.charAt(0) - 96, str.charAt(1) - 96);
		if (compatibilityScore > 0.2)
			return str.substring(0, 2);

		return str.substring(0, 1);
	}

	public List<String> desperateSearch(Trie trie, List<String> list, String sarr[], String prev, String searchStr,
			int len, int lim) {
		System.out.println("Desparate search!");
		if (prev.equals("") || !searchStr.startsWith(prev) || len <= 3)
			list = findUnkownKeywordArr(sarr, searchStr, len, lim);
		else {
			String trieWord = determineTrieWord(searchStr);
			list = trie.findCompletions(trieWord);
			if (!list.isEmpty())
				list = findUnkownKeyword(list, searchStr, len, lim);
			else
				list = findUnkownKeywordArr(sarr, searchStr, len, lim);
		}
		return list;
	}

}
