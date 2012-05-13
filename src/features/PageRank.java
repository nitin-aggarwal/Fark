package features;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import constants.ConfigurationConstants;

import entities.AbstractDB;
import entities.ArticleDetails;
import generics.StopWords;

/**
 * Implements PageRank algorithm to identify important keywords within a a document,
 * considering each sentence as a page.
 * Identify importance of sentences, and then consider all repeating
 * keywords from the top on-fourth sentences of the document
 */
public class PageRank extends Feature{

	private static PageRank pageRank = null;
	
	// HashMap used for storing keywords within a sentence within a article, along with its PageRank
	private static HashMap<HashSet<String>,Double> sentences = new HashMap<HashSet<String>,Double>();
	
	// Set used for storing all important distinct words within a sentence
	private static HashSet<String> distinct = new HashSet<String>();
	
	// Set used for storing all important distinct words within a sentence
	private static HashMap<String,Integer> distinctCount = new HashMap<String,Integer>();
		
	
	private static HashSet<String> pos = new HashSet<String>();
	private static String[] posTags = "JJ JJR JJS RB RBR RBS VB VBD VBG VBN VBP VBZ".split(" ");
	
	private static int countSentences = 0;
	private static double damping = 0.85;
	private static int wordThreshold = 1;
	
	private static long articleProcessed = 0;
	
	private PageRank() {
	}

	public static PageRank getInstance() {
		if (pageRank == null)
			pageRank = new PageRank();
		
		for(String s: posTags)
			pos.add(s);
		return pageRank;
	}

	/** Compute UNIGRAM keywords from sentences for article content strPOS  */
	public void calculateFeatureVector(AbstractDB object, StringBuilder strPOS, File file)
			throws IOException {
		
		// Compute the PageRank for article content over POS and words
		// May return false if POS tagged file doesn't exist
		if(!computePageRank(object))
			return;
		
		articleProcessed++;
		
		double threshold = 0;
		ArrayList<Double> ranks = new ArrayList<Double>();
		
		for(HashSet<String> sets: sentences.keySet())
			ranks.add(sentences.get(sets));
		Collections.sort(ranks);
		if(ranks.size() > 0)
			threshold = ranks.get((int)(ranks.size()*0.75));
		
		for(HashSet<String> sets: sentences.keySet())	{
			if(sentences.get(sets) >= threshold)
				for(String str: sets)
					distinctCount.put(str,0);
		}
		
		Integer b = 0;
		for(HashSet<String> sets: sentences.keySet())	{
			for(String str: sets)	{
				if((b = distinctCount.get(str))!= null)
					distinctCount.put(str,++b);
			}
		}
		
		for(String word: distinctCount.keySet())	{
			if(distinctCount.get(word) > wordThreshold)
				distinct.add(word);
		}
		sentences.clear();
		distinctCount.clear();
			
	}
	
	public static boolean computePageRank(AbstractDB article) throws IOException {
		
		String filename = ((ArticleDetails) article).getId() + ".txt";
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		File f = new File(parentDirectoryPath +(new StringBuilder()).
				append(File.separator).append(ConfigurationConstants.FILE_DIRECTORY_PATH). 
				append(File.separator).append("posDocs").
				append(File.separator).append(filename).toString());
		System.out.println("Processing data for Unique Set: "+filename);
		
		String tempStr = "";
		countSentences = 0;
		// Prepare data structure with keywords and initial default pageRank as 1
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			while ((tempStr = br.readLine()) != null) {
				if(!sentKeywords(tempStr).isEmpty()){
					countSentences++;
					sentences.put(sentKeywords(tempStr),1.0);
				}
			}
			br.close();
		}catch(FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
		
		double weight = 0;
		
		// Update the initial PageRank on the basis on number of sentences
		for(HashSet<String> sets: sentences.keySet())	{
			weight = sentences.get(sets);
			sentences.put(sets,weight/countSentences);
		}
			
		// Compute PageRank
		iteratePageRank();

		return true;
	}

