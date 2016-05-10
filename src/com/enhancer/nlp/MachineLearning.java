package com.enhancer.nlp;

import java.util.*;
import com.enhancer.model.WordProbability;

public class MachineLearning {

	public List<String> calculateMaxLikeEst(List<String> suggList, String str) {
		DynamicBigram db = DynamicBigram.getInstance();
		String sarr[] = str.split(" ");
		String word1 = "";
		double startingProb = 0, secondWordProb = 0;
		int len = sarr.length;
		List<String> firstWordList = new ArrayList<>();
		List<String> list = new ArrayList<>();
		if (len >= 3) {
			word1 = sarr[len - 3] + " " + sarr[len - 2];
			list = switchToTrigrams(suggList, word1);
		} else {
			word1 = sarr[0];
			startingProb = db.unsmoothedProbability(":S", sarr[0]);
			secondWordProb = db.secondWordProbability(firstWordList, sarr[0]);
			List<WordProbability> wordList = new ArrayList<>();
			if (secondWordProb >= startingProb) {
				for (String s : firstWordList)
					wordList.add(new WordProbability(s + " " + word1, db.unsmoothedProbability(s, word1)));
			}
			for (String s : suggList) {
				String arr[] = s.split(" ");
				wordList.add(new WordProbability(s, db.unsmoothedProbability(word1, arr[1])));
			}
			Collections.sort(wordList);
			for (WordProbability wrd : wordList)
				list.add(wrd.toString());
		}
		return list;
	}

	public List<String> switchToTrigrams(List<String> suggList, String word1) {

		DynamicTrigram dt = DynamicTrigram.getInstance();
		List<WordProbability> wordList = new ArrayList<>();
		for (String s : suggList) {
			String words[] = s.split(" ");
			int arrlen = words.length;
			wordList.add(new WordProbability(s, dt.unsmoothedProbability(word1, words[arrlen - 1])));
		}
		Collections.sort(wordList);
		List<String> list = new ArrayList<>();
		for (WordProbability wrd : wordList)
			list.add(wrd.toString());
		return list;
	}

}
