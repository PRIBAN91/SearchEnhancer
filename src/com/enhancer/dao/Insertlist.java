package com.enhancer.dao;

import java.util.ArrayList;

import org.hibernate.Session;
import org.hibernate.Transaction;
import com.enhancer.model.Wordlist;
import com.enhancer.util.HibernateUtil;
import com.enhancer.util.ReadFile;

public class Insertlist {

	public void insertWordList() {

		System.out.println("Inside load method :: ");
		Session session = HibernateUtil.getSessionFactory().openSession();
		System.out.println("After session");
		Transaction tx = session.beginTransaction();
		ArrayList<String> ar = new ArrayList<>();
		ReadFile rf = new ReadFile();
		rf.read(ar);
		int sz = ar.size();
		System.out.println("After reading file");
		for (int i = 0; i < sz; i++) {
			Wordlist wl = new Wordlist();
			wl.setSequence(i + 1);
			wl.setWords(ar.get(i));
			session.save(wl);
			if (i % 20 == 0) { // 20, same as the JDBC batch size
				// flush a batch of inserts and release memory:
				session.flush();
				session.clear();
			}
		}
		tx.commit();
		session.close();
		System.out.println("After successful transaction!");
	}
}
