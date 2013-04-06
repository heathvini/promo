package edu.promo.pcfg;

public class DemoPCFG {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Running PCFG Demo");
		Parser pcfg = new Parser();
		pcfg.trainSemiSupervised("data/sampleDoc.txt", "data/englishPCFG.ser.gz");
		double prob = pcfg.test("data/sampleDoc.txt");
		System.out.println("Probability of test corpus: " + prob);
	}

}
