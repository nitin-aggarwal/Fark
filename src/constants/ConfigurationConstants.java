package constants;

public class ConfigurationConstants {

    //public static final int DATE_VARIABLE = 0;
   
    public static final int WEEK_DIFFERENCE = -7;
   
    public static final boolean INSERTION_FLAG = false;
   
    public static final String STARTING_DATE = "2012-03-18";

    public static final String END_DATE = "2006-02-05";
   
    public static final String FARK_CATEGORY = "sports";
   
    public static final Boolean ARTICLE_CONTENT_INSERTION = true;
   
    public static final String SITE_TYPE = "all";
    
    public static final String TAGGING_TYPE = "pos";
    
    public static final String FILE_DIRECTORY_PATH = "files/";   
    public static final String STATS_DIRECTORY_PATH = "stats/";   
    
    public static final String UNIQUE_FEATURE_DIRECTORY = "distinct";
    public static final String UNIQUE_FEATURECOUNT_DIRECTORY = "count";
    public static final String UNIQUE_SEED_DIRECTORY = "seed";
    
    public static final String WEKA_COUNT_DIRECTORY = "wekaInputCount";
    public static final String WEKA_BINARY_DIRECTORY = "wekaInputBinary";
    public static final String WEKA_TFIDF_DIRECTORY = "wekaInputTFIDF";
    
    public static final String SVM_COUNT_DIRECTORY = "svmInputCount";
    public static final String SVM_BINARY_DIRECTORY = "svmInputBinary";
    public static final String SVM_TFIDF_DIRECTORY = "svmInputTFIDF";
    
    public static final String NGRAM_FEATURES = "unigramPOS";//,"unigramWord","bigramPOS", "trigramPOS"};
   
    // add sites
    public static final String[] sites = {"others"};
    
    public static final boolean debugMode = false;
    
    public static final String[] FOLDER_NAMES = {"unigramWord"};//, "unigramWord","bigramPOS", "trigramPOS"};
    
    public static final String[] VALID_TAGS = {"JJ","JJR","JJS","VBD","VBN","VBD","NNP","NN","NNPS","NNS"};
    
    public static final String[] REPETITIVE_VERBS = {"was","were","had","have","said","did","been","is","am","are"};
}
//"news.yahoo.com", "cnn.com",
