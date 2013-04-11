package edu.promo.pcfg;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import com.aliasi.corpus.ObjectHandler;

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
	private String seedModelFileName;
	private OutputStreamWriter osw;
	
	public Parser(String seedModelFileName) {
		
		this.seedModelFileName = seedModelFileName;
	}
	
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
	public void train(String trainFilename) {
		
		LexicalizedParser lpSeed = LexicalizedParser.loadModel(seedModelFileName);
		Treebank tb = parseDocument(trainFilename, lpSeed);
		Options op = initStanfordOptions();
		lp = LexicalizedParser.trainFromTreebank(tb, op);
		
	}

	public ObjectHandler<String> train() {
		
		class TrainHandler implements ObjectHandler<String> {
			public void handle(String s) {
				train(s);
			}
		}
		return new TrainHandler();
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
		prob = prob / calculateSize(tb);
		if (osw != null) {
			try {
				osw.append(testFilename + "," + prob + "\n");
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
				test(s);
			}
		}
		return new TestHandler();
	}

	private int calculateSize(Treebank tb) {
		int size = 0;
		for (Tree t : tb) {
			List<Tree> leaves = t.getLeaves();
			size += leaves.size();
		}
		return size;
	}
	
	public void save(String filename) {
		
		lp.saveParserToSerialized(filename);
	}
	
	public void load(String filename) {
		
		lp = LexicalizedParser.loadModel(filename);
	}
	
	public void setResultsOutput(OutputStreamWriter osw) {
		
		this.osw = osw;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
