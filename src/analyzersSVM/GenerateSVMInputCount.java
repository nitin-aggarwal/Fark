package analyzersSVM;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import services.RetrieveDataSrv;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;


public class GenerateSVMInputCount 
{
	Object dummyObject = new Object();
	
	/**
	 * It holds all the word, count pairs for an article at a time
	 */
	private static HashMap<String,Integer> map = new HashMap<String, Integer>();
	
	/**
	 * It contains all the features i.e. distinct words or POS tags
	 */
	private static LinkedList<String> featureList = new LinkedList<String>();
	
	
	public static void compute(String dest, String uniq, String srcDir, long rcd, String[] category) throws NumberFormatException, IOException, SQLException, ClassNotFoundException
	{
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		parentDirectoryPath.append(File.separator).append("files"); 
		
		// Weka file created in wekaInput folder
		String filename = dest;
		
		// Distinct file from uniqueNGramFeatures folder
		String unique = uniq;
		
		String folder = srcDir;
		long records = rcd;
		
		File trainFile = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("svmInput").append(File.separator).append(filename+".dat").toString());
		File testFile = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("svmInput").append(File.separator).append(filename+"Test.dat").toString());
		BufferedWriter bw1,bw2 = null;
		bw1 = new BufferedWriter(new FileWriter(trainFile));
		bw2 = new BufferedWriter(new FileWriter(testFile));
		
		File uniqueFile = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).
				append(ConfigurationConstants.UNIQUE_FEATURE_DIRECTORY).append(File.separator).append(unique).toString());
		
		BufferedReader br = null;
		
		// Create a linked list for all the distinct features
		try
		{
			if(uniqueFile.exists())
			{
				br = new BufferedReader(new FileReader(uniqueFile));
				String line;
				while((line = br.readLine())!= null)
				{
					featureList.add(line);
				}
				br.close();
			}
			else
			{
				System.out.println("Distinct file provided does not exist");
				return;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		featureList.size();
		
		/*
		 * A - Amusing
		 * B - Interesting
		 * C - Cool
		 * D - Obvious
		 */
		String[] tags = category;
		int tagCount = tags.length;
		
		System.out.println("Processing data for generating SVM file: "+filename);
		
		// Retrieve all the records from the database with the required criteria
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of dataset: "+articleList.size());
		
		StringBuilder dir = new StringBuilder(parentDirectoryPath +(new StringBuilder()).append(File.separator).append(folder).toString());
		
		long count = 0;
		long totalCount = 0;
		
		// Map to keep count of documents processed for each tag
		HashMap<String,Integer> countTag = new HashMap<String, Integer>();
		for(String s: tags)
			countTag.put(s, 0);
		
		// Train or test
		String recordStatus = null;
		
		for(AbstractDB article:articleList)
		{
			try 
			{
				String id = ""+((ArticleDetails)article).getId();
				//System.out.println("ArticleId: "+id);
                File articleFile = new File(dir+new StringBuilder().append(File.separator).append(id).append(".txt").toString());
                
                if(articleFile.exists())
                {
                	totalCount = 0;
                	for(String tag: countTag.keySet())
                		totalCount += countTag.get(tag);
                	
                	if(totalCount == tagCount*records)
                		break;
                	
                	++count;
        			if(count % 1000 == 0)
        				System.out.println(count);
        			
                	String articleTag = ((ArticleDetails)article).getFarkTag().toLowerCase();
                	int tempCount = 0;
                	if(countTag.keySet().contains(articleTag))
        			{
                		if(countTag.get(articleTag) < records)
                		{
                			tempCount = countTag.get(articleTag);
                			countTag.remove(articleTag);
                			countTag.put(articleTag, tempCount + 1);
                			if(tempCount > rcd*0.8)
                				recordStatus = "test";
                			else
                				recordStatus = "train";
                		}
                		else
                			continue;
        			}
                	else
                		continue;
                	
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
        			StringBuilder result = new StringBuilder();
        			
        			// 4-way classification
        			if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("amusing"))
        			{
        				result.append("1 ");
        			}
        			else if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("interesting"))
        			{
        				result.append("2 ");
        			}
        			else if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("cool"))
        			{
        				result.append("3 ");
        			}
        			else if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("obvious"))
        			{
        				result.append("4 ");
        			}
        			
        			int varCount = 1;
        			for(String str: featureList)
        			{
        				value = map.get(str);
        				if(value != null)
        					result.append(varCount+":"+value+" ");
        				varCount++;
        			}
        			if(recordStatus.compareTo("train") == 0)	{
        				bw1.write(result.toString());
        				bw1.write("\n");
        			}
        			else	{
        				bw2.write(result.toString());
        				bw2.write("\n");
        			}
        				
        			
        			map.clear();
            	} 
            }
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("Total records processed: "+count);
		System.out.println("Total records considered: "+totalCount);
		bw1.flush();
		bw1.close();
		bw2.flush();
		bw2.close();
		
		featureList.clear();
	}
	public static void main(String args[])
	{
		try {

			// 4-way classification
			
			// UNIGRAM Words
			compute("UnigramWord100","UnigramWord100","unigramWord",100,"amusing cool interesting obvious".split(" "));
			compute("UnigramWord250","UnigramWord250","unigramWord",250,"amusing cool interesting obvious".split(" "));
			compute("UnigramWord500","UnigramWord500","unigramWord",500,"amusing cool interesting obvious".split(" "));
			compute("UnigramWord1000","UnigramWord1000","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			compute("UnigramWord2000","UnigramWord2000","unigramWord",2000,"amusing cool interesting obvious".split(" "));
			compute("UnigramWord5000","UnigramWord5000","unigramWord",5000,"amusing cool interesting obvious".split(" "));
			
			
			// UNIGRAM POS
			compute("UnigramPOS500","UnigramPOS500","unigramPOS",500,"amusing cool interesting obvious".split(" "));
			compute("UnigramPOS1000","UnigramPOS1000","unigramPOS",1000,"amusing cool interesting obvious".split(" "));
			compute("UnigramPOS2500","UnigramPOS2500","unigramPOS",2500,"amusing cool interesting obvious".split(" "));
			compute("UnigramPOS5000","UnigramPOS5000","unigramPOS",5000,"amusing cool interesting obvious".split(" "));
			
			// BIGRAM POS
			compute("BigramPOS500","BigramPOS500","bigramPOS",500,"amusing cool interesting obvious".split(" "));
			compute("BigramPOS1000","BigramPOS1000","bigramPOS",1000,"amusing cool interesting obvious".split(" "));
			compute("BigramPOS2500","BigramPOS2500","bigramPOS",2500,"amusing cool interesting obvious".split(" "));
			compute("BigramPOS5000","BigramPOS5000","bigramPOS",5000,"amusing cool interesting obvious".split(" "));
			
			// TRIGRAM POS
			compute("TrigramPOS100","TrigramPOS100","trigramPOS",100,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS250","TrigramPOS250","trigramPOS",250,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS400","TrigramPOS400","trigramPOS",400,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS500","TrigramPOS500","trigramPOS",500,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS1000","TrigramPOS1000","trigramPOS",1000,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS2500","TrigramPOS2500","trigramPOS",2500,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS4000","TrigramPOS4000","trigramPOS",4000,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS5000","TrigramPOS5000","trigramPOS",5000,"amusing cool interesting obvious".split(" "));
			
			// STOP Words (174)
			compute("StopWordA1000","stopWords1","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			compute("StopWordA2000","stopWords1","unigramWord",2000,"amusing cool interesting obvious".split(" "));
			compute("StopWordA5000","stopWords1","unigramWord",5000,"amusing cool interesting obvious".split(" "));
			
			// STOP Words (543)
			compute("StopWordB1000","stopWords2","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			compute("StopWordB2000","stopWords2","unigramWord",2000,"amusing cool interesting obvious".split(" "));
			compute("StopWordB5000","stopWords2","unigramWord",5000,"amusing cool interesting obvious".split(" "));
			
			// STOP Words (667)
			compute("StopWordC1000","stopWords3","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			compute("StopWordC2000","stopWords3","unigramWord",2000,"amusing cool interesting obvious".split(" "));
			compute("StopWordC5000","stopWords3","unigramWord",5000,"amusing cool interesting obvious".split(" "));
			
			// PageRank UNIGRAMS
			compute("UnigramPRank500","pageRankUnique","unigramWord",500,"amusing cool interesting obvious".split(" "));
			compute("UnigramPRank1000","pageRankUnique","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			compute("UnigramPRank2500","pageRankUnique","unigramWord",2500,"amusing cool interesting obvious".split(" "));
			compute("UnigramPRank5000","pageRankUnique","unigramWord",5000,"amusing cool interesting obvious".split(" "));
			
			// TextRank UNIGRAMS
			compute("UnigramTRank500","textRankUnique","unigramWord",500,"amusing cool interesting obvious".split(" "));
			compute("UnigramTRank1000","textRankUnique","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			compute("UnigramTRank2500","textRankUnique","unigramWord",2500,"amusing cool interesting obvious".split(" "));
			compute("UnigramTRank5000","textRankUnique","unigramWord",5000,"amusing cool interesting obvious".split(" "));
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}