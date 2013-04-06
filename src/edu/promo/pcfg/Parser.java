package edu.promo.pcfg;

import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParserQuery;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;

/**
 * This class provides the top-level API and command-line interface to train and test
 * the Stanford Parser on a completely unlabeled training document via an initial
 * seed model.
 *
 * @author Heath Vinicombe
 */
public class Parser {

	private LexicalizedParser lp;
	
	// Parses a document into a Treebank
	private Treebank parseDocument(String docFilename, LexicalizedParser lp) {
		
		MemoryTreebank mtb = new MemoryTreebank();
		for (List<HasWord> sentence : new DocumentPreprocessor(docFilename)) {
		      Tree parse = lp.apply(sentence);
		      mtb.add(parse);
		}
		return mtb;
	}
	
	private Options initStanfordOptions() {
		Options op = new Options();
		op.doDep = false;
		op.doPCFG = true;
		op.setOptions("-goodPCFG", "-evals", "tsv");
		op.trainOptions.printAnnotatedRuleCounts = false;
		op.trainOptions.printAnnotatedStateCounts = false;
		op.trainOptions.printStates = false;
		op.testOptions.verbose  = false;
		op.testOptions.printAllBestParses = false;
		return op;
		
	}
	
	/**
	   * Trains the parser on an unlabeled training data via an initial seed
	   * model.
	   *
	   * @param trainFilename Filename of the training document
	   * @param seedModelFileName Filename of the seed model which is a previously
	   *                          trained <code>edu.stanford.nlp.LexicalizedParser</code>.
	   */
	public void trainSemiSupervised(String trainFilename, String seedModelFileName) {
		
		LexicalizedParser lpSeed = LexicalizedParser.loadModel(seedModelFileName);
		Treebank tb = parseDocument(trainFilename, lpSeed);
		Options op = initStanfordOptions();
		lp = LexicalizedParser.trainFromTreebank(tb, op);
		
	}

	/**
	   * Tests the trained model on a document and returns the log probability.
	   *
	   * @param testFilename Filename of the test document
	   * 
	   */
	public double test(String testFilename) {
		
		Treebank tb = parseDocument(testFilename, lp);
		LexicalizedParserQuery lpq = lp.parserQuery();
		double prob = lpq.testOnTreebank(tb);
		return prob;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
