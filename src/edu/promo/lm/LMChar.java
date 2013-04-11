package edu.promo.lm;

import com.aliasi.lm.NGramBoundaryLM;

public class LMChar extends AbstractLM {

	private final int NUM_CHARS = 100;
	private final char BOUNDARY_CHAR = '\uFFFF';
	
	public LMChar(int maxNGram, double lambdaFactor) {
		
		lm = new NGramBoundaryLM(maxNGram, NUM_CHARS, lambdaFactor, BOUNDARY_CHAR);
	}
	
	public LMChar(String fname) {
		
		super(fname);
	}

	protected int calculateSize(String[] sentences) {
		
		int length = 0;
		for (String sentence: sentences) {
			length += sentence.length() + 1;
		}
		return length;
	}
	
	public void prune(int minSize) {

		((NGramBoundaryLM) lm).substringCounter().prune(minSize);
	}
	
	public static void main(String[] args) {
		

	}

}
