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
@WebServlet(description = "This would be called when Enter is pressed after typing in Search Bar", urlPatterns = {
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
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		ServletContext context = request.getServletContext();
		HttpSession session = request.getSession();
		List<String> list = new ArrayList<>();
		List<String> lst = new ArrayList<>();
		String searchStr = request.getParameter("searchString");
		int lim = 6, needed = 0;
		if (searchStr != null) {
			searchStr = searchStr.toLowerCase().trim();
			HashSet<String> categorySet = (HashSet<String>) context.getAttribute("Categories");
			int len = searchStr.length();
			boolean spacePresentInSugg = false;
			System.out.println("In enter, Search String  : " + searchStr);
			searchStr = searchStr.toLowerCase();
			SearchOnEnter luw = new SearchOnEnter();
			Stopwatch sw = new Stopwatch();
			if (categorySet.contains(searchStr)) {
				System.out.println("In Hashset contains.");
				list.add(searchStr);
				luw.updateNgrams(searchStr);
			} else {
				String arr[] = (String[]) context.getAttribute("ProductArray");
				boolean spacePresent = (Boolean) session.getAttribute("SpacePresent");
				lst = (List<String>) session.getAttribute("SuggestionList");
				if (!lst.isEmpty()) {
					if (spacePresent)
						list = lst;
					else {
						spacePresentInSugg = luw.isSpacePresentInSugg(list);
						if (spacePresentInSugg) {
							System.out.println("In word break.");
							WordBreak wb = new WordBreak();
							list = wb.wordBreakUtil(list, searchStr);
						} else {
							System.out.println("In previous suggestion for enter.");
							list = luw.findKeywordSuggestion(lst, searchStr, len, lim);
						}
					}
				}
				needed = (lim - list.size()) < 0 ? 0 : lim - list.size();
				System.out.println("Needed in enter : " + needed);
				if (needed > 0) {
					System.out.println("In completely unknown keyword!");
					list = luw.findUnkownKeyword(list, arr, searchStr, len, lim);
				}
			}

			System.out.println("List : " + list);
			System.out.println("Elapsed time in Enter : " + sw.elapsedTime());
		}
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
