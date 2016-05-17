package com.enhancer.nlp;

import java.util.*;
import com.enhancer.model.Correctspell;

public class SpellAutoCorrect {

	private final int deleteCost = 1, insertCost = 1, replaceCost = 1, swapCost = 1;

	public TreeSet<Correctspell> calculateEditDistance(List<String> list, String str, int len) {
		Calculations calc = new Calculations();
		int maxEdist = calc.determineMaxEdist(len);
		TreeSet<Correctspell> ts = new TreeSet<>();
		for (String s : list) {
			if (Math.abs(s.length() - len) <= maxEdist) {
				int res = getDamerauLevenshteinDistance(str, s);
				if (res <= maxEdist) {
					ts.add(new Correctspell(s, res));
					// System.out.println(s);
				}
			}
		}
		return ts;
	}

	public TreeSet<Correctspell> calculateEditDistanceDam(List<String> list, String str, int len, int lim,
			long runningTime) {
		TreeSet<Correctspell> ts = new TreeSet<>();
		Calculations calc = new Calculations();
		int maxEdist = calc.determineMaxEdist(len);
		int minEdist = maxEdist >> 1, smallCnt = 0;
		long start = System.currentTimeMillis();
		long end = start + runningTime;
		for (String s : list) {
			if (System.currentTimeMillis() >= end)
				break;
			if (Math.abs(s.length() - len) <= maxEdist) {
				int res = getDamerauLevenshteinDistance(str, s);
				if (res <= maxEdist) {
					if (res <= minEdist)
						smallCnt++;
					ts.add(new Correctspell(s, res));
					// System.out.println(s);
					if (smallCnt == lim)
						break;
				}
			}
		}
		return ts;
	}

	public TreeSet<Correctspell> calculateEditDistanceArr(String arr[], String str, int len, int lim,
			long runningTime) {

		TreeSet<Correctspell> ts = new TreeSet<>();
		Calculations calc = new Calculations();
		int maxEdist = calc.determineMaxEdist(len);
		int minEdist = maxEdist >> 1, smallCnt = 0;
		long start = System.currentTimeMillis();
		long end = start + runningTime;
		for (String s : arr) {
			if (System.currentTimeMillis() >= end)
				break;
			if (Math.abs(s.length() - len) <= maxEdist) {
				int res = getLevenshteinDistance(str, s);
				if (res <= maxEdist) {
					if (res <= minEdist)
						smallCnt++;
					ts.add(new Correctspell(s, res));
					// System.out.println(s);
					if (smallCnt == lim)
						break;
				}
			}
		}
		return ts;
	}

