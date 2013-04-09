package edu.promo.lm;

import java.io.File;
import java.io.IOException;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.util.Files;

//To Do:
//Pruning
//Writing / reading to file

public class LMChar extends NGramProcessLM {

    private double LOG2_TO_LOGE = 0.69314718;
    
	public LMChar(int maxNGram, int numChars, double lambdaFactor) {
		
		super(maxNGram,numChars,lambdaFactor);
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
		train(text);
	}
	
	public double test(String seq) {
		
		double prob = super.log2Estimate(seq) / seq.length();
		prob = prob * LOG2_TO_LOGE;
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
		double prob = test(text);
		return prob;
	}

	public void load () {
		
		
	}
	
	public void save () {
		
		
	}
	public static void main(String[] args) {
		

	}

}
