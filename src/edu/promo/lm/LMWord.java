package edu.promo.lm;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.lm.UniformBoundaryLM;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;

// To Do:
// Normalize probabilities
// Modelling unknown words
// Writing / reading to file

public class LMWord extends TokenizedLM {

	static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
    static final SentenceModel SENTENCE_MODEL  = new IndoEuropeanSentenceModel(); // Is IndoEuro better?
    static final SentenceChunker SENTENCE_CHUNKER = new SentenceChunker(TOKENIZER_FACTORY,SENTENCE_MODEL);
    private double LOG2_TO_LOGE = 0.69314718;
    
	public LMWord(int maxNGram, double lambdaFactor) {
	
		/*super(new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE), 
					maxNGram, new TokenizedLM(TOKENIZER_FACTORY, 1), 
				UniformBoundaryLM.ZERO_LM, lambdaFactor);*/
		
		super(new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE), 
				maxNGram, new NGramBoundaryLM(5), 
			UniformBoundaryLM.ZERO_LM, lambdaFactor);
		
	}
	
	public void train(String seq) {
		
		super.train(seq);
	}

	public void train(File file) {
		
		String text ="";
		try {
			text = Files.readFromFile(file,"ISO-8859-1");
		} catch (IOException e) {
			System.out.println("Error reading file: " + file.getName());
			return;
		}
		String[] textSentences = textToSentences(text);
		for (String sentence : textSentences) {
			train(sentence);
		}
	}

	public double test(String seq) {
		
		double prob = super.log2Estimate(seq);
		prob = prob * LOG2_TO_LOGE;
		System.out.println("Sequence : " + seq);
		System.out.println("Likelihood : " + prob);
		return prob;
	}
	
	public double test(File file) {
		
		String text ="";
		try {
			text = Files.readFromFile(file,"ISO-8859-1");
		} catch (IOException e) {
			System.out.println("Error reading file: " + file.getName());
			return 0;
		}
		double prob = 0;
		String[] textSentences = textToSentences(text);
		for (String sentence : textSentences) {
			prob += test(sentence);
		}
		return prob;
	}

	public String[] textToSentences(String text) {
		
		Chunking chunking = SENTENCE_CHUNKER.chunk(text.toCharArray(),0,text.length());
		Set<Chunk> sentences = chunking.chunkSet();
		String slice = chunking.charSequence().toString();
		String[] textSentences = new String[sentences.size()];
		
		int i = 0;
		for (Iterator<Chunk> it = sentences.iterator(); it.hasNext(); ) {
		    Chunk sentence = it.next();
		    int start = sentence.start();
		    int end = sentence.end();
		    textSentences[i] = slice.substring(start,end);
		    i++;
		}
		return textSentences;
	}
	
	public void load () {
		
		
	}
	
	public void save () {
		
		
	}
	public static void main(String[] args) {
		

	}

}
