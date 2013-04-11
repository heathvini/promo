package edu.promo.expt;

import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.util.AbstractCommand;

import edu.promo.lm.AbstractLM;
import edu.promo.lm.LMChar;
import edu.promo.lm.LMWord;
import edu.promo.pcfg.Parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.Random;

public class RunPCFGExpts extends AbstractCommand {

	private String dir;
	private String modelFilename;
	private String seedFilename;
	private String resultsFilename;
	private int numFolds;
	private int seed;
	
	private Parser pcfg;
	
    public RunPCFGExpts(String[] args)  {

    		// Get default parameters from user input and initialize values
            super(args,DEFAULT_PARAMS);

            // Initialize learning parameters
            dir = getArgument("dir");
            modelFilename = getArgument("modelFilename");
            resultsFilename = getArgument("resultsFilename");
            seedFilename = getArgument("seedFilename");
            numFolds = getArgumentInt("numFolds");
            seed = getArgumentInt("seed");
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
            	
            	pcfg = new Parser(seedFilename);
        		pcfg.setResultsOutput(fw);
        		
                corpus.setFold(fold);
                corpus.visitTrain(pcfg.train());
                corpus.visitTest(pcfg.test());
                pcfg.save(modelFilename + "_" + fold);
            }
    }
 
    static final Properties DEFAULT_PARAMS = new Properties();
    static {
        DEFAULT_PARAMS.setProperty("dir","/");
        DEFAULT_PARAMS.setProperty("seedFilename","data/englishPCFG.ser.gz");
        DEFAULT_PARAMS.setProperty("modelFilename","model");
        DEFAULT_PARAMS.setProperty("resultsFilename","data/results.csv");
        DEFAULT_PARAMS.setProperty("numFolds","10");
        DEFAULT_PARAMS.setProperty("seed","1");  
    }

    public static void main(String[] args) 
        throws IllegalAccessException, 
               ClassNotFoundException, 
               NoSuchMethodException, 
               InstantiationException, 
               InvocationTargetException {

    	System.out.println("Running PCFG");
    	RunLMExpts exp = new RunLMExpts(args);
    	exp.run();
    }
}
