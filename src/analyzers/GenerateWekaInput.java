package analyzers;

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


public class GenerateWekaInput 
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
		
		File file = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("wekaInput").append(File.separator).append(filename).toString());
		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(file));
		
		bw.write("@relation "+filename);
		bw.write("\n");
		
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
		
		for(int z=1;z <= featureList.size();z++)
		{
			bw.write("@attribute attr"+z+" numeric");
			bw.write("\n");
		}
		
		/*
		 * A - Amusing
		 * B - Interesting
		 * C - Cool
		 * D - Obvious
		 */
		bw.write("@attribute ranking {A,B,C,D}");
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
        			StringBuilder result = new StringBuilder();
        			for(String str: featureList)
        			{
        				value = map.get(str);
        				if(value == null)
        					result.append(0).append(",");
        				else
        					result.append(value).append(",");
        			}
        			
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
		
		featureList.clear();
	}
	public static void main(String args[])
	{
		try {

			// 4-way classification
			
			// UNIGRAM Words
			/*
			compute("UnigramWord100.arff","UnigramWord100","unigramWord",100,"amusing cool interesting obvious".split(" "));
			compute("UnigramWord250.arff","UnigramWord250","unigramWord",250,"amusing cool interesting obvious".split(" "));
			compute("UnigramWord500.arff","UnigramWord500","unigramWord",500,"amusing cool interesting obvious".split(" "));
			compute("UnigramWord1000.arff","UnigramWord1000","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			*/
			
			// UNIGRAM POS
			compute("UnigramPOS500.arff","UnigramPOS500","unigramPOS",500,"amusing cool interesting obvious".split(" "));
			compute("UnigramPOS2000.arff","UnigramPOS2000","unigramPOS",2000,"amusing cool interesting obvious".split(" "));
			compute("UnigramPOS5000.arff","UnigramPOS5000","unigramPOS",5000,"amusing cool interesting obvious".split(" "));
			compute("UnigramPOS10000.arff","UnigramPOS10000","unigramPOS",10000,"amusing cool interesting obvious".split(" "));
			
			// BIGRAM POS
			compute("BigramPOS500.arff","BigramPOS500","bigramPOS",500,"amusing cool interesting obvious".split(" "));
			compute("BigramPOS1000.arff","BigramPOS1000","bigramPOS",1000,"amusing cool interesting obvious".split(" "));
			compute("BigramPOS2500.arff","BigramPOS2500","bigramPOS",2500,"amusing cool interesting obvious".split(" "));
			compute("BigramPOS6000.arff","BigramPOS6000","bigramPOS",6000,"amusing cool interesting obvious".split(" "));
			
			// TRIGRAM POS
			compute("TrigramPOS100.arff","TrigramPOS100","trigramPOS",100,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS250.arff","TrigramPOS250","trigramPOS",250,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS400.arff","TrigramPOS400","trigramPOS",400,"amusing cool interesting obvious".split(" "));
			compute("TrigramPOS500.arff","TrigramPOS500","trigramPOS",500,"amusing cool interesting obvious".split(" "));
			
			// STOP Words (174)
			compute("StopWordA1000.arff","stopWords1","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			compute("StopWordA2000.arff","stopWords1","unigramWord",2000,"amusing cool interesting obvious".split(" "));
			compute("StopWordA6000.arff","stopWords1","unigramWord",6000,"amusing cool interesting obvious".split(" "));
			
			// STOP Words (543)
			compute("StopWordB1000.arff","stopWords2","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			compute("StopWordB2000.arff","stopWords2","unigramWord",2000,"amusing cool interesting obvious".split(" "));
			compute("StopWordB6000.arff","stopWords2","unigramWord",6000,"amusing cool interesting obvious".split(" "));
			
			// STOP Words (667)
			compute("StopWordC1000.arff","stopWords3","unigramWord",1000,"amusing cool interesting obvious".split(" "));
			compute("StopWordC2000.arff","stopWords3","unigramWord",2000,"amusing cool interesting obvious".split(" "));
			compute("StopWordC6000.arff","stopWords3","unigramWord",6000,"amusing cool interesting obvious".split(" "));
			
			
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