package analyzersWeka;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import services.RetrieveDataSrv;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;


public class GenerateWekaInputCount 
{
	/** It holds all the word, count pairs for an article at a time */
	private static HashMap<String,Integer> map = new HashMap<String, Integer>();
	
	/** It contains all the features i.e. distinct words or POS tags */
	private static LinkedList<String> featureList = new LinkedList<String>();
	
	private static void compute(String dest, String uniq, String srcDir, long rcd, String[] category)
	{
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		
		// Weka file created in wekaCount folder
		String filename = dest;
		// Distinct file from distinct folder
		String unique = uniq;
		// Folder file for feature file to be processed for all articles
		String folder = srcDir;
		// No of instances to be processed for each tag
		long records = rcd;
		
		File wekaFile = new File(parentDirectoryPath +(new StringBuilder()).
				append(File.separator).append(ConfigurationConstants.STATS_DIRECTORY_PATH). 
				append(File.separator).append(ConfigurationConstants.WEKA_COUNT_DIRECTORY).
				append(File.separator).append(filename).toString());
		System.out.println("Generating Weka File: "+wekaFile);
		
		BufferedReader br = null;
		BufferedWriter bw = null;
		
		File uniqueFile = new File(parentDirectoryPath +(new StringBuilder()).
				append(File.separator).append(ConfigurationConstants.STATS_DIRECTORY_PATH). 
				append(File.separator).append(ConfigurationConstants.UNIQUE_FEATURE_DIRECTORY).
				append(File.separator).append(unique).toString());
		
		// Create a linked list for all the distinct features
		try
		{
			bw = new BufferedWriter(new FileWriter(wekaFile));
			bw.write("@relation "+filename+"\n");
			if(uniqueFile.exists())	{
				br = new BufferedReader(new FileReader(uniqueFile));
				String line;
				while((line = br.readLine())!= null)
					featureList.add(line.split(" ")[0]);
				br.close();
			}
			else	{
				System.out.println("Distinct file provided does not exist");
				return;
			}
			for(String str: featureList)
				bw.write("@attribute \""+str+"\" numeric"+"\n");
			
			/*
			 * A - Amusing B - Interesting
			 * C - Cool    D - Obvious
			 */
			bw.write("@attribute farkTag {A,B,C,D}");
			bw.write("\n\n@data\n");
			
			String[] tags = category;
			int tagCount = tags.length;
			
			// Map to keep count of documents processed for each tag
			HashMap<String,Integer> countTag = new HashMap<String, Integer>();
			for(String s: tags)
				countTag.put(s, 0);
			
			// Retrieve all the records from the database with the required criteria
			List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
			System.out.println("Size of dataset: "+articleList.size());
			
			long articlesObserved = 0;
			long articlesProcessed = 0;
			
			StringBuilder dir = new StringBuilder(parentDirectoryPath +(new StringBuilder()).
					append(File.separator).append(ConfigurationConstants.FILE_DIRECTORY_PATH). 
					append(File.separator).append(folder).toString());
			
			for(AbstractDB article:articleList)
			{
				String id = ""+((ArticleDetails)article).getId();
				File articleFile = new File(dir+new StringBuilder().append(File.separator).append(id).append(".txt").toString());
	            
				if(articleFile.exists())	{	
                	++articlesObserved;
        			if(articlesProcessed == tagCount*records)
                    	break;
        			
                	String articleTag = ((ArticleDetails)article).getFarkTag().toLowerCase();
                	if(countTag.keySet().contains(articleTag))	{
                		if(countTag.get(articleTag) < records)
                			countTag.put(articleTag, countTag.get(articleTag) + 1);
                		else
                			continue;
        			}
                	else
                		continue;
	            	
                	++articlesProcessed;
	            	br = new BufferedReader(new FileReader(articleFile));
	            	
	            	// Read the article content in the map
	        		String line;
	        		while((line = br.readLine())!= null)
	        		{
	        			String[] words = line.split("\\s");
						StringBuilder feature = new StringBuilder();
						for(int i = 0 ; i < words.length-2;i++)
						{
							feature.append(words[i]+" ");
						}
						feature.append(words[words.length-2]);
						map.put(feature.toString(), Integer.parseInt(words[words.length-1]));
	        		}
	        		br.close();
	        		

	    			Integer value;
	    			StringBuilder result = new StringBuilder("{");
	    			int varCount = 0;
	    			for(String str: featureList)
	    			{
	    				value = map.get(str);
	    				if(value != null)
	    					result.append(varCount+" "+value).append(",");
	    				varCount++;
	    			}
	    			result.append(varCount+" ");
	    			
	    			// 4-way classification
	    			if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("amusing"))	{
	    				result.append("A");
	    			}
	    			else if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("interesting"))	{
	    				result.append("B");
	    			}
	    			else if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("cool"))	{
	    				result.append("C");
	    			}
	    			else if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("obvious"))	{
	    				result.append("D");
	    			}
	    			result.append("}");
	    			bw.write(result.toString());
	    			bw.write("\n");
	    			
	    			map.clear();
	            	} 
	            }
			bw.close();
			System.out.println("Total records considered: "+articlesObserved);
			System.out.println("Total records processed: "+articlesProcessed);
		}
		catch(IOException e)	{
			e.printStackTrace();
		}
		featureList.clear();
	}
	public static void main(String args[])
	{
		// 4-way classification
		long[] instances = {5000};
		for(long instance: instances)	{
			compute("UnigramWord"+instance+".arff","UnigramWord"+instance+"","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("UnigramWordA"+instance+".arff","UnigramWordA"+instance+"","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("UnigramWordB"+instance+".arff","UnigramWordB"+instance+"","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("UnigramWordC"+instance+".arff","UnigramWordC"+instance+"","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("UnigramPOS"+instance+".arff","UnigramPOS"+instance+"","unigramPOS",instance,"amusing cool interesting obvious".split(" "));
			compute("BigramPOS"+instance+".arff","BigramPOS"+instance+"","bigramPOS",instance,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS"+instance+".arff","TrigramPOS"+instance+"","trigramPOS",instance,"amusing cool interesting obvious".split(" "));
			compute("StopWordC"+instance+".arff","stopWords3","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("UnigramPRank0"+instance+".arff","pageRankUnique0","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("UnigramPRank1"+instance+".arff","pageRankUnique1","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("UnigramTRank"+instance+".arff","textRankUnique","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("UnigramWordDoc"+instance+".arff","DocTags"+instance+"-Top35K","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("UnigramWordCnt"+instance+".arff","CountTags"+instance+"-Top35K","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("DocCat10K"+instance+".arff","DocCat10K","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("CountCat10K"+instance+".arff","CountCat10K","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("DocCat20K"+instance+".arff","DocCat20K","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("CountCat20K"+instance+".arff","CountCat20K","unigramWord",instance,"amusing cool interesting obvious".split(" "));
		}
	}
}