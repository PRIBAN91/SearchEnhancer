package com.enhancer.controller;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.*;
import com.enhancer.bo.*;
import com.enhancer.nlp.MachineLearning;
import com.enhancer.util.*;
import java.util.*;

/**
 * Servlet implementation class SearchCall
 */
@WebServlet(description = "This would be called on each key press in Search Bar", urlPatterns = { "/SearchCall" })
public class SearchCall extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchCall() {
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
		SearchOnKeyup luw = new SearchOnKeyup();
		MachineLearning learn = new MachineLearning();
		String searchStr = request.getParameter("searchString"), prev = "";
		if (searchStr != null) {
			int lim = 8, needed = 0, len = searchStr.length();
			if (len >= 2) {
				boolean checkContains = true, desparatePrev = false, prevSuggChk = true;
				boolean spaceEncountered = searchStr.contains(" ");
				if (len == 2) {
					session.setAttribute("SuggestionList", null);
					session.setAttribute("PrevSuggListChk", false);
				}
				if (len > 2) {
					checkContains = (Boolean) (session.getAttribute("CheckContains") != null
							? session.getAttribute("CheckContains") : true);
					prev = (String) (session.getAttribute("PrevSearch") != null ? session.getAttribute("PrevSearch")
							: "");
				}
				Stopwatch sw = new Stopwatch();
				List<String> list = new ArrayList<>();
				searchStr = searchStr.trim().toLowerCase();
				System.out.println("Search string : " + searchStr);
				Trie trie = (Trie) context.getAttribute("Products");
				list = trie.findCompletions(searchStr);
				if (!list.isEmpty()) {
					if (spaceEncountered)
						list = learn.calculateMaxLikeEst(list, searchStr);
					else
						Collections.sort(list);
					if (list.size() > lim)
						list = list.subList(0, lim);
				} else {
					prevSuggChk = (Boolean) (session.getAttribute("PrevSuggListChk") != null
							? session.getAttribute("PrevSuggListChk") : false);
					list = (List<String>) (session.getAttribute("SuggestionList") != null
							? session.getAttribute("SuggestionList") : new ArrayList<>());
					list = luw.checkPrevSuggestion(list, prevSuggChk, searchStr);
				}
				String sarr[] = (String[]) context.getAttribute("ProductArray");
				needed = lim - list.size();
				if (len <= 10 && !spaceEncountered) {
					if (prev.equals("") || prev.equals(searchStr) || !searchStr.startsWith(prev) || checkContains) {
						checkContains = true;
						int count = luw.moreSuggestionNeeded(list, sarr, searchStr, len, needed);
						if (needed > 0 && count == 0)
							checkContains = false;
						if ((needed > 0 && count > 0) || checkContains || prev.equals(""))
							prev = searchStr;
					}
				}
				if (list.isEmpty()) {
					desparatePrev = true;
					list = luw.desperateSearch(trie, list, sarr, prev, searchStr, len, lim);
				}

				if (list.isEmpty()) {
					if (spaceEncountered) {
						list = (List<String>) (session.getAttribute("SuggestionList") != null
								? session.getAttribute("SuggestionList") : new ArrayList<>());
						list = learn.calculateMaxLikeEst(list, searchStr);
					}
				}
				System.out.println(list);
				System.out.println("Time elapsed in this search : " + sw.elapsedTime());
				if (!list.isEmpty())
					session.setAttribute("SuggestionList", list);
				session.setAttribute("SpacePresent", spaceEncountered);
				session.setAttribute("PrevSuggListChk", prevSuggChk);
				session.setAttribute("CheckContains", checkContains);
				session.setAttribute("PrevSearch", prev);
				session.setAttribute("DesparateSearch", desparatePrev);

				JSONObject obj = new JSONObject();
				JSONArray array = new JSONArray(list);
				try {
					obj.put("SuggestionList", array);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				PrintWriter out = response.getWriter();
				out.print(obj.toString());
				out.close();
			}
		}
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
