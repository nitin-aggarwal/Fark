package analyzersWeka;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import services.RetrieveDataSrv;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;


public class GenerateDistinctCount 
{
	static Object dummyObject = new Object();
	
	/**
	 * It holds all the distinct words for all the articles processed
	 */
	private static HashMap<String,Integer> distinctMap = new HashMap<String, Integer>();
	
	
	public static void compute(String dest,String uniq, String srcDir, long rcd, String[] category) throws NumberFormatException, IOException, SQLException, ClassNotFoundException
	{
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		parentDirectoryPath.append(File.separator).append("files"); 
		
		// Distinct file in folder uniqueNGramFeatures
		String unique = uniq;
				
		// Folder file
		String folder = srcDir;
		long records = rcd;
		
		File uniqueFile = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).
				append(ConfigurationConstants.UNIQUE_FEATURE_DIRECTORY).append(File.separator).append(unique).toString());
		File uniqueCountFile = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).
				append("count").append(File.separator).append(dest).toString());
		
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
					//System.out.println("put: "+line+"XXX");
					distinctMap.put(line,0);
					//System.out.println("get: "+distinctMap.get(line));
				}
				br.close();
			}
			else	{
				System.out.println("file does not exist");
				return;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		String[] tags = category;
		int tagCount = tags.length;
		
		// Map to keep count of documents processed for each tag
		HashMap<String,Integer> countTag = new HashMap<String, Integer>();
		for(String s: tags)
			countTag.put(s, 0);
		
		long count = 0;
		long totalCount = 0;
		
		System.out.println("Processing data for "+unique);
		
		// Retrieve all the records from the database with the required criteria
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of dataset: "+articleList.size());
		
		StringBuilder dir = new StringBuilder(parentDirectoryPath +(new StringBuilder()).append(File.separator).append(folder).toString());
		
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
    					Integer docCount = 0;
    					if((docCount = distinctMap.get(feature.toString())) != null){
    						distinctMap.put(feature.toString(),++docCount);
    					}
            		}
            		br.close();
            	} 
            }
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("Total records processed: "+count);
		System.out.println("Total records considered: "+totalCount);
		BufferedWriter bw = new BufferedWriter(new FileWriter(uniqueCountFile));
		Set<String> entrySet = distinctMap.keySet();
		Iterator<String> iterator = entrySet.iterator();

		try	{
			while(iterator.hasNext())
			{
				String feature = iterator.next();
				bw.write(feature +" "+distinctMap.get(feature));
				bw.write("\n");
			}
			bw.flush();
			bw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		distinctMap.clear();
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