	/** Compute keywords to be considered on the basis of POS tags specified*/
	public static HashSet<String> sentKeywords(String sentence) {
		
		// Temporary arrays used for storing distinct words within a sentence
		HashSet<String> keywords = new HashSet<String>();
		
		String[] temp1 = sentence.split(" ");
		boolean stopWordFlag = false;
		
		// Extract provided POS tag words from sentence
		for (String temp : temp1) {
			String[] str = temp.split("/");
			if (str.length == 2 && pos.contains(str[1]))	{
				if(!str[0].matches(".*[0-9].*") && !str[0].matches(".*[\\W].*")){		
					stopWordFlag = false;
					// Excluding all the stop words
					for(String sw: StopWords.stopWords){
						if(str[0].compareTo(sw)==0){
							stopWordFlag = true;
							break;
						}
					}
					if(!stopWordFlag)
						keywords.add(str[0]);
				}
			}
		}
		return keywords;
	}

	/** Page Rank Algorithm Implementation */
	public static void iteratePageRank() {
		int iterations = 15;
		
		// HashMap to store the iterations intermediate results
		HashMap<HashSet<String>,Double> inter = new HashMap<HashSet<String>,Double>();
		
		// Local HashMap used for storing keywords within a sentence within a article, along with its PageRank
		HashMap<HashSet<String>,Integer> temp = new HashMap<HashSet<String>,Integer>();
		
		for(int i = 1;i <= iterations; i++)	{
			
			// Compute the L factor i.e. total occurrence for all the sentences
			for(HashSet<String> sentence: sentences.keySet())	{
				int count = 0;
				
				for(HashSet<String> others: sentences.keySet())	{
					if(!sentence.containsAll(others) && !others.containsAll(sentence)){
						// Compare elements of sentences for overlapping elements
						Set<String> intersection = new HashSet<String>(sentence);
						intersection.retainAll(others);
						count += intersection.size();
					}
				}
				temp.put(sentence,count);
			}
			
			// Compute the page ranks
			for(HashSet<String> sentence: sentences.keySet())	{
				double overlap = 0;
				
				for(HashSet<String> others: temp.keySet())	{
					if(!sentence.containsAll(others) && !others.containsAll(sentence)){
						// Compare elements of sentences for overlapping elements
						Set<String> intersection = new HashSet<String>(sentence);
						intersection.retainAll(others);
						if(temp.get(others) != 0)
							overlap += sentences.get(others)* ((double)intersection.size()/temp.get(others));
					}
						
				}
				double result = ((1 - damping)/countSentences) + damping * overlap;
				inter.put(sentence,result);
			}
			
			sentences.clear();
			// Copy the calculated page ranks to original hash map
			for(HashSet<String> sentence: inter.keySet())
				sentences.put(sentence,inter.get(sentence));

			inter.clear();
			temp.clear();
		}
	}
	
	public static void main(String args[]) throws IOException
	{
		PageRank obj = getInstance();
		obj.calculateFeatureVector(null,null,null);
	}
	
	public void print()
	{
		int i = 0;
		for(String word: distinct)	{
			if(i%10 == 0)
				System.out.println();
			System.out.print(word+" ");
			i++;
		}
		System.out.println("\nSize of keyset: "+distinct.size());
		System.out.println("Total records processed: "+articleProcessed);
	}
	
	public void writeFile(String filename)
	{
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		File uniqueFile = new File(parentDirectoryPath +(new StringBuilder()).
				append(File.separator).append(ConfigurationConstants.STATS_DIRECTORY_PATH). 
				append(File.separator).append(ConfigurationConstants.UNIQUE_FEATURE_DIRECTORY).
				append(File.separator).append(filename).toString());
		System.out.println("Processing data for Page Rank: "+filename);
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(uniqueFile));
			for(String word: distinct)	{
				bw.write(word);
				bw.write("\n");
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\nSize of keyset: "+distinct.size());
	}
}
