package com.enhancer.nlp;

import java.util.*;
import com.enhancer.model.Correctspell;

/**
 * @author PRITAM. Created for Spell check of the searched string with the list
 *         of products. It will return closest set of items/products.
 *
 */
public class SpellAutoCorrect {

	private final int deleteCost = 1, insertCost = 1, replaceCost = 1, swapCost = 1;

	/**
	 * This method calls Damerau Lavenstein function for calculating distance
	 * between the searched string and previous set of suggestions.
	 * 
	 * @param list
	 *            as previous suggestion list
	 * @param str
	 *            as the searched string
	 * @param len
	 *            as the length of searched string
	 * 
	 */
	public TreeSet<Correctspell> calculateEditDistance(List<String> list, String str, int len) {
		Calculations calc = new Calculations();
		// Calculate max Edit Distance from heuristic calculations
		int maxEdist = calc.determineMaxEdist(len);
		// Treeset used for sorting closest words by their edit distances
		TreeSet<Correctspell> ts = new TreeSet<>();
		// Loop over suggestion list
		for (String s : list) {
			// Check if length of searced string and word from lis is lesser
			// than maximum edist distance. This is done as difference in length
			// of source and target string can be the maximum Edit Distance
			// possible. This will save some time.
			if (Math.abs(s.length() - len) <= maxEdist) {
				// Calculate Damerau Lavenstein with searched sring as source
				// and string from suggestion list as target
				int res = getDamerauLevenshteinDistance(str, s);
				// Put in suggestion list only if distance < max Edit Distance
				if (res <= maxEdist) {
					ts.add(new Correctspell(s, res));
					// System.out.println(s);
				}
			}
		}
		return ts;
	}

	/**
	 * This method calls Damerau Lavenstein function for calculating distance
	 * between the searched string and previous set of suggestions.
	 * 
	 * @param list
	 *            as list for iteration
	 * @param str
	 *            as the searched string
	 * @param len
	 *            as the length of searched string
	 * @param lim
	 *            as the maximum number of closer words
	 * @param runningtime
	 *            as the limitation of time for execution
	 * 
	 */
	public TreeSet<Correctspell> calculateEditDistanceDam(List<String> list, String str, int len, int lim,
			long runningTime) {
		TreeSet<Correctspell> ts = new TreeSet<>();
		Calculations calc = new Calculations();
		// Calculate max Edit Distance from Heuristic calculations
		int maxEdist = calc.determineMaxEdist(len);
		// Minimum Edit Distance is Half of Maximum Edit Distance for strings
		// too close to each other
		int minEdist = maxEdist >> 1, smallCnt = 0;
		long start = System.currentTimeMillis();
		long end = start + runningTime;
		for (String s : list) {
			// Timeout function is implemented according to average typing speed
			if (System.currentTimeMillis() >= end)
				break;
			if (Math.abs(s.length() - len) <= maxEdist) {
				int res = getDamerauLevenshteinDistance(str, s);
				if (res <= maxEdist) {
					// Check if the word is too close to searched string
					if (res <= minEdist)
						smallCnt++;
					ts.add(new Correctspell(s, res));
					// System.out.println(s);
					// Check if closest string list has reached the limit
					if (smallCnt == lim)
						break;
				}
			}
		}
		return ts;
	}

	/**
	 * This method calls for Lavenstein distance function between the searched
	 * string and array set of Products/Items.
	 * 
	 * @param arr
	 *            as the complete array of Products/Items
	 * @param str
	 *            as the searched string
	 * @param len
	 *            as the length of searched string
	 * @param lim
	 *            as the number of suggestions needed
	 * @param runningtime
	 *            as the limitation of time for execution
	 * 
	 */
	public TreeSet<Correctspell> calculateEditDistanceArr(String arr[], String str, int len, int lim,
			long runningTime) {
		TreeSet<Correctspell> ts = new TreeSet<>();
		Calculations calc = new Calculations();
		// Calculate max Edit Distance from Heuristic calculations
		int maxEdist = calc.determineMaxEdist(len);
		// Minimum Edit Distance is Half of Maximum Edit Distance for strings
		// too close to each other
		int minEdist = maxEdist >> 1, smallCnt = 0;
		long start = System.currentTimeMillis();
		long end = start + runningTime;
		for (String s : arr) {
			// Timeout function is implemented according to average typing speed
			if (System.currentTimeMillis() >= end)
				break;
			if (Math.abs(s.length() - len) <= maxEdist) {
				int res = getLevenshteinDistance(str, s);
				if (res <= maxEdist) {
					// Check if the word is too close to searched string
					if (res <= minEdist)
						smallCnt++;
					ts.add(new Correctspell(s, res));
					// System.out.println(s);
					// Check if closest string list has reached the limit
					if (smallCnt == lim)
						break;
				}
			}
		}
		return ts;
	}

	/**
	 * This method calculates Damerau Lavenstein distance between the source and
	 * target strings.
	 * 
	 * @param source
	 *            as source string
	 * @param target
	 *            as target string
	 * 
	 */
	public int getDamerauLevenshteinDistance(String source, String target) {
		// Check
		// https://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance
		// for more details
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

	/**
	 * This method calculates Lavenstein distance between the source and the
	 * target string.
	 * 
	 * @param source
	 *            as source string
	 * @param target
	 *            as target string
	 * 
	 */
	public int getLevenshteinDistance(String source, String target) {
		// Check https://en.wikipedia.org/wiki/Edit_distance for more details
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

	/**
	 * This method calculates Weighted Lavenstein distance between the source
	 * and target strings.
	 * 
	 * @param source
	 *            as source string
	 * @param target
	 *            as target string
	 * 
	 */
	public double getWeightedLevenshtein(String source, String target) {
		// Same as above method with weights from Calculations
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
