package com.enhancer.util;

import java.util.Date;
import java.util.TimerTask;

import com.enhancer.nlp.DynamicBigram;

public class UpdateBigramTask extends TimerTask {

	@Override
	public void run() {
		DynamicBigram db = DynamicBigram.getInstance();
		db.updatePerplexity();
		System.out.println("Bigram perplexity updated at : " + new Date());
	}

}
