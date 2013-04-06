
package edu.promo.lm;

public class DemoLM {
		
	public static void main(String[] args) {
	
		// Run demo
		LMWord lmWord = new LMWord(1, 1000);
		lmWord.train("dog bit man");
		lmWord.train("man bit sausage");
		lmWord.train("dog bit sausage");
		lmWord.train("man bought sausage");
		lmWord.train("man bought dog");
		lmWord.test("man");
		lmWord.test("dog");
		lmWord.test("sausage");
		lmWord.test("bit");
		lmWord.test("bought");
		lmWord.test("");
		lmWord.test("dog bit");
		lmWord.test("man bit dog");
		System.out.println(lmWord);
	}

}
