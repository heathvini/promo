package edu.promo.lm;

import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.lm.UniformBoundaryLM;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.util.Strings;

public class LMWord extends AbstractLM {
    
	private final int MAX_CHAR_NGRAM_UNKNOWN = 3;

	public LMWord(int maxNGram, double lambdaFactor) {
		
		lm = new TokenizedLM(new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE), 
				maxNGram, new NGramBoundaryLM(MAX_CHAR_NGRAM_UNKNOWN), UniformBoundaryLM.ZERO_LM, lambdaFactor);
	}
	
	public LMWord(String fname) {
		
		super(fname);
	}

	public int calculateSize(String[] sentences) {
		int length = 0;
		for (String sentence: sentences) {
			char[] cs = Strings.toCharArray(sentence);
			Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(cs,0,cs.length);
			String[] tokens = tokenizer.tokenize();
			length += tokens.length + 1;
		}
		return length;
	}

	public void prune(int minSize) {

		((TokenizedLM) lm).sequenceCounter().prune(minSize);
		LanguageModel.Sequence lmUnknown = ((TokenizedLM) lm).unknownTokenLM();
		((NGramBoundaryLM) lmUnknown).substringCounter().prune(3);

	}
	
	public static void main(String[] args) {
		

	}

}
