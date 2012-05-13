package analyzersGen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import services.RetrieveDataSrv;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;

/** Create distinct sets of different features with respective document counts for NGRAMS (both POS and Words)
 * Used for computing TF-IDFs */
public class GenerateDocCount 
{
	/** It holds all the distinct words along with their counts for all the articles processed */
	private static HashMap<String,Integer> distinctMap = new HashMap<String, Integer>();
	
	/** Computes counts for unique features and write to a file in folder files/count */
	private static void compute(String dest,String uniq, String srcDir, long rcd, String[] category)
	{
		BufferedReader br = null;
		BufferedWriter bw = null;
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		
		// Distinct file with count in folder stats/count
		String unique = uniq;
		// Folder file for feature file to be processed for all articles
		String folder = srcDir;
		// No of instances to be processed for each tag
		long records = rcd;
		
		File uniqueFile = new File(parentDirectoryPath +(new StringBuilder()).
				append(File.separator).append(ConfigurationConstants.STATS_DIRECTORY_PATH). 
				append(File.separator).append(ConfigurationConstants.UNIQUE_FEATURE_DIRECTORY).
				append(File.separator).append(unique).toString());
		File uniqueCountFile = new File(parentDirectoryPath +(new StringBuilder()).
				append(File.separator).append(ConfigurationConstants.STATS_DIRECTORY_PATH). 
				append(File.separator).append(ConfigurationConstants.UNIQUE_FEATURECOUNT_DIRECTORY).
				append(File.separator).append(dest).toString());
		System.out.println("Processing data for Unique Count: "+unique);
		
		String[] tags = category;
		int tagCount = tags.length;
		
		// Map to keep count of documents processed for each tag
		HashMap<String,Integer> countTag = new HashMap<String, Integer>();
		for(String s: tags)
			countTag.put(s, 0);
		
		long articlesObserved = 0;
		long articlesProcessed = 0;
		
		// Retrieve all the records from the database with the required criteria
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of dataset: "+articleList.size());
		
		StringBuilder dir = new StringBuilder(parentDirectoryPath +(new StringBuilder()).
				append(File.separator).append(ConfigurationConstants.FILE_DIRECTORY_PATH). 
				append(File.separator).append(folder).toString());
		
		try {
			// Read in map all the distinct features
			if(uniqueFile.exists())	{
				br = new BufferedReader(new FileReader(uniqueFile));
				String line;
				while((line = br.readLine())!= null)	{
					distinctMap.put(line,0);
				}
				br.close();
			}
			else	{
				System.out.println("file does not exist");
				return;
			}
			
			// Process all articles
			for(AbstractDB article:articleList)	{
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
                	
                	// Processing an article
                	++articlesProcessed;
                	br = new BufferedReader(new FileReader(articleFile));
            		String line;
            		while((line = br.readLine())!= null)	{
            			String[] words = line.split("\\s");
            			StringBuilder feature = new StringBuilder();
    					for(int i = 0 ; i < words.length-2;i++)
    					{
    						feature.append(words[i]+" ");
    					}
    					feature.append(words[words.length-2]);
    					Integer docCount = 0;
    					if((docCount = distinctMap.get(feature.toString())) != null){
    						distinctMap.put(feature.toString(),++docCount);
    					}
            		}
            		br.close();
            	} 
            }
			System.out.println("Total records considered: "+articlesObserved);
			System.out.println("Total records processed: "+articlesProcessed);
			
			// Write to file document counts for different features
			bw = new BufferedWriter(new FileWriter(uniqueCountFile));	
			for(String keyword: distinctMap.keySet())
				bw.write(keyword +" "+distinctMap.get(keyword)+"\n");
			bw.close();
		}
		catch(IOException e)	{
			e.printStackTrace();
		}
		distinctMap.clear();
	}
	public static void main(String args[])
	{
		// 4-way classification
		/*
		 * UNIGRAM Features considered:
		 * 1. Stop Words (667 words)
		 * 2. All words (159856 words)
		 * 3. Words without numeral (130437 words)
		 * 4. Words without special characters (113609 words)
		 * 5. Words without numerals and special characters (107710 words)
		 * 6. Important words using PageRank Algorithm (8850 words)
		 * 7. Important words using TextRank Algorithm (36563 words)
		 */
		
		long[] instances = {5000};
		String tags = "amusing cool interesting obvious";
		for(long instance: instances)	{
			compute("stopWords3","stopWords3","unigramWord",instance,tags.split(" "));
			compute("UnigramWord"+instance,"UnigramWord"+instance,"unigramWord",instance,tags.split(" "));
			compute("UnigramWordA"+instance,"UnigramWordA"+instance,"unigramWord",instance,tags.split(" "));
			compute("UnigramWordB"+instance,"UnigramWordB"+instance,"unigramWord",instance,tags.split(" "));
			compute("UnigramWordC"+instance,"UnigramWordC"+instance,"unigramWord",instance,tags.split(" "));
			compute("UnigramPOS"+instance,"UnigramPOS"+instance,"unigramPOS",instance,tags.split(" "));
			compute("BigramPOS"+instance,"BigramPOS"+instance,"bigramPOS",instance,tags.split(" "));
			compute("TrigramPOS"+instance,"TrigramPOS"+instance,"trigramPOS",instance,tags.split(" "));
			compute("pageRankUnique0","pageRankUnique0","unigramWord",instance,tags.split(" "));
			compute("pageRankUnique1","pageRankUnique1","unigramWord",instance,tags.split(" "));
			compute("textRankUnique","textRankUnique","unigramWord",instance,tags.split(" "));
			compute("DocTags"+instance+"-Top35K","DocTags"+instance+"-Top35K","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("CountTags"+instance+"-Top35K","CountTags"+instance+"-Top35K","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("DocCat10K","DocCat10K","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("CountCat10K","CountCat10K","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("DocCat20K","DocCat20K","unigramWord",instance,"amusing cool interesting obvious".split(" "));
			compute("CountCat20K","CountCat20K","unigramWord",instance,"amusing cool interesting obvious".split(" "));
		}
	}
}