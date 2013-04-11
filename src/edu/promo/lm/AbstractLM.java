package edu.promo.lm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.lm.LanguageModel;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;

public abstract class AbstractLM {

	static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
    static final SentenceModel SENTENCE_MODEL  = new IndoEuropeanSentenceModel(); // Is IndoEuro better?
    static final SentenceChunker SENTENCE_CHUNKER = new SentenceChunker(TOKENIZER_FACTORY,SENTENCE_MODEL);
    static final double LOG2_TO_LOGE = 0.69314718;
    protected LanguageModel lm;
    OutputStreamWriter osw;
    
	protected AbstractLM() {}
	
	protected AbstractLM(String fname) {
		
		try {
			FileInputStream fis = new FileInputStream(fname);
			ObjectInputStream ois = new ObjectInputStream(fis);
			lm = (LanguageModel) ois.readObject();
			ois.close();
			fis.close();
		} catch (FileNotFoundException e) {
			System.out.println("Could not find file: " + fname);
		} catch (IOException e) {
			System.out.println("Could not read model from file: " + fname);
		} catch (ClassNotFoundException e) {
			System.out.println("Could not find class");
		}
		
	}

	public void trainSentence(String seq) {
		
		((LanguageModel.Dynamic) lm).train(seq);
	}

	public void train(String filename) {
		
		String text ="";
		try {
			File file = new File(filename);
			text = Files.readFromFile(file,"ISO-8859-1");
		} catch (IOException e) {
			System.out.println("Error reading file: " + filename);
			return;
		}
		String[] textSentences = textToSentences(text);
		for (String sentence : textSentences) {
			trainSentence(sentence);
		}
		System.out.println("Training LM...");
	}

	public ObjectHandler<String> train() {
		
		class TrainHandler implements ObjectHandler<String> {
			public void handle(String s) {
				train(s);
			}
		}
		return new TrainHandler();
	}
	
	public double test(String seq) {
		
		double prob = lm.log2Estimate(seq);
		prob = prob * LOG2_TO_LOGE;
		System.out.println("Testing LM...");
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
		prob = prob / calculateSize(textSentences);
		if (osw != null) {
			try {
				osw.append(file.getName() + "," + prob + "\n");
				osw.flush();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		return prob;
	}

	public ObjectHandler<String> test() {
		
		class TestHandler implements ObjectHandler<String> {
			public void handle(String s) {
				test(new File(s));
			}
		}
		return new TestHandler();
	}

	protected abstract int calculateSize(String[] sentences);
	
	public abstract void prune(int minSize);
	
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
	
	public void save (String fname) {
		
		try {
			FileOutputStream fos = new FileOutputStream(fname);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			((LanguageModel.Dynamic) lm).compileTo(oos);
			oos.close();
			fos.close();
		} catch (Exception e) {
			System.out.println("Could not write to file: " + fname);
		}
	}
	
	public void setResultsOutput(OutputStreamWriter osw) {
		
		this.osw = osw;
	}
	
	public LanguageModel getLM() {
		
		return lm;
	}
}
