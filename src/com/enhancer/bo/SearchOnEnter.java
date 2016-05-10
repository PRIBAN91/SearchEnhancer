package com.enhancer.bo;

import java.util.*;
import com.enhancer.model.Correctspell;
import com.enhancer.nlp.DynamicBigram;
import com.enhancer.nlp.DynamicTrigram;
import com.enhancer.nlp.SpellAutoCorrect;

public class SearchOnEnter {

	public List<String> findKeywordSuggestion(List<String> list, String str, int len, int lim) {

		SpellAutoCorrect luw = new SpellAutoCorrect();
		TreeSet<Correctspell> ts = luw.calculateEditDistance(list, str, len);
		int count = 0;
		list = new ArrayList<String>();
		for (Correctspell csp : ts) {
			list.add(csp.getS());
			count++;
			if (count == lim)
				break;
		}
		return list;
	}

	public List<String> findUnkownKeyword(String arr[], String str, int len, int lim) {

		SpellAutoCorrect luw = new SpellAutoCorrect();
		TreeSet<Correctspell> ts = luw.calculateEditDistanceArr(arr, str, len, lim >> 1, 1440);
		int count = 0;
		List<String> list = new ArrayList<String>();
		for (Correctspell csp : ts) {
			list.add(csp.getS());
			count++;
			if (count == lim)
				break;
		}
		return list;
	}

	public void updateBigram(String str) {
		DynamicBigram db = DynamicBigram.getInstance();
		db.continueTraining(str);
		String words[] = str.split(" ");
		if (words.length >= 3) {
			DynamicTrigram dt = DynamicTrigram.getInstance();
			dt.continueTraining(words);
		}
	}

}
