package com.enhancer.dao;

import java.util.*;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import com.enhancer.model.Wordlist;
import com.enhancer.util.HibernateUtil;

/**
 * @author PRITAM. Created for loading list from database table.
 *
 */
public class Loadlist {

	/**
	 * This method loads list from database table Wordlist. Hibernate is used as
	 * an ORM tool for fetching data from table.
	 */
	@SuppressWarnings("unchecked")
	public TreeSet<Wordlist> loadWordList() {
		System.out.println("Inside load method :: ");
		// Get sessionfactory object initialized in Listener.java and create
		// session object
		Session session = HibernateUtil.getSessionFactory().openSession();
		// Simple HQL for fetching list
		Query query = session.createQuery("from Wordlist");
		ArrayList<Wordlist> ar = new ArrayList<>();
		try {
			ar = (ArrayList<Wordlist>) query.list();
			System.out.println("Length of Items in db :: " + ar.size());
		} catch (HibernateException e) {
			e.printStackTrace();
		}
		session.close();
		// Treeset used, so that any duplicate is removed and the list is also
		// sorted in natural ascending order
		TreeSet<Wordlist> ts = new TreeSet<>(ar);
		return ts;
	}

}
