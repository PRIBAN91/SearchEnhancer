package com.enhancer.dao;

import java.util.*;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import com.enhancer.model.Wordlist;
import com.enhancer.util.HibernateUtil;

public class Loadlist {

	@SuppressWarnings("unchecked")
	public TreeSet<Wordlist> loadWordList() {

		System.out.println("Inside load method :: ");
		Session session = HibernateUtil.getSessionFactory().openSession();
		System.out.println("After session");
		Query query = session.createQuery("from Wordlist");
		ArrayList<Wordlist> ar = new ArrayList<>();
		try {
			ar = (ArrayList<Wordlist>) query.list();
			System.out.println("Length of dictionary in db :: " + ar.size());
		} catch (HibernateException e) {
			e.printStackTrace();
		}
		session.close();
		TreeSet<Wordlist> ts = new TreeSet<>(ar);
		return ts;
	}

}
