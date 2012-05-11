package analyzersWeka;

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


public class GenerateWekaInputTfidf 
{
	Object dummyObject = new Object();
	
	/**
	 * It holds all the word, count pairs for an article at a time
	 */
	private static HashMap<String,Integer> map = new HashMap<String, Integer>();
	
	/**
	 * It contains all the features i.e. distinct words or POS tags
	 */
	private static HashMap<String,Integer> featureMap = new HashMap<String,Integer>();
	
	
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
		
		File file = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("wekaInputTFIDF").append(File.separator).append(filename).toString());
		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(file));
		
		bw.write("@relation "+filename);
		bw.write("\n");
		
		File uniqueFile = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).
				append("count").append(File.separator).append(unique).toString());
		
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
					String[] words = line.split("\\s");
					StringBuilder feature = new StringBuilder();
					for(int i = 0 ; i < words.length-2;i++)
					{
						feature.append(words[i]+" ");
					}
					feature.append(words[words.length-2]);
					featureMap.put(feature.toString(), Integer.parseInt(words[words.length-1]));
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
		
		for(String str: featureMap.keySet())
		{
			bw.write("@attribute \""+str+"\" numeric");
			bw.write("\n");
		}
		
		/*
		 * A - Amusing
		 * B - Interesting
		 * C - Cool
		 * D - Obvious
		 */
		bw.write("@attribute farkTag {A,B,C,D}");
		bw.write("\n");
		bw.write("\n");
		bw.write("@data");
		bw.write("\n");
		
		String[] tags = category;
		int tagCount = tags.length;
		
		System.out.println("Processing data for generating weka file: "+filename);
		
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
        			StringBuilder result = new StringBuilder("{");
        			int varCount = 0;
        			double temp = 0.0;
        			for(String str: featureMap.keySet())
        			{
        				value = map.get(str);
        				if(value != null){
        					temp = Math.log(rcd*tagCount/(1+featureMap.get(str)));
        					result.append(varCount+" "+value*temp).append(",");
        				}
        				varCount++;
        			}
        			result.append(varCount+" ");
        			
        			// 4-way classification
        			if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("amusing"))
        			{
        				result.append("A");
        			}
        			else if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("interesting"))
        			{
        				result.append("B");
        			}
        			else if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("cool"))
        			{
        				result.append("C");
        			}
        			else if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("obvious"))
        			{
        				result.append("D");
        			}
        			result.append("}");
        			bw.write(result.toString());
        			bw.write("\n");
        			
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
		bw.flush();
		bw.close();
		
		featureMap.clear();
	}
	public static void main(String args[])
	{
		try {

			// 4-way classification
			
			// UNIGRAM Words
			
			compute("UnigramWord100.arff","UnigramWord100","unigramWord",100,"amusing cool interesting obvious".split(" "));
			compute("UnigramWord250.arff","UnigramWord250","unigramWord",250,"amusing cool interesting obvious".split(" "));
			compute("UnigramWord500.arff","UnigramWord500","unigramWord",500,"amusing cool interesting obvious".split(" "));
			compute("UnigramWord1000.arff","UnigramWord1000","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			compute("UnigramWord2000.arff","UnigramWord2000","unigramWord",2000,"amusing cool interesting obvious".split(" "));
			compute("UnigramWord5000.arff","UnigramWord5000","unigramWord",5000,"amusing cool interesting obvious".split(" "));
			
			
			// UNIGRAM POS
			compute("UnigramPOS500.arff","UnigramPOS500","unigramPOS",500,"amusing cool interesting obvious".split(" "));
			compute("UnigramPOS1000.arff","UnigramPOS1000","unigramPOS",1000,"amusing cool interesting obvious".split(" "));
			compute("UnigramPOS2500.arff","UnigramPOS2500","unigramPOS",2500,"amusing cool interesting obvious".split(" "));
			compute("UnigramPOS5000.arff","UnigramPOS5000","unigramPOS",5000,"amusing cool interesting obvious".split(" "));
			
			// BIGRAM POS
			compute("BigramPOS500.arff","BigramPOS500","bigramPOS",500,"amusing cool interesting obvious".split(" "));
			compute("BigramPOS1000.arff","BigramPOS1000","bigramPOS",1000,"amusing cool interesting obvious".split(" "));
			compute("BigramPOS2500.arff","BigramPOS2500","bigramPOS",2500,"amusing cool interesting obvious".split(" "));
			compute("BigramPOS5000.arff","BigramPOS5000","bigramPOS",5000,"amusing cool interesting obvious".split(" "));
			
			// TRIGRAM POS
			compute("TrigramPOS1000.arff","TrigramPOS1000","trigramPOS",1000,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS2500.arff","TrigramPOS2500","trigramPOS",2500,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS4000.arff","TrigramPOS4000","trigramPOS",4000,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS5000.arff","TrigramPOS5000","trigramPOS",5000,"amusing cool interesting obvious".split(" "));
			
			// STOP Words (667)
			compute("StopWordC1000.arff","StopWordC1000","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			compute("StopWordC2000.arff","StopWordC2000","unigramWord",2000,"amusing cool interesting obvious".split(" "));
			compute("StopWordC5000.arff","StopWordC5000","unigramWord",5000,"amusing cool interesting obvious".split(" "));
			
			// PageRank UNIGRAMS
			compute("UnigramPRank500.arff","UnigramPRank500","unigramWord",500,"amusing cool interesting obvious".split(" "));
			compute("UnigramPRank1000.arff","UnigramPRank1000","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			compute("UnigramPRank2500.arff","UnigramPRank2500","unigramWord",2500,"amusing cool interesting obvious".split(" "));
			compute("UnigramPRank5000.arff","UnigramPRank5000","unigramWord",5000,"amusing cool interesting obvious".split(" "));
			
			// TextRank UNIGRAMS
			compute("UnigramTRank500.arff","UnigramTRank500","unigramWord",500,"amusing cool interesting obvious".split(" "));
			compute("UnigramTRank1000.arff","UnigramTRank1000","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			compute("UnigramTRank2500.arff","UnigramTRank2500","unigramWord",2500,"amusing cool interesting obvious".split(" "));
			compute("UnigramTRank5000.arff","UnigramTRank5000","unigramWord",5000,"amusing cool interesting obvious".split(" "));
			
			
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