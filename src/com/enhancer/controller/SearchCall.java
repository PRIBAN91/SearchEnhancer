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
import com.enhancer.nlp.MachineLearning;
import com.enhancer.bo.*;
import com.enhancer.util.*;
import java.util.*;
import org.json.*;

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
				boolean spaceEncountered = false, corrFirstWord = true;
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
				spaceEncountered = searchStr.contains(" ");
				System.out.println("Search string : " + searchStr);
				Trie trie = (Trie) context.getAttribute("Products");
				list = trie.findCompletions(searchStr);
				if (!list.isEmpty()) {
					if (spaceEncountered)
						list = learn.calculateMostProbable(list, searchStr);
					else
						Collections.sort(list);
				} else {
					if (!spaceEncountered) {
						prevSuggChk = (Boolean) (session.getAttribute("PrevSuggListChk") != null
								? session.getAttribute("PrevSuggListChk") : false);
						list = (List<String>) (session.getAttribute("SuggestionList") != null
								? session.getAttribute("SuggestionList") : new ArrayList<>());
						list = luw.checkPrevSuggestion(list, prevSuggChk, searchStr);
						corrFirstWord = false;
					}
				}
				String sarr[] = (String[]) context.getAttribute("ProductArray");
				needed = (lim - list.size()) < 0 ? 0 : lim - list.size();
				if (!spaceEncountered) {
					if (prev.equals("") || prev.equals(searchStr) || !searchStr.startsWith(prev) || checkContains) {
						checkContains = true;
						int count = luw.moreSuggestionNeeded(list, sarr, searchStr, needed);
						if (needed > 0 && count == 0)
							checkContains = false;
						if ((needed > 0 && count > 0) || checkContains || prev.equals(""))
							prev = searchStr;
						if (count > 0)
							corrFirstWord = true;
					}

					if (list.isEmpty()) {
						desparatePrev = true;
						list = luw.desperateSearch(trie, list, sarr, prev, searchStr, len, lim);
						corrFirstWord = false;
					}
				}

				if (spaceEncountered) {
					if (list.isEmpty()) {
						list = (List<String>) (session.getAttribute("SuggestionList") != null
								? session.getAttribute("SuggestionList") : new ArrayList<>());
						corrFirstWord = (Boolean) (session.getAttribute("FirstWordCorrect") != null
								? session.getAttribute("FirstWordCorrect") : false);
						if (!corrFirstWord) {
							if (list.size() > (lim >> 1))
								list = list.subList(0, lim >> 1);
							list = learn.checkAnotherContain(sarr, list, 240);
							corrFirstWord = true;
						} else
							list = learn.calculateMaxLikeEst(list, searchStr, corrFirstWord);
					}
				}
				System.out.println(list);
				System.out.println("Time elapsed in this search : " + sw.elapsedTime());
				session.setAttribute("SuggestionList", list);
				session.setAttribute("SpacePresent", spaceEncountered);
				session.setAttribute("PrevSuggListChk", prevSuggChk);
				session.setAttribute("CheckContains", checkContains);
				session.setAttribute("PrevSearch", prev);
				session.setAttribute("DesparateSearch", desparatePrev);
				session.setAttribute("FirstWordCorrect", corrFirstWord);

				JSONObject obj = new JSONObject();
				if (list.size() > lim)
					list = list.subList(0, lim);
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
