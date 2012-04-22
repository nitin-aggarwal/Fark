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
	private static HashMap<String,Integer> map = new HashMap<String, Integer>();
	
	
	public static void compute() throws NumberFormatException, IOException, SQLException, ClassNotFoundException
	{
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		parentDirectoryPath.append(File.separator).append("files"); 
		
		// Weka file
		String filename = "stopWordsC1000.arff";
		
		// Distinct file
		String unique = "stopWords3";
		
		// Folder file
		String folder = "unigramWord";
		long records = 1000;
		
		File file = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("wekaInput").append(File.separator).append(filename).toString());
		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(file));
		
		bw.write("@relation "+filename);
		bw.write("\n");
		
		File uniqueFile = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).
				append(ConfigurationConstants.UNIQUE_FEATURE_DIRECTORY).append(File.separator).append(unique).toString());
		
		LinkedList<String> featureList = new LinkedList<String>();
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
		
		
		String[] tags = {"amusing","cool","obvious","interesting"};
		
		// Retrieve all the records from the database with the required criteria
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of dataset: "+articleList.size());
		
		StringBuilder dir = new StringBuilder(parentDirectoryPath +(new StringBuilder()).append(File.separator).append(folder).toString());
		long count = 0;
		long amusingCount = 0;
		long interestCount = 0;
		long coolCount = 0;
		long obviousCount = 0;
		long totalCount = 0;
		for(AbstractDB article:articleList)
		{
			try 
			{
				String id = ""+((ArticleDetails)article).getId();
				//System.out.println("ArticleId: "+id);
                File articleFile = new File(dir+new StringBuilder().append(File.separator).append(id).append(".txt").toString());
                
                if(articleFile.exists())
                {
                	totalCount = amusingCount+interestCount+coolCount+obviousCount;
                	if(totalCount == 4*records)
                		break;
                	if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("amusing"))
        			{
                		if(amusingCount < records)
                			amusingCount++;
                		else
                			continue;
        			}
        			else if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("interesting"))
        			{
        				if(interestCount < records)
                			interestCount++;
                		else
                			continue;
        			}
        			else if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("cool"))
        			{
        				if(coolCount < records)
                			coolCount++;
                		else
                			continue;
        			}
        			else if(((ArticleDetails)article).getFarkTag().equalsIgnoreCase("obvious"))
        			{
        				if(obviousCount < records)
                			obviousCount++;
                		else
                			continue;
        			}
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
        			++count;
        			if(count % 1000 == 0)
        				System.out.println(count);
        			map.clear();
            	} 
            }
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("Total records processed: "+count);
		bw.flush();
		bw.close();
	}
	public static void main(String args[])
	{
		try {
			compute();
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