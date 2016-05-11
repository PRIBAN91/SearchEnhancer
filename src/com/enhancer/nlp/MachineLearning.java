package com.enhancer.nlp;

import java.util.*;
import com.enhancer.model.BigramWord;
import com.enhancer.model.TrigramWord;

public class MachineLearning {

	public List<String> calculateMaxLikeEst(List<String> suggList, String str) {
		DynamicBigram db = DynamicBigram.getInstance();
		String sarr[] = str.split(" ");
		String word1 = "";
		double startingProb = 0, secondWordProb = 0;
		int len = sarr.length;
		List<String> firstWordList = new ArrayList<>();
		List<String> list = new ArrayList<>();
		if (len >= 4) {
			word1 = sarr[len - 3] + " " + sarr[len - 2];
			list = switchToTrigrams(suggList, word1);
		} else {
			word1 = sarr[0];
			startingProb = db.unsmoothedProbability(":S", word1);
			secondWordProb = db.secondWordProbability(firstWordList, word1);
			List<BigramWord> wordList = new ArrayList<>();
			if (secondWordProb >= startingProb) {
				for (String s : firstWordList) {
					String tmp = s + " " + word1;
					wordList.add(new BigramWord(tmp, db.perplexity(tmp)));
				}
			}
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

}
