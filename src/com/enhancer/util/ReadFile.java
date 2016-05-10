package com.enhancer.util;

import java.io.*;
import java.util.*;

public class ReadFile {

	public void read(ArrayList<String> ar) {

		BufferedReader br = null;
		TreeSet<String> ts = new TreeSet<>();

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader("C:\\Users\\PRITAM\\Desktop\\EcommProj\\Words.txt"));

			while ((sCurrentLine = br.readLine()) != null) {
				ts.add(sCurrentLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		ar.addAll(ts);
	}
}