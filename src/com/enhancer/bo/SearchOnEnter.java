package com.enhancer.bo;

import java.util.*;
import org.apache.commons.lang3.StringUtils;
import com.enhancer.model.Correctspell;
import com.enhancer.nlp.*;

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

	public List<String> findUnkownKeyword(List<String> list, String arr[], String str, int len, int lim) {
		SpellAutoCorrect luw = new SpellAutoCorrect();
		TreeSet<Correctspell> ts = luw.calculateEditDistanceArr(arr, str, len, lim >> 1, 1200);
		int count = 0;
		for (Correctspell csp : ts) {
			list.add(csp.getS());
			count++;
			if (count == lim)
				break;
		}
		return list;
	}

	public void updateNgrams(String str) {
		DynamicBigram db = DynamicBigram.getInstance();
		db.continueTraining(str);
		if (StringUtils.countMatches(str, " ") >= 2) {
			DynamicTrigram dt = DynamicTrigram.getInstance();
			dt.continueTraining(str.split(" "));
		}
	}

	public boolean isSpacePresentInSugg(List<String> list) {
		for (String s : list)
			if (s.contains(" "))
				return true;
		return false;
	}

}
