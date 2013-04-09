
package edu.promo.lm;

import java.io.File;

public class DemoLM {
		
	public static void main(String[] args) {
	
		// Run demo
/*		LMChar lmChar = new LMChar(1, 100, 10);
		File file = new File("data/sampleDoc.txt");
		lmChar.train(file);
		double prob = lmChar.test(file);
		System.out.println(lmChar);
		System.out.println(prob);*/
		
		LMWord lmWord = new LMWord(3,10);
		File file = new File("data/sampleDoc2.txt");
		lmWord.train(file);
		double prob = lmWord.test(file);
		System.out.println(lmWord);
		System.out.println(prob);
		System.out.println(lmWord.unknownTokenLM());
		lmWord.test("dog");
		lmWord.test("dogs");
		lmWord.test("doe");
		lmWord.test("red");
	}

}
