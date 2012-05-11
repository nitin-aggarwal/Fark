package analyzersWeka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import services.RetrieveDataSrv;
import entities.AbstractDB;
import entities.ArticleDetails;
import entities.ParseDetails;


public class GenerateWekaDBParse 
{
	Object dummyObject = new Object();
	
	/**
	 * It holds all the article's id, tag mappings
	 */
	private static HashMap<Integer,String> tagMap = new HashMap<Integer,String>();
	
	public static void compute(String dest, long rcd, String[] category) throws NumberFormatException, IOException, SQLException, ClassNotFoundException
	{
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		parentDirectoryPath.append(File.separator).append("files"); 
		
		// Weka file created in wekaInput folder
		String filename = dest;
		
		long records = rcd;
		
		File file = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("wekaInputSparse").append(File.separator).append(filename).toString());
		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(file));
		
		bw.write("@relation "+filename);
		bw.write("\n");
		
		ParseDetails pd = new ParseDetails();
		
		bw.write("@attribute \"adjPhrases\" numeric\n");
		bw.write("@attribute \"conjPhrases\" numeric\n");
		bw.write("@attribute \"depth\" numeric\n");
		bw.write("@attribute \"nounPhrases\" numeric\n");
		bw.write("@attribute \"score\" numeric\n");
		bw.write("@attribute \"sentences\" numeric\n");
		bw.write("@attribute \"sentPhrases\" numeric\n");
		bw.write("@attribute \"size\" numeric\n");
		bw.write("@attribute \"verbPhrases\" numeric\n");
	
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
		
		ArticleDetails articleObj = null;
		for(AbstractDB article:articleList)	{
			articleObj = (ArticleDetails)article;
			tagMap.put(articleObj.getId(), articleObj.getFarkTag());
		}
		
		// Fetch all the articles for which parsing has been done
		List<AbstractDB> parseList = RetrieveDataSrv.retrieveDBRecords("ParseDetails", tags);
		System.out.println("Size of parse dataset: " + parseList.size());
		
		long count = 0;
		long totalCount = 0;
		
		// Map to keep count of documents processed for each tag
		HashMap<String,Integer> countTag = new HashMap<String, Integer>();
		for(String s: tags)
			countTag.put(s, 0);
		
		for(AbstractDB article:parseList)
		{
			try 
			{
				pd = (ParseDetails)article;
				System.out.println("Processing Article: "+pd.getId());
               
            	totalCount = 0;
            	for(String tag: countTag.keySet())
            		totalCount += countTag.get(tag);
            	
            	if(totalCount == tagCount*records)
            		break;
            	
            	++count;
    			if(count % 1000 == 0)
    				System.out.println(count);
    			
            	String articleTag = tagMap.get(pd.getId());
            	if(articleTag != null)
            		articleTag = articleTag.toLowerCase();
            	else 
            		continue;
            	
            	int tempCount = 0;
            	System.out.println("Tag: "+articleTag);
            	if(countTag.keySet().contains(articleTag))
    			{
            		System.out.println("Inside");
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
            	
            	int varCount = 0;
            	double sentences = pd.getSentences();
            	StringBuilder result = new StringBuilder("{");
    			result.append(varCount+" "+pd.getAdjPhrases()/sentences).append(",");
    			result.append(++varCount+" "+pd.getConjPhrases()/sentences).append(",");
    			result.append(++varCount+" "+pd.getDepth()/sentences).append(",");
    			result.append(++varCount+" "+pd.getNounPhrases()/sentences).append(",");
    			result.append(++varCount+" "+Math.abs(pd.getScore()/sentences)).append(",");
    			result.append(++varCount+" "+pd.getSentences()).append(",");
    			result.append(++varCount+" "+pd.getSentPhrases()/sentences).append(",");
    			result.append(++varCount+" "+pd.getSize()/sentences).append(",");
    			result.append(++varCount+" "+pd.getVerbPhrases()/sentences).append(",");
    			result.append(++varCount+" ");
    			
    			// 4-way classification
    			if(articleTag.equalsIgnoreCase("amusing"))
    			{
    				result.append("A");
    			}
    			else if(articleTag.equalsIgnoreCase("interesting"))
    			{
    				result.append("B");
    			}
    			else if(articleTag.equalsIgnoreCase("cool"))
    			{
    				result.append("C");
    			}
    			else if(articleTag.equalsIgnoreCase("obvious"))
    			{
    				result.append("D");
    			}
    			result.append("}");
    			bw.write(result.toString());
    			bw.write("\n");
    			System.out.println("article written");
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
	}
	public static void main(String args[])
	{
		try {

			// 4-way classification
			
			// parse Features
			compute("parse500.arff",500,"amusing cool interesting obvious".split(" "));
			compute("parse1000.arff",1000,"amusing cool interesting obvious".split(" "));
			compute("parse2000.arff",2000,"amusing cool interesting obvious".split(" "));
			compute("parse5000.arff",5000,"amusing cool interesting obvious".split(" "));
			
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