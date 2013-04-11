package edu.promo.expt;

import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.util.AbstractCommand;

import edu.promo.lm.AbstractLM;
import edu.promo.lm.LMChar;
import edu.promo.lm.LMWord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.Random;

public class RunLMExpts extends AbstractCommand {

	private String dir;
	private String modelType;
	private String modelFilename;
	private String resultsFilename;
	private int numFolds;
	private int maxNGrams;
	private int minSize;
	private int seed;
	private double lambda;
	
	private AbstractLM lm;
	
    public RunLMExpts(String[] args)  {

    		// Get default parameters from user input and initialize values
            super(args,DEFAULT_PARAMS);

            // Initialize learning parameters
            dir = getArgument("dir");
            modelType = getArgument("modelType");
            modelFilename = getArgument("modelFilename");
            resultsFilename = getArgument("resultsFilename");
            numFolds = getArgumentInt("numFolds");
            maxNGrams = getArgumentInt("maxNGrams");
            minSize = getArgumentInt("minSize");
            seed = getArgumentInt("seed");
            lambda = getArgumentDouble("lambda");
    }
      
    public void run() {
           		
    		// Build the cross validation corpus
    		XValidatingObjectCorpus<String> corpus = new XValidatingObjectCorpus<String>(numFolds);
    		File trainDir = new File(dir);
            for (File trainingFile : trainDir.listFiles()) {
                corpus.handle(trainingFile.getAbsolutePath());
            }
            corpus.permuteCorpus(new Random(seed));

            // Setup results recording
            FileWriter fw = null;
			try {
				fw = new FileWriter(resultsFilename);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
            // Train and test
            for (int fold = 0; fold < numFolds; ++fold) {
            	
            	// Initialize the language model
        		if (modelType.equals("LMChar")) {
        			lm = new LMChar(maxNGrams, lambda);
        		} else if (modelType.equals("LMWord")) {
        			lm = new LMWord(maxNGrams, lambda);
        		}
        		lm.setResultsOutput(fw);
        		
                corpus.setFold(fold);
                corpus.visitTrain(lm.train());
                lm.prune(minSize);
                corpus.visitTest(lm.test());
                lm.save(modelFilename + "_" + fold);
            }
    }
 
    static final Properties DEFAULT_PARAMS = new Properties();
    static {
        DEFAULT_PARAMS.setProperty("dir","/");
        DEFAULT_PARAMS.setProperty("modelType","LMWord");
        DEFAULT_PARAMS.setProperty("modelFilename","model");
        DEFAULT_PARAMS.setProperty("resultsFilename","data/results.csv");
        DEFAULT_PARAMS.setProperty("numFolds","10");
        DEFAULT_PARAMS.setProperty("lambda","10");
        DEFAULT_PARAMS.setProperty("maxNGrams","1");
        DEFAULT_PARAMS.setProperty("minSize","1");
        DEFAULT_PARAMS.setProperty("seed","1");  
    }

    public static void main(String[] args) 
        throws IllegalAccessException, 
               ClassNotFoundException, 
               NoSuchMethodException, 
               InstantiationException, 
               InvocationTargetException {

    	System.out.println("Running LM");
    	RunLMExpts exp = new RunLMExpts(args);
    	exp.run();
    }
}
