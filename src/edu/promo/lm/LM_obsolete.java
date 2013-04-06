package edu.promo.lm;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;

import com.aliasi.io.FileExtensionFilter;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.TrieCharSeqCounter;

import com.aliasi.stats.Statistics;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.lang.reflect.InvocationTargetException;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class LM_obsolete extends AbstractCommand {

    TrainingHandler mTextHandler;
    NGramProcessLM mLM;
    PrintWriter mPrinter = null;
    long mMaxTrainingCharCount;
    Parser<ObjectHandler<CharSequence>> mTextParser;
    FileFilter mFileExtensionFilter;

    int mNGram;
    int mNumChars;

    double[] mLambdas;

    int mSampleFrequency;
    double[][] mSamples;
    int mSampleIndex = 0;
    long mCharCount = 0;

    Runtime mRuntime;
    long mStartTime;
    
    public LM_obsolete(String[] args) 
            throws ClassNotFoundException, NoSuchMethodException, 
                   InstantiationException, IllegalAccessException, InvocationTargetException {

    		// Get default parameters from user input and initialize values
            super(args,DEFAULT_PARAMS);
            File outFile = getArgumentFile(REPORT_FILE_PARAM);
            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(outFile);
                OutputStreamWriter osWriter = new OutputStreamWriter(fileOut);
                mPrinter = new PrintWriter(osWriter);
            } catch (IOException e) {
                throw new IllegalArgumentException("IOException=" + e);
            }
            // printParams();

            // Initialize learning parameters
            mNGram = getArgumentInt(MAX_NGRAM_PARAM);
            mNumChars = getArgumentInt(NUM_CHARS_PARAM);
            String[] lambdaNames = getArgument(LAMBDA_FACTORS_PARAM).split(",");
            if (lambdaNames.length < 1)
                illegalArgument(LAMBDA_FACTORS_PARAM,
                                "Must supply at least one lambda factor.");
            mLambdas = new double[lambdaNames.length];
            for (int i = 0; i < mLambdas.length; ++i)
                mLambdas[i] = Double.valueOf(lambdaNames[i]);
            mLM = new NGramProcessLM(mNGram,mNumChars); // The actual ngram estimator

            mMaxTrainingCharCount = getArgumentLong(MAX_TRAINING_CHAR_COUNT);

            mSamples = new double[mLambdas.length][getArgumentInt(SAMPLE_SIZE)];
            mSampleIndex = 0;
            mSampleFrequency = getArgumentInt(SAMPLE_FREQUENCY);

            mTextHandler = new TrainingHandler(); // Does the actual work of training the model
    		
    		// Creates a text parser with the class specified as a user parameter
            String textParserClassName = getExistingArgument(TEXT_PARSER_PARAM);
            @SuppressWarnings({"unchecked","rawtypes"})
            Parser<ObjectHandler<CharSequence>> textParser
                = (Parser<ObjectHandler<CharSequence>>)
                Class
                .forName(textParserClassName)
                .getConstructor(new Class[0]) // 0 parameters
                .newInstance(new Object[0]);
            mTextParser = textParser;
            mTextParser.setHandler(mTextHandler);
            String fileExtension = getExistingArgument(FILE_EXTENSION_PARAM);
            mFileExtensionFilter = new FileExtensionFilter(fileExtension,false);

            mRuntime = Runtime.getRuntime();
            mStartTime = System.currentTimeMillis();
        }

    public void run() {
        try {
            train();
            //printTotals();
            //printTopNGrams();
        } catch (Exception e) {
            //println("Exception=" + e);
            e.printStackTrace(System.out);
            e.printStackTrace(mPrinter);
        } finally {
            Streams.closeQuietly(mPrinter);
        }
    }
    
    void train() throws IOException {
        /*println("");
        println("LEARNING CURVE");
        print("#CHARS, ELAPSED(s), TOTAL_MEM(MB), FREE_MEM(MB), TOT-FREE(MB)");
        for (int i = 0; i < mLambdas.length; ++i)
            print(", MEAN(" + mLambdas[i] + "), DEV(" + mLambdas[i] + ")");
        println("");*/

        for (int i = 0; i < numBareArguments(); ++i) {
            File dir = new File(getBareArgument(i));
            if (!dir.isDirectory()) {
                String msg = "Arguments must be directories."
                    + "Found arg " + i + "=" + dir;
                throw new IllegalArgumentException(msg);
            }
            //println("# Visiting directory=" + dir);
            File[] files = dir.listFiles(mFileExtensionFilter);
            for (int j = 0; j < files.length; ++j)
                trainFile(files[j]);
        }
    }

	// Trains on a single file branching based on the type of file
    void trainFile(File file) throws IOException {
        String fileName = file.getName();
        if (fileName.endsWith(".gz"))
            //trainGZipFile(file);
        	System.out.println("No training function for .gz"); 
        else
            trainTextFile(file);
    }

    void trainTextFile(File file) throws IOException {
        String url = file.toURI().toURL().toString();
        //InputSource in = new InputSource(url);
        //mTextParser.parse(in); // mTextParser parses the file and mTextHandler trains on it
    }
    
    void exit() {
        //println("Hard stop at character=" + mCharCount);
        System.exit(0);
    }
    
    class TrainingHandler implements ObjectHandler<CharSequence> {
        public void handle(CharSequence cSeq) {
            char[] cs = cSeq.toString().toCharArray();
            int start = 0;
            int length = cs.length;
            for (int i = 1; i <= length; ++i) {
                ++mCharCount;
                if (mCharCount > mMaxTrainingCharCount) exit();
                if ((mCharCount % mSampleFrequency) != 0) continue;
                for (int j = 0; j < mLambdas.length; ++j)
                    mSamples[j][mSampleIndex]
                        = -mLM.log2ConditionalEstimate(cs,start,i,
                                                       mNGram,
                                                       mLambdas[j]);
                ++mSampleIndex;
                if (mSampleIndex == mSamples[0].length) {
                    //report();
                    mSampleIndex = 0;
                }
            }
            mLM.train(cSeq);
        }
    }
    
    static final String CORPUS_NAME_PARAM = "corpusName";
    static final String FILE_EXTENSION_PARAM = "fileExtension";
    static final String TEXT_PARSER_PARAM = "textParser";

    static final String MAX_TRAINING_CHAR_COUNT = "maxTrainingChars";

    static final String MAX_NGRAM_PARAM = "maxNGram";
    static final String NUM_CHARS_PARAM = "numChars";

    static final String LAMBDA_FACTORS_PARAM = "lambdaFactors";

    static final String SAMPLE_SIZE = "sampleSize";
    static final String SAMPLE_FREQUENCY = "sampleFreq";
    static final String REPORT_FILE_PARAM = "reportFile";

    static final Properties DEFAULT_PARAMS = new Properties();
    static {
        DEFAULT_PARAMS.setProperty(MAX_NGRAM_PARAM,"5");
        DEFAULT_PARAMS.setProperty(SAMPLE_SIZE,"1000");
        DEFAULT_PARAMS.setProperty(SAMPLE_FREQUENCY,"1000");
        DEFAULT_PARAMS.setProperty(LAMBDA_FACTORS_PARAM,"1.4,5.6,16");
        String reportFileName
            = "TrainLM-Report" + System.currentTimeMillis() + ".txt";
        DEFAULT_PARAMS.setProperty(REPORT_FILE_PARAM,reportFileName);
        DEFAULT_PARAMS.setProperty(NUM_CHARS_PARAM,"256");
        DEFAULT_PARAMS.setProperty(FILE_EXTENSION_PARAM,".txt");
        DEFAULT_PARAMS.setProperty(CORPUS_NAME_PARAM,"unk");
        DEFAULT_PARAMS.setProperty(MAX_TRAINING_CHAR_COUNT,
                                   Long.toString(Long.MAX_VALUE));
    }

    public static void main(String[] args) 
        throws IllegalAccessException, 
               ClassNotFoundException, 
               NoSuchMethodException, 
               InstantiationException, 
               InvocationTargetException {

    	System.out.println("Running LM");
        // new LM(args).run();
    }
}
