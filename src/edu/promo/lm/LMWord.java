package edu.promo.lm;

import com.aliasi.lm.TokenizedLM;
import com.aliasi.lm.UniformBoundaryLM;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class LMWord extends TokenizedLM {

	public LMWord(int maxNGram, double lambdaFactor) {
		
		super(IndoEuropeanTokenizerFactory.INSTANCE, maxNGram, UniformBoundaryLM.ZERO_LM, 
				UniformBoundaryLM.ZERO_LM, lambdaFactor);
		
	}
	
	public void train(String seq) {
		
		super.train(seq);
	}

	public double test(String seq) {
		
		double prob = super.log2Estimate(seq);
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
