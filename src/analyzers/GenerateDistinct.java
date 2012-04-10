package analyzers;

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


public class GenerateDistinct 
{
	static Object dummyObject = new Object();
	private static HashMap<String,Object> map = new HashMap<String, Object>();
	
	
	public static void compute() throws NumberFormatException, IOException, SQLException, ClassNotFoundException
	{
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		parentDirectoryPath.append(File.separator).append("files"); 
		
		// Distinct file
		String unique = "unigramWord2000";
		
		// Folder file
		String folder = "unigramWord";
		
		long records = 2000;
		
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
					map.put(line,dummyObject);
				}
				br.close();
			} 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		/*
		 * A - Amusing
		 * B - Interesting
		 * C - Cool
		 * D - Obvious
		 */
		
		
		String[] tags = {"amusing","cool","obvious","interesting"};
		
		// Retrieve all the records from the database with the required criteria
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of dataset: "+articleList.size());
		
		StringBuilder dir = new StringBuilder(parentDirectoryPath +(new StringBuilder()).append(File.separator).append(folder).toString());
		long amusingCount = 0;
		long interestCount = 0;
		long coolCount = 0;
		long obviousCount = 0;
		
		for(AbstractDB article:articleList)
		{
			try 
			{
				String id = ""+((ArticleDetails)article).getId();
				//System.out.println("ArticleId: "+id);
                File articleFile = new File(dir+new StringBuilder().append(File.separator).append(id).append(".txt").toString());
                
                if(articleFile.exists())
                {
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
    					if(map.get(feature) == null)
    						map.put(feature.toString(),dummyObject);
            		}
            		br.close();
            	} 
            }
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("Total records processed: "+(amusingCount+interestCount+coolCount+obviousCount));
		BufferedWriter bw = new BufferedWriter(new FileWriter(uniqueFile));
		Set<String> entrySet = map.keySet();
		Iterator<String> iterator = entrySet.iterator();

		try
		{
			while(iterator.hasNext())
			{
				String feature = iterator.next();
				bw.write(feature);
				bw.write("\n");
			}
			bw.flush();
			bw.close();
			
		}
		catch(Exception e)
		{
			
		}
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