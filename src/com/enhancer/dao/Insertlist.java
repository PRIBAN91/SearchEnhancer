package com.enhancer.dao;

import java.util.ArrayList;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.enhancer.model.Wordlist;
import com.enhancer.util.HibernateUtil;
import com.enhancer.util.ReadFile;

/**
 * @author PRITAM. Created for loading list from database table.
 *
 */
public class Insertlist {

	/**
	 * This method inserts list of products/items from file to database table.
	 * Preferrable file formats are .txt and .csv
	 */
	public void insertWordList() {
		System.out.println("Inside load method :: ");
		// Get sessionfactory object initialized in Listener.java and create
		// session object
		Session session = HibernateUtil.getSessionFactory().openSession();
		System.out.println("After session initialization.");
		Transaction tx = session.beginTransaction();
		ArrayList<String> ar = new ArrayList<>();
		// Read from file
		ReadFile rf = new ReadFile();
		rf.read(ar);
		int sz = ar.size();
		System.out.println("After reading file");
		// Insertion is done using batches of 20 records
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

	// For demo purpose. Run this java file for bulk insert of data in table.
	// Uncomment the below lines of code.
	// public static void main(String[] args) {
	// Insertlist il = new Insertlist();
	// il.insertWordList();
	// }
}
