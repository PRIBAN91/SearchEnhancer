package com.enhancer.controller;

import java.io.*;
import java.util.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.enhancer.bo.SearchOnEnter;
import com.enhancer.nlp.WordBreak;
import com.enhancer.util.Stopwatch;

/**
 * Servlet implementation class SearchEntered
 */
@WebServlet(description = "This can be called when Enter is pressed after typing the desired string in Search", urlPatterns = {
		"/SearchEntered" })
public class SearchEntered extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchEntered() {
		super();

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response) This method can be called on jquery keyUp function for
	 *      KeyCode 13. Check index.jsp for more details on consuming the
	 *      service. It takes HTTP Request and HTTP Response of Web Server as
	 *      parameters.
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// The response type is JSON array and is same as SearchCall. This time
		// there will be an array named 'SearchedList'.
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		ServletContext context = request.getServletContext();
		HttpSession session = request.getSession();
		List<String> list = new ArrayList<>();
		List<String> lst = new ArrayList<>();
		String searchStr = request.getParameter("searchString");
		int lim = 6, needed = 0;
		if (searchStr != null) {
			// Trim the searched string of all leading and trailing spaces
			searchStr = searchStr.toLowerCase().trim();
			HashSet<String> categorySet = (HashSet<String>) context.getAttribute("Categories");
			int len = searchStr.length();
			boolean spacePresentInSugg = false;
			System.out.println("In enter, Searched String  : " + searchStr);
			searchStr = searchStr.toLowerCase();
			SearchOnEnter luw = new SearchOnEnter();
			Stopwatch sw = new Stopwatch();
			// Check if the searched string is an exact match
			if (categorySet.contains(searchStr)) {
				list.add(searchStr);
				// Increase the Bigram and Trigram(if any) frequency for
				// definitive search
				luw.updateNgrams(searchStr);
			} else {
				String arr[] = (String[]) context.getAttribute("ProductArray");
				boolean spacePresent = (Boolean) session.getAttribute("SpacePresent");
				// Take the Suggestion list from session
				lst = (List<String>) session.getAttribute("SuggestionList");
				if (!lst.isEmpty()) {
					// If there are multiple words in the searched string and is
					// not an exact match, the maximum likelihood estimate has
					// already been calculated in SearchCall
					if (spacePresent)
						list = lst;
					else {
						spacePresentInSugg = luw.isSpacePresentInSugg(lst);
						// If there is a space present in the old suggestion
						// list, there might be a possibility of two jumbled up
						// words without space in between them
						if (spacePresentInSugg) {
							System.out.println("In word break.");
							WordBreak wb = new WordBreak();
							list = wb.wordBreakUtil(lst, searchStr);
							// In this case also it is a definitive search, if
							// list size > 0. So increase the Ngram frequencies.
							if (!list.isEmpty())
								luw.updateNgrams(list.get(0));
						} else {
							System.out.println("In previous suggestion for enter.");
							// Check older suggestion for faster search.
							list = luw.findKeywordSuggestion(lst, searchStr, len, lim);
						}
					}
				}
				needed = (lim - list.size()) < 0 ? 0 : lim - list.size();
				System.out.println("Needed in enter : " + needed);
				// Check if more suggestions needed
				if (needed > 0) {
					System.out.println("In completely unknown keyword!");
					luw.findUnkownKeyword(list, arr, searchStr, len, lim);
				}
			}

			System.out.println("List : " + list);
			System.out.println("Elapsed time in Enter : " + sw.elapsedTime());
		}

		// Put the list in JSON array
		JSONObject obj = new JSONObject();
		if (list.size() > lim)
			list = list.subList(0, lim);
		JSONArray array = new JSONArray(list);
		try {
			obj.put("SearchedList", array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		PrintWriter out = response.getWriter();
		out.print(obj.toString());
		out.close();
		session.invalidate();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
