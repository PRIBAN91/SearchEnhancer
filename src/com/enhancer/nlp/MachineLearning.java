package com.enhancer.nlp;

import java.util.*;
import com.enhancer.model.*;

public class MachineLearning {

	public List<String> calculateMostProbable(List<String> suggList, String str) {
		Bigram db = Bigram.getInstance();
		String sarr[] = str.split(" ");
		String word1 = "";
		int len = sarr.length;
		List<String> list = new ArrayList<>();
		if (len >= 4) {
			word1 = sarr[len - 3] + " " + sarr[len - 2];
			list = switchToTrigrams(suggList, word1);
		} else {
			List<BigramWord> wordList = new ArrayList<>();
			for (String s : suggList)
				wordList.add(new BigramWord(s, db.perplexity(s)));
			Collections.sort(wordList);
			for (BigramWord wrd : wordList)
				list.add(wrd.toString());
		}
		return list;
	}

	public List<String> switchToTrigrams(List<String> suggList, String word1) {

		DynamicTrigram dt = DynamicTrigram.getInstance();
		List<TrigramWord> wordList = new ArrayList<>();
		for (String s : suggList) {
			String words[] = s.split(" ");
			int arrlen = words.length;
			wordList.add(new TrigramWord(s, dt.unsmoothedProbability(word1, words[arrlen - 1])));
		}
		Collections.sort(wordList);
		List<String> list = new ArrayList<>();
		for (TrigramWord wrd : wordList)
			list.add(wrd.toString());
		return list;
	}

	// Work-in-progress
	public List<String> calculateMaxLikeEst(List<String> suggList, String str, boolean correctFirstWord) {
		List<String> list = calculateMostProbable(suggList, str);
		return list;
	}

	public List<String> higherPrecedenceList(List<String> list, String secondWord) {
		list = splitWordsForPrecedence(list);
		List<String> higherRankList = new ArrayList<>();
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			String s = it.next();
			if (s.contains(secondWord)) {
				higherRankList.add(s);
				it.remove();
			}
		}
		return higherRankList;
	}

	public List<String> splitWordsForPrecedence(List<String> list) {
		List<String> splitList = new ArrayList<>();
		for (String s : list) {
			splitList.addAll(Arrays.asList(s.split(" ")));
		}
		return list;
	}

	public List<String> checkAnotherContain(String sarr[], List<String> suggList, long runningTime) {
		List<String> list = new ArrayList<>();
		long start = System.currentTimeMillis();
		long end = start + runningTime;
		for (String s : sarr) {
			if (System.currentTimeMillis() >= end)
				break;
			for (String w : suggList)
				if (s.contains(w))
					list.add(s);
		}
		return list;
	}

	public List<String> findUnkownKeywordWeighted(List<String> list, String str, int len, int lim) {
		SpellAutoCorrect luw = new SpellAutoCorrect();
		int count = 0;
		TreeSet<CorrectSpelling> ts = luw.calculateWeightedEditDist(list, str, len, lim >> 2, 240);
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

}
