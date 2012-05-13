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

import analyzers.WordnetSynsetExtractor;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;
import generics.StopWords;

/**
 * Implements TextRank algorithm to identify important keywords within a a document,
 * considering closed class word as a node.
 * Identify importance of keywords, and then consider all the
 * keywords in the top 10% threshold over all the keywords
 */
public class TextRank extends Feature{

	private static TextRank textRank = null;
	
	// HashMap used for storing keywords within an article, along with its TextRank
	private static HashMap<String,Double> words = new HashMap<String,Double>();
	
	// Set used for storing all important distinct words within an article
	private static HashSet<String> distinct = new HashSet<String>();
	
	// HashMap used for storing keywords and its synset definition words set
	private static HashMap<String,HashSet<String>> wordsSet = new HashMap<String,HashSet<String>>();
		
	
	private static HashSet<String> pos = new HashSet<String>();
	private static String[] posTags = "JJ JJR JJS RB RBR RBS VB VBD VBG VBN VBP VBZ".split(" ");
	
	private static int countWords = 0;
	private static double damping = 0.85;
	
	private TextRank() {
	}

	public static TextRank getInstance() {
		if (textRank == null)
			textRank = new TextRank();
		
		for(String s: posTags)
			pos.add(s);
		return textRank;
	}

	/** Compute UNIGRAM keywords from document for article content strPOS  */
	public void calculateFeatureVector(AbstractDB object, StringBuilder strPOS, File file)
			throws IOException {
		
		// Compute the TextRank for article content over POS and words
		// May return false if POS tagged file doesn't exist
		if(!computeTextRank(object))
			return;
		
		double threshold = 0;
		ArrayList<Double> ranks = new ArrayList<Double>();
		
		for(String keyword: words.keySet())
			ranks.add(words.get(keyword));
		Collections.sort(ranks);
		if(ranks.size() > 0)
			threshold = ranks.get((int)(ranks.size()*0.95));
		
		for(String keyword: words.keySet())	{
			if(words.get(keyword) >= threshold)
				distinct.add(keyword);
		}
		
		words.clear();	
	}
	
	public static boolean computeTextRank(AbstractDB article) throws IOException {
		
		String filename = ((ArticleDetails) article).getId() + ".txt";
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		parentDirectoryPath.append(File.separator).append("files"); 
		
		File f = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("posDocs").append(File.separator).append(filename).toString());
		
		String tempStr = "";
		StringBuilder doc = new StringBuilder();
		countWords = 0;
		// Prepare data structure with keywords and initial default TextRank as 1
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			while ((tempStr = br.readLine()) != null) {
				doc.append(tempStr+" ");
			}
			br.close();
		}catch(FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
		docKeywords(doc.toString());
		double weight = 0;
		
		// Update the initial TextRank on the basis on number of words
		for(String keyword: words.keySet())	{
			weight = words.get(keyword);
			words.put(keyword,weight/countWords);
		}
			
		// Compute TextRank
		iterateTextRank();

		return true;
	}

	/** Compute keywords to be considered on the basis of POS tags specified*/
	public static void docKeywords(String text) {
		
		String[] temp1 = text.split(" ");
		boolean stopWordFlag = false;
		
		// Extract provided POS tag words from sentence
		for (String temp : temp1) {
			String[] str = temp.split("/");
			if (str.length == 2 && pos.contains(str[1]))	{
				// Exclude all words with special characters
				if(!str[0].matches(".*[0-9].*") && !str[0].matches(".*[\\W].*")
						&& !str[0].contains("_")){		
					
					stopWordFlag = false;
					// Excluding all the stop words
					for(String sw: StopWords.stopWords){
						if(str[0].compareTo(sw)==0){
							stopWordFlag = true;
							break;
						}
					}
					
					Double b = 0.0;
					if(!stopWordFlag && str[0].length() > 1){
						if((b = words.get(str[0])) != null)
							words.put(str[0],++b);
						else
							words.put(str[0],1.0);
						countWords++;
					}
				}
			}
		}
	}

	/** Text Rank Algorithm Implementation */
	public static void iterateTextRank() {
		int iterations = 10;
		
		// HashMap to store the iterations intermediate results
		HashMap<String,Double> inter = new HashMap<String,Double>();
		
		// Local HashMap used for storing keywords within a sentence within a article, along with its TextRank
		HashMap<String,Double> temp = new HashMap<String,Double>();
		
		for(String word: words.keySet())	{
			if(wordsSet.get(word) == null)	{
				HashSet<String> tempSet = (HashSet<String>) WordnetSynsetExtractor.getDefinition(word);
				wordsSet.put(word, tempSet);
			}
		}
		
		for(int i = 1;i <= iterations; i++)	{
			
			// Compute the L factor i.e. total occurrence for all the words
			for(String word: words.keySet())	{
				double count = 0;
				
				for(String other: words.keySet())	{
					if(word.compareTo(other) != 0){
						
						Set<String> wordSet = wordsSet.get(word);
						Set<String> otherSet = wordsSet.get(other);
						if(wordSet != null && otherSet != null)	{
							wordSet.retainAll(otherSet);
							// Compare elements of words for overlapping elements
							count +=  wordSet.size();
						}
					}
				}
				temp.put(word,count);
				//System.out.println("Calculated");
			}
			
			// Compute the Text ranks
			for(String word: words.keySet())	{
				double overlap = 0;
				
				for(String other: temp.keySet())	{
					if(word.compareTo(other) != 0){
						// Compare elements of words for overlapping elements
						double measure = 0.0;
						
						Set<String> wordSet = wordsSet.get(word);
						Set<String> otherSet = wordsSet.get(other);
						if(wordSet != null && otherSet != null)	{	
							wordSet.retainAll(otherSet);
							measure =  wordSet.size();
							if(temp.get(other) != 0)
								overlap += words.get(other)* (measure/temp.get(other));
						}
					}
						
				}
				double result = ((1 - damping)/countWords) + damping * overlap;
				inter.put(word,result);
			}
			
			words.clear();
			// Copy the calculated Text ranks to original hash map
			for(String word: inter.keySet())
				words.put(word,inter.get(word));

			inter.clear();
			temp.clear();
			//wordsSet.clear();
		}
	}
	
	public static void main(String args[]) throws IOException
	{
		TextRank obj = getInstance();
		obj.calculateFeatureVector(null,null,null);
		obj.print();
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
		
	}
	public void writeFile(String filename)
	{
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		File uniqueFile = new File(parentDirectoryPath +(new StringBuilder()).
				append(File.separator).append(ConfigurationConstants.STATS_DIRECTORY_PATH). 
				append(File.separator).append(ConfigurationConstants.UNIQUE_FEATURE_DIRECTORY).
				append(File.separator).append(filename).toString());
		System.out.println("Processing data for Text Rank: "+filename);
		
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(uniqueFile));
			for(String word: distinct)
				bw.write(word+"\n");
				bw.write("\n");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
