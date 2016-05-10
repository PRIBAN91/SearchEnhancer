package com.enhancer.util;

import java.util.TimerTask;
import javax.servlet.ServletContext;

public class DbFetchTask extends TimerTask {

	private ServletContext context;

	public DbFetchTask(ServletContext cntxt) {
		context = cntxt;
	}

	@Override
	public void run() {
		System.out.println("Fetching fresh list from DB now.");
		UpdateFromDb luw = new UpdateFromDb();
		luw.fetchFreshList(context);
	}
}