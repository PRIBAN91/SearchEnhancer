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
		List<String> list = higherPrecedenceList(suggList, str);
		return list;
	}

	public List<String> higherPrecedenceList(List<String> suggList, String str) {
		Bigram db = Bigram.getInstance();
		String sarr[] = str.split(" ");
		String lastWord = sarr[sarr.length - 1];
		List<String> list = new ArrayList<>();
		List<BigramWord> wordList = new ArrayList<>();
		for (String s : suggList) {
			double factor = db.perplexity(s);
			factor += findEditWeight(s.split(" "), lastWord);
			wordList.add(new BigramWord(s, factor));
		}
		Collections.sort(wordList);
		for (BigramWord wrd : wordList)
			list.add(wrd.toString());

		return list;
	}

	public double findEditWeight(String words[], String lastWord) {
		SpellAutoCorrect luw = new SpellAutoCorrect();
		Calculations calc = new Calculations();
		int len = lastWord.length(), startWithCount = 1;
		double result = len;
		int maxEdist = calc.determineMaxEdist(len);
		for (String word : words) {
			if (word.startsWith(lastWord))
				startWithCount++;
			if (Math.abs(word.length() - len) <= maxEdist) {
				double res = luw.getWeightedLevenshtein(lastWord, word);
				if (res <= maxEdist) {
					result = Math.min(result, res);
				}
			}
		}
		result /= startWithCount;
		return result;
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

}
