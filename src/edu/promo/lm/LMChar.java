package edu.promo.lm;

import com.aliasi.lm.NGramProcessLM;

public class LMChar extends NGramProcessLM {

	public LMChar(int maxNGram,int numChars, double lambdaFactor) {
		
		super(maxNGram,numChars,lambdaFactor);
	}
	
	public void train(String seq) {
		
		super.train(seq);
	}

	public double test(String seq) {
		
		double prob = super.log2Estimate(seq) / seq.length();
		System.out.println("Sequence : " + seq);
		System.out.println("Likelihood : " + prob);
		return prob;
	}
	
	public void load () {
		
		
	}
	
	public void save () {
		
		
	}
	public static void main(String[] args) {
		

	}

}