	public int getDamerauLevenshteinDistance(String source, String target) {
		char src[] = source.toCharArray();
		char trgt[] = target.toCharArray();
		int m = src.length;
		int n = trgt.length;
		if (m == 0)
			return m * insertCost;
		if (n == 0)
			return n * deleteCost;
		int[][] table = new int[m][n];
		Map<Character, Integer> sourceIndexByCharacter = new HashMap<Character, Integer>();
		if (src[0] != trgt[0])
			table[0][0] = Math.min(replaceCost, deleteCost + insertCost);
		sourceIndexByCharacter.put(src[0], 0);
		for (int i = 1; i < m; i++) {
			int deleteDistance = table[i - 1][0] + deleteCost;
			int insertDistance = (i + 1) * deleteCost + insertCost;
			int matchDistance = i * deleteCost + (src[i] == trgt[0] ? 0 : replaceCost);
			table[i][0] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
		}
		for (int j = 1; j < n; j++) {
			int deleteDistance = (j + 1) * insertCost + deleteCost;
			int insertDistance = table[0][j - 1] + insertCost;
			int matchDistance = j * insertCost + (src[0] == trgt[j] ? 0 : replaceCost);
			table[0][j] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
		}
		for (int i = 1; i < m; i++) {
			int maxSourceLetterMatchIndex = src[i] == trgt[0] ? 0 : -1;
			for (int j = 1; j < n; j++) {
				Integer candidateSwapIndex = sourceIndexByCharacter.get(trgt[j]);
				int jSwap = maxSourceLetterMatchIndex;
				int deleteDistance = table[i - 1][j] + deleteCost;
				int insertDistance = table[i][j - 1] + insertCost;
				int matchDistance = table[i - 1][j - 1];
				if (src[i] != trgt[j]) {
					matchDistance += replaceCost;
				} else {
					maxSourceLetterMatchIndex = j;
				}
				int swapDistance;
				if (candidateSwapIndex != null && jSwap != -1) {
					int iSwap = candidateSwapIndex;
					int preSwapCost;
					if (iSwap == 0 && jSwap == 0) {
						preSwapCost = 0;
					} else {
						preSwapCost = table[Math.max(0, iSwap - 1)][Math.max(0, jSwap - 1)];
					}
					swapDistance = preSwapCost + (i - iSwap - 1) * deleteCost + (j - jSwap - 1) * insertCost + swapCost;
				} else {
					swapDistance = Integer.MAX_VALUE;
				}
				table[i][j] = Math.min(Math.min(Math.min(deleteDistance, insertDistance), matchDistance), swapDistance);
			}
			sourceIndexByCharacter.put(src[i], i);
		}
		return table[m - 1][n - 1];
	}

	public int getLevenshteinDistance(String source, String target) {

		int len1 = source.length();
		int len2 = target.length();
		int[][] arr = new int[len1 + 1][len2 + 1];
		for (int i = 0; i <= len1; i++)
			arr[i][0] = i;
		for (int i = 1; i <= len2; i++)
			arr[0][i] = i;
		for (int i = 1; i <= len1; i++) {
			for (int j = 1; j <= len2; j++) {
				int m = (source.charAt(i - 1) == target.charAt(j - 1)) ? 0 : 1;
				arr[i][j] = Math.min(Math.min(arr[i - 1][j] + 1, arr[i][j - 1] + 1), arr[i - 1][j - 1] + m);
			}
		}
		return arr[len1][len2];
	}

	public double getWeightedLevenshtein(String source, String target) {
		Calculations calc = new Calculations();
		double delCost = 0, addCost = 0, repCost;
		int len1 = source.length();
		int len2 = target.length();
		double[][] arr = new double[len1 + 1][len2 + 1];

		for (int i = 1; i <= len1; i++) {
			if (i > 1)
				addCost = calc.addScore(((int) (source.charAt(i - 1) - 96)), ((int) (source.charAt(i - 2) - 96)));
			arr[i][0] = i + addCost;
		}

		for (int i = 1; i <= len2; i++) {
			if (i > 1)
				delCost = calc.delScore(((int) (target.charAt(i - 1) - 96)), ((int) (target.charAt(i - 2) - 96)));
			arr[0][i] = i + delCost;
		}

		for (int i = 1; i <= len1; i++) {
			for (int j = 1; j <= len2; j++) {
				delCost = 0;
				addCost = 0;
				repCost = calc.subScore(((int) (source.charAt(i - 1) - 96)), ((int) (target.charAt(j - 1) - 96)));
				double m = (source.charAt(i - 1) == target.charAt(j - 1)) ? 0 : 1 + repCost;

				if (i > 1)
					delCost = calc.delScore(((int) (source.charAt(i - 2) - 96)), ((int) (source.charAt(i - 1) - 96)));

				if (j > 1)
					addCost = calc.addScore(((int) (target.charAt(j - 2) - 96)), ((int) (target.charAt(j - 1) - 96)));

				arr[i][j] = Math.min(Math.min(arr[i - 1][j] + 1 + delCost, arr[i][j - 1] + 1 + addCost),
						arr[i - 1][j - 1] + m);

			}
		}
		return arr[len1][len2];
	}

}
