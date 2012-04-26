package features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import services.InsertDataSrv;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;
import entities.CohesionDetails;
import generics.StopWords;

public class SentenceCohesion extends Feature{

	// Cohesion features for POS overlap
	private static int posNPCohesion = 0;
	private static int posADJCohesion = 0;
	private static int posAVBCohesion = 0;
	private static int posVBCohesion = 0;
	
	// Cohesion features for words overlap
	private static int wordALLCohesion = 0;
	private static int wordCohesion = 0;
	
	private static SentenceCohesion sentenceCohesion = null;
	private static CohesionDetails cd;
	
	// Temporary arrays used for storing distinct words within a sentence
	private static HashSet<String> array1 = new HashSet<String>();
	private static HashSet<String> array2 = new HashSet<String>();

	// Arrays used for storing sentences within a article
	private static LinkedList<String> sentences = new LinkedList<String>();
	
	private SentenceCohesion() {
	}

	public static SentenceCohesion getInstance() {
		if (sentenceCohesion == null)
			sentenceCohesion = new SentenceCohesion();
		return sentenceCohesion;
	}

	/**
	 * Compute Sentence overlap features for article content strPOS, 
	 * and create an entry in CohesionDetails file in database
	 */
	public void calculateFeatureVector(AbstractDB object, StringBuilder strPOS, File file)
			throws IOException {
		// TODO Auto-generated method stub
		
		// Compute the cohesion for article content over POS and words
		// May return false if POS tagged file doesn't exist
		if(!computeCohesion(object))
			return;
		
		InsertDataSrv.beginTransaction();
		
		cd = new CohesionDetails();
		cd.setId(((ArticleDetails) object).getId());
		
		// Update the attributes
		cd.setPosNP(posNPCohesion);
		cd.setPosADJ(posADJCohesion);
		cd.setPosAVB(posAVBCohesion);
		cd.setPosVB(posVBCohesion);
		
		cd.setWordISW(wordALLCohesion);
		cd.setWordESW(wordCohesion);
		
		// Update the row in the database for the article
		InsertDataSrv.save(cd);
		InsertDataSrv.commit();
	}
	
	public static boolean computeCohesion(AbstractDB article) throws IOException {
		
		String filename = ((ArticleDetails) article).getId() + ".txt";
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		parentDirectoryPath.append(File.separator).append("files"); 
		
		File f = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("posDocs").append(File.separator).append(filename).toString());
		
		String tempStr = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			while ((tempStr = br.readLine()) != null) {
				sentences.add(tempStr);
			}
			br.close();
		}catch(FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
		// Nouns and pronouns
		posNPCohesion = sentPOSCohesion("NN NNS NNP NNPS PRP PRP$".split(" "));
		// Adjectives
		posADJCohesion = sentPOSCohesion("JJ JJR JJS".split(" "));
		// Adverbs
		posAVBCohesion = sentPOSCohesion("RB RBR RBS".split(" "));
		// Verbs
		posVBCohesion = sentPOSCohesion("VB VBD VBG VBN VBP VBZ".split(" "));
		
		// Including stop words
		wordALLCohesion = sentWordCohesion(false);
		
		// Excluding stop words
		wordCohesion = sentWordCohesion(true);
		
		if(ConfigurationConstants.debugMode)	{
			System.out.println("1: "+posNPCohesion);
			System.out.println("2: "+posADJCohesion);
			System.out.println("3: "+posAVBCohesion);
			System.out.println("4: "+posVBCohesion);
			System.out.println("5: "+wordALLCohesion);
			System.out.println("6: "+wordCohesion);
		}
		sentences.clear();
		return true;
	}

	/**
	 * Compute continuity in two sentences over POS
	 */
	public static int sentPOSCohesion(String[] posTags) {
		
		int cohesion = 0;
		HashSet<String> pos = new HashSet<String>();
		for(String s: posTags)
			pos.add(s);
		
		String[] temp1 = sentences.get(0).split(" ");
		// Extract provided POS tag words from sentence 1
		for (String temp : temp1) {
			String[] str = temp.split("/");
			if (str.length == 2 && pos.contains(str[1]))
				array1.add(str[0]);
		}
		
		for (int k = 1; k < sentences.size(); k++) {
			
			temp1 = sentences.get(k).split(" ");
			// Extracts provided POS tag words from sentence 2
			for (String temp : temp1) {
				String[] str = temp.split("/");
				
				if (str.length == 2 && pos.contains(str[1]))
					array2.add(str[0]);
			}
			
			// Compare elements of sentences for overlapping elements
			Set<String> intersection = new HashSet<String>(array1);
			intersection.retainAll(array2);
			cohesion += intersection.size();
			
			array1.clear();
			array1.addAll(array2);
			array2.clear();
		}
		array1.clear();
		return cohesion;
	}

	/**
	 * Compute continuity in two sentences over words
	 */
	public static int sentWordCohesion(boolean stopWords) {
		int cohesion = 0;
		boolean stopWordFlag = false;

		String[] temp1 = sentences.get(0).split(" ");
		// Excluding all the words containing special characters and numbers
		for (String temp : temp1) {
			String str = temp.split("/")[0];
			if(!str.matches(".*[0-9].*") && !str.matches(".*[\\W].*")){		
				if(stopWords)	{
					stopWordFlag = false;
					// Excluding all the stop words
					for(String sw: StopWords.stopWords){
						if(str.compareTo(sw)==0){
							stopWordFlag = true;
							break;
						}
					}
				}
				if (!stopWordFlag) {
					array1.add(str.toLowerCase());
				}
			}
		}
		
		for (int k = 1; k < sentences.size(); k++) {
			
			temp1 = sentences.get(k).split(" ");
			// Excluding all the words containing special characters and numbers
			for (String temp : temp1) {
				String str = temp.split("/")[0];
				
				if(!str.matches(".*[0-9].*") && !str.matches(".*[\\W].*")){	
					if(stopWords)
					{
						stopWordFlag = false;
						// Excluding all the stop words
						for(String sw: StopWords.stopWords){
							if(str.compareTo(sw)==0){
								stopWordFlag = true;
								break;
							}
						}
					}
					if(!stopWordFlag) {
						array2.add(str.toLowerCase());
					}
				}
			}
			
			
			// Compare elements of sentences for overlapping elements
			Set<String> intersection = new HashSet<String>(array1);
			intersection.retainAll(array2);
			cohesion += intersection.size();
			
			array1.clear();
			array1.addAll(array2);
			array2.clear();
		}
		array1.clear();
		return cohesion;
	}
}
