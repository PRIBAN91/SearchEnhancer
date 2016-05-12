package com.enhancer.listener;

import java.util.*;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;
import org.apache.commons.lang3.StringUtils;
import com.enhancer.dao.Loadlist;
import com.enhancer.model.Wordlist;
import com.enhancer.nlp.Bigram;
import com.enhancer.nlp.DynamicBigram;
import com.enhancer.nlp.DynamicTrigram;
import com.enhancer.util.DbFetchTask;
import com.enhancer.util.HibernateUtil;
import com.enhancer.util.Stopwatch;
import com.enhancer.util.Trie;
import com.enhancer.util.UpdateBigramTask;

/**
 * Application Lifecycle Listener implementation class Listener
 *
 */
@WebListener
public class Listener implements ServletContextListener, ServletContextAttributeListener, HttpSessionListener,
		HttpSessionAttributeListener, HttpSessionActivationListener, HttpSessionBindingListener, HttpSessionIdListener,
		ServletRequestListener, ServletRequestAttributeListener, AsyncListener {

	private Timer timer;

	/**
	 * Default constructor.
	 */
	public Listener() {

	}

	/**
	 * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent arg0) {

	}

	/**
	 * @see ServletContextAttributeListener#attributeRemoved(ServletContextAttributeEvent)
	 */
	public void attributeRemoved(ServletContextAttributeEvent arg0) {

	}

	/**
	 * @see AsyncListener#onError(AsyncEvent)
	 */
	public void onError(AsyncEvent arg0) throws java.io.IOException {

	}

	/**
	 * @see HttpSessionIdListener#sessionIdChanged(HttpSessionEvent, String)
	 */
	public void sessionIdChanged(HttpSessionEvent arg0, String arg1) {

	}

	/**
	 * @see ServletRequestAttributeListener#attributeAdded(ServletRequestAttributeEvent)
	 */
	public void attributeAdded(ServletRequestAttributeEvent arg0) {

	}

	/**
	 * @see AsyncListener#onTimeout(AsyncEvent)
	 */
	public void onTimeout(AsyncEvent arg0) throws java.io.IOException {

	}

	/**
	 * @see HttpSessionAttributeListener#attributeReplaced(HttpSessionBindingEvent)
	 */
	public void attributeReplaced(HttpSessionBindingEvent arg0) {

	}

	/**
	 * @see HttpSessionActivationListener#sessionWillPassivate(HttpSessionEvent)
	 */
	public void sessionWillPassivate(HttpSessionEvent arg0) {

	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent arg0) {

		System.out.println("Hi there, just getting started up. This may take some time!");
		try {
			HibernateUtil.buildSessionFactory();
			System.out.println("Before loading list");
			ServletContext context = arg0.getServletContext();
			// Initializing TimerTask to fetch from DB after every 10 minutes
			TimerTask task = new DbFetchTask(context);
			TimerTask anotherTask = new UpdateBigramTask();
			timer = new Timer();
			timer.schedule(task, 300000, 1200000);
			timer.schedule(anotherTask, 60000, 60000);
			Stopwatch sw = new Stopwatch();
			Loadlist ls = new Loadlist();
			TreeSet<Wordlist> ts = ls.loadWordList();
			int sz = ts.size(), step = 0;
			System.out.println("After loading list");
			Trie trie = new Trie();
			String arr[] = new String[sz];
			HashSet<String> hs = new HashSet<>();
			HashSet<String> trigramSet = new HashSet<>();
			System.out.println("Putting it in Trie");
			for (Wordlist wl : ts) {
				String s = wl.getWords();
				trie.addWord(s);
				arr[step++] = s;
				hs.add(s);
				if (StringUtils.countMatches(s, " ") >= 2)
					trigramSet.add(s);
			}
			Bigram bigram = Bigram.getInstance();
			bigram.initializeTraining(hs);
			DynamicTrigram dt = DynamicTrigram.getInstance();
			dt.initializeTraining(trigramSet);
			DynamicBigram db = DynamicBigram.getInstance();
			db.initializeTraining(hs);
			double time = sw.elapsedTime();
			System.out.println("Elapsed time : " + time);
			context.setAttribute("Products", trie);
			context.setAttribute("ProductArray", arr);
			context.setAttribute("Categories", hs);
			System.out.println("Products got loaded successfully during StartUp!");
		} catch (Throwable t) {
			if (t != null && t instanceof OutOfMemoryError) {
				System.out.println(
						"Too many products! Please contact tech team for more information! Server startup failed. Terminating program."
								+ " Please increase your Virtual Machine(VM) space in Apache Tomcat for successful continuation! ");
				System.exit(0);
			} else {
				System.out.println("Unexpected exception : ");
				t.printStackTrace();
				System.exit(0);
			}
		}
	}

	/**
	 * @see ServletContextAttributeListener#attributeAdded(ServletContextAttributeEvent)
	 */
	public void attributeAdded(ServletContextAttributeEvent arg0) {

	}

	/**
	 * @see AsyncListener#onComplete(AsyncEvent)
	 */
	public void onComplete(AsyncEvent arg0) throws java.io.IOException {

	}

	/**
	 * @see ServletRequestListener#requestDestroyed(ServletRequestEvent)
	 */
	public void requestDestroyed(ServletRequestEvent arg0) {

	}

	/**
	 * @see ServletRequestAttributeListener#attributeRemoved(ServletRequestAttributeEvent)
	 */
	public void attributeRemoved(ServletRequestAttributeEvent arg0) {

	}

	/**
	 * @see AsyncListener#onStartAsync(AsyncEvent)
	 */
	public void onStartAsync(AsyncEvent arg0) throws java.io.IOException {

	}

	/**
	 * @see HttpSessionBindingListener#valueBound(HttpSessionBindingEvent)
	 */
	public void valueBound(HttpSessionBindingEvent arg0) {

	}

	/**
	 * @see ServletRequestListener#requestInitialized(ServletRequestEvent)
	 */
	public void requestInitialized(ServletRequestEvent arg0) {

	}

	/**
	 * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent arg0) {

	}

	/**
	 * @see HttpSessionActivationListener#sessionDidActivate(HttpSessionEvent)
	 */
	public void sessionDidActivate(HttpSessionEvent arg0) {

	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		timer.cancel();
		HibernateUtil.shutdown();
	}

	/**
	 * @see ServletRequestAttributeListener#attributeReplaced(ServletRequestAttributeEvent)
	 */
	public void attributeReplaced(ServletRequestAttributeEvent arg0) {

	}

	/**
	 * @see HttpSessionAttributeListener#attributeAdded(HttpSessionBindingEvent)
	 */
	public void attributeAdded(HttpSessionBindingEvent arg0) {

	}

	/**
	 * @see HttpSessionAttributeListener#attributeRemoved(HttpSessionBindingEvent)
	 */
	public void attributeRemoved(HttpSessionBindingEvent arg0) {

	}

	/**
	 * @see ServletContextAttributeListener#attributeReplaced(ServletContextAttributeEvent)
	 */
	public void attributeReplaced(ServletContextAttributeEvent arg0) {

	}

	/**
	 * @see HttpSessionBindingListener#valueUnbound(HttpSessionBindingEvent)
	 */
	public void valueUnbound(HttpSessionBindingEvent arg0) {

	}

}
