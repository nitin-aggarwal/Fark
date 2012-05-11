package analyzersWeka;

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
import java.util.TreeMap;

import services.RetrieveDataSrv;
import entities.AbstractDB;
import entities.ArticleDetails;
import generics.StopWords;


public class GenerateSeedWords 
{
	Object dummyObject = new Object(); 
	
	/**
	 * It holds all the word, count pairs for an article at a time
	 */
	private static HashMap<String,Integer> map = new HashMap<String, Integer>();
	
	/**
	 * It holds all the distinct word, count pairs from the distinct file
	 */
	private static TreeMap<String,Integer> distinct = new TreeMap<String, Integer>();
	
	// Comparator extended to get the sorted list
	private static ValueComparator bvc =  new ValueComparator(distinct);
	private static TreeMap<String,Integer> distinctSorted = new TreeMap<String, Integer>(bvc);

	/**
	 * 
	 * @param dest - refers to the name of the file to be created in the seed folder
	 * @param sourceDir - refers to the feature directory, which contain files for individual articles
	 * @param rcd - refers to number of articles to be processed for each category
	 * @param category - string array of the fark tags to be processed
	 * 
	 * @throws NumberFormatException
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void compute(String dest, String srcDir, long rcd, String[] category) throws NumberFormatException, IOException, SQLException, ClassNotFoundException
	{
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		parentDirectoryPath.append(File.separator).append("files"); 
		
		// SeedWords file in directory seed
		String filename = dest;
		
		// Folder file
		String folder = srcDir;
		long records = rcd;
		
		File file = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("seed").append(File.separator).append(filename).toString());
		BufferedReader br = null;
		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(file));
		
		String[] tags = category;
		int tagCount = tags.length;
		
		System.out.println("Processing data for "+filename);
		
		// Retrieve all the records from the database with the required criteria
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of dataset: "+articleList.size());
		
		StringBuilder dir = new StringBuilder(parentDirectoryPath +(new StringBuilder()).append(File.separator).append(folder).toString());
		long count = 0;
		
		// Map to keep count of documents processed for each tag
		HashMap<String,Integer> countTag = new HashMap<String, Integer>();
		for(String s: tags)
			countTag.put(s, 0);
		
		long totalCount = 0;
		for(AbstractDB article:articleList)
		{
			try 
			{
				String id = ""+((ArticleDetails)article).getId();
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
    					map.put(feature.toString(), Integer.parseInt(words[words.length-1]));
            		}
            		br.close();
            		

        			Integer value;
        			for(String str: map.keySet())
        			{
        				value = distinct.get(str);
        				if(value != null)
        					distinct.put(str,value+1);
        				else
        					distinct.put(str,1);
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
		distinctSorted.putAll(distinct);
		for(String str: distinctSorted.keySet())
		{
			// Excluding all the words containing special characters and numbers
			if(!str.matches(".*[0-9].*") && !str.matches(".*[\\W].*"))
			{
				boolean flag = false;
				// Excluding all the stop words
				for(String sw: StopWords.stopWords){
					if(str.compareTo(sw)==0){
						flag = true;
						break;
					}
				}
				
				// Writing to file all the seed words
				if(distinct.get(str) > 10 && !flag){
					bw.write(str+" "+distinct.get(str));
					bw.write("\n");
				}
			}
		}
		bw.flush();
		bw.close();
		
		map.clear();
		distinct.clear();
		distinctSorted.clear();
	}
	public static void main(String args[])
	{
		try {
			compute("Amusing4000","unigramWord",4000,"amusing".split(" "));
			compute("Cool4000","unigramWord",4000,"cool".split(" "));
			compute("Obvious4000","unigramWord",4000,"obvious".split(" "));
			compute("Interesting4000","unigramWord",4000,"interesting".split(" "));
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

	  TreeMap<String, Integer> base;
	  public ValueComparator(TreeMap<String, Integer> base) {
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
