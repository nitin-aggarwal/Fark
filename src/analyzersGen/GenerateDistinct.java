package analyzersGen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import services.RetrieveDataSrv;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;

/** Create distinct sets of different features for NGRAMS (both POS and Words)*/
public class GenerateDistinct 
{
	/** It holds all the distinct words for all the articles processed */
	private static HashSet<String> distinctSet = new HashSet<String>();
	
	/** Computes unique features and write to a file in files/uniqueNGramFeatures 
	 * @param unigramNumCheck - whether to remove features with numerals
	 * @param unigramSCCheck - whether to remove features with special characters
	 */
	private static void compute(String uniq, String srcDir, long rcd, String[] category, boolean unigramNumCheck, boolean unigramSCCheck)
	{
		BufferedReader br = null;
		BufferedWriter bw = null;
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		
		// Distinct file in folder stats/distinct
		String unique = uniq;
		// Folder file for feature file to be processed for all articles
		String folder = srcDir;
		// No of instances to be processed for each tag
		long records = rcd;
		
		File uniqueFile = new File(parentDirectoryPath +(new StringBuilder()).
				append(File.separator).append(ConfigurationConstants.STATS_DIRECTORY_PATH). 
				append(File.separator).append(ConfigurationConstants.UNIQUE_FEATURE_DIRECTORY).
				append(File.separator).append(unique).toString());
		System.out.println("Processing data for Unique Set: "+unique);
		
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
			bw = new BufferedWriter(new FileWriter(uniqueFile));
			
			for(AbstractDB article:articleList)	{
				String id = ""+((ArticleDetails)article).getId();
				File articleFile = new File(dir + new StringBuilder().append(File.separator).append(id).append(".txt").toString());
                
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
    					// Reduce noise by eliminating words with inappropriate length
    					if(feature.length() < 15 || feature.length() > 1)	{
    						// To exclude words with numerals and special characters
    						if(unigramNumCheck | unigramSCCheck)	{
    							if(unigramNumCheck)
    								if(feature.toString().matches(".*[0-9].*"))
    									continue;
    							if(unigramSCCheck)
    								if(feature.toString().matches(".*[\\W].*"))
    									continue;
    						}
    						distinctSet.add(feature.toString());
    					}
            		}
            		br.close();
            	} 
            }
			System.out.println("Total records considered: "+articlesObserved);
			System.out.println("Total records processed: "+articlesProcessed);
			
			// Write distinct words in a file
			for(String keyword: distinctSet)
				bw.write(keyword+"\n");
			bw.close();
		}
		catch(IOException e)	{
			e.printStackTrace();
		}
		distinctSet.clear();
	}
	
	public static void main(String args[])
	{
		// 4-way classification
		long[] instances = {5000};
		for(long instance: instances)	{
			compute("UnigramWord"+instance,"unigramWord",instance,"amusing cool interesting obvious".split(" "),false,false);
			compute("UnigramWordA"+instance,"unigramWord",instance,"amusing cool interesting obvious".split(" "),true,false);
			compute("UnigramWordB"+instance,"unigramWord",instance,"amusing cool interesting obvious".split(" "),false,true);
			compute("UnigramWordC"+instance,"unigramWord",instance,"amusing cool interesting obvious".split(" "),true,true);
			compute("UnigramPOS"+instance,"unigramPOS",instance,"amusing cool interesting obvious".split(" "),false,false);
			compute("BigramPOS"+instance,"bigramPOS",instance,"amusing cool interesting obvious".split(" "),false,false);
			compute("TrigramPOS"+instance,"trigramPOS",instance,"amusing cool interesting obvious".split(" "),false,false);
		}
	}
}