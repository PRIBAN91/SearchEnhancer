package com.enhancer.util;

import java.util.*;
import javax.servlet.ServletContext;
import com.enhancer.dao.Loadlist;
import com.enhancer.model.Wordlist;

public class UpdateFromDb {

	public synchronized void fetchFreshList(ServletContext context) {

		System.out.println("::::  Refreshing already present list from DB ::::");
		Stopwatch sw = new Stopwatch();
		Loadlist ls = new Loadlist();
		TreeSet<Wordlist> ts = ls.loadWordList();
		int sz = ts.size(), step = 0;
		System.out.println("After loading list");
		Trie trie = new Trie();
		String arr[] = new String[sz];
		HashSet<String> hs = new HashSet<>();
		System.out.println("Putting it in Trie");
		for (Wordlist wl : ts) {
			String s = wl.getWords();
			trie.addWord(s);
			arr[step++] = s;
			hs.add(s);
		}
		double time = sw.elapsedTime();
		System.out.println("Elapsed time : " + time);
		context.setAttribute("Products", trie);
		context.setAttribute("ProductArray", arr);
		context.setAttribute("Categories", hs);
		System.out.println("Products got loaded at : " + new Date() + " successfully!");
	}

}
