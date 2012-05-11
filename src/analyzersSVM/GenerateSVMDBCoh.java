package analyzersSVM;

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
import entities.CohesionDetails;


public class GenerateSVMDBCoh 
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
		
		File trainFile = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("svmInput").append(File.separator).append(filename+".dat").toString());
		File testFile = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("svmInput").append(File.separator).append(filename+"Test.dat").toString());
		BufferedWriter bw1,bw2 = null;
		bw1 = new BufferedWriter(new FileWriter(trainFile));
		bw2 = new BufferedWriter(new FileWriter(testFile));
		
		CohesionDetails cd = new CohesionDetails();
		
		/*
		 * A - Amusing
		 * B - Interesting
		 * C - Cool
		 * D - Obvious
		 */
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
		List<AbstractDB> cohesionList = RetrieveDataSrv.retrieveDBRecords("CohesionDetails", tags);
		System.out.println("Size of parse dataset: " + cohesionList.size());
		
		long count = 0;
		long totalCount = 0;
		
		// Map to keep count of documents processed for each tag
		HashMap<String,Integer> countTag = new HashMap<String, Integer>();
		for(String s: tags)
			countTag.put(s, 0);
		
		// Train or test
		String recordStatus = null;
				
		
		for(AbstractDB article:cohesionList)
		{
			try 
			{
				cd = (CohesionDetails)article;
				System.out.println("Processing Article: "+cd.getId());
               
            	totalCount = 0;
            	for(String tag: countTag.keySet())
            		totalCount += countTag.get(tag);
            	
            	if(totalCount == tagCount*records)
            		break;
            	
            	++count;
    			if(count % 1000 == 0)
    				System.out.println(count);
    			
            	String articleTag = tagMap.get(cd.getId());
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
            	
            	int varCount = 1;
            	StringBuilder result = new StringBuilder("");
            	// 4-way classification
    			if(articleTag.equalsIgnoreCase("amusing"))
    			{
    				result.append("1 ");
    			}
    			else if(articleTag.equalsIgnoreCase("interesting"))
    			{
    				result.append("2 ");
    			}
    			else if(articleTag.equalsIgnoreCase("cool"))
    			{
    				result.append("3 ");
    			}
    			else if(articleTag.equalsIgnoreCase("obvious"))
    			{
    				result.append("4 ");
    			}
    			
            	
    			result.append(varCount+":"+cd.getPosADJ()).append(" ");
    			result.append(++varCount+":"+cd.getPosAVB()).append(" ");
    			result.append(++varCount+":"+cd.getPosNP()).append(" ");
    			result.append(++varCount+":"+cd.getPosVB()).append(" ");
    			result.append(++varCount+":"+cd.getWordESW()).append(" ");
    			result.append(++varCount+":"+cd.getWordISW()).append(" ");
    			
    			if(recordStatus.compareTo("train") == 0)	{
    				bw1.write(result.toString());
    				bw1.write("\n");
    			}
    			else	{
    				bw2.write(result.toString());
    				bw2.write("\n");
    			}System.out.println("article written");
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
	}
	public static void main(String args[])
	{
		try {

			// 4-way classification
			
			// parse Features
			compute("cohesion500",500,"amusing cool interesting obvious".split(" "));
			compute("cohesion1000",1000,"amusing cool interesting obvious".split(" "));
			compute("cohesion2000",2000,"amusing cool interesting obvious".split(" "));
			compute("cohesion5000",5000,"amusing cool interesting obvious".split(" "));
			
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