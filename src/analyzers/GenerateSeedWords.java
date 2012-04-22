package analyzers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import services.RetrieveDataSrv;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;


public class GenerateSeedWords 
{
	Object dummyObject = new Object();
	private static HashMap<String,Integer> map = new HashMap<String, Integer>();
	private static TreeMap<String,Integer> distinct = new TreeMap<String, Integer>();
	
	private static ValueComparator bvc =  new ValueComparator(distinct);
	private static TreeMap<String,Integer> distinctSorted = new TreeMap<String, Integer>(bvc);

	
	public static void compute() throws NumberFormatException, IOException, SQLException, ClassNotFoundException
	{
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		parentDirectoryPath.append(File.separator).append("files"); 
		
		// SeedWords file in directory seed
		String filename = "Amusing1000";
		
		// Distinct file in directory uniqueNGramFeatures
		String unique = "unigramWordA1000";
		
		// Folder file
		String folder = "unigramWord";
		long records = 1000;
		
		File file = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("seed").append(File.separator).append(filename).toString());
		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(file));
		
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
					distinct.put(line,0);
				}
				br.close();
			} 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		//String[] tags = {"amusing","cool","obvious","interesting"};
		String[] tags = {"amusing"};
		
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
        			for(String str: distinct.keySet())
        			{
        				value = map.get(str);
        				if(value != null)
        					distinct.put(str,distinct.get(str)+1);
        			}
        			
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
		distinctSorted.putAll(distinct);
		for(String str: distinctSorted.keySet())
		{
			bw.write(str+" "+distinctSorted.get(str));
			bw.write("\n");
		}
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
class ValueComparator implements Comparator<Object> {

	  Map<String, Integer> base;
	  public ValueComparator(Map<String, Integer> base) {
	      this.base = base;
	  }

	  public int compare(Object a, Object b) {

	    if(base.get(a) <= base.get(b)) {
	      return 1;
	    } else if(base.get(a) == base.get(b)) {
	      return 0;
	    } else {
	      return -1;
	    }
	  }
	}
