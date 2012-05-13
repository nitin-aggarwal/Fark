package analyzersGen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import services.RetrieveDataSrv;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;
import generics.StopWords;

/** Create sets of seed words features for NGRAMS (both POS and Words) on the basis of occurrences*/
public class GenerateSeedWords 
{
	/** It holds all the word, count pairs for an article at a time */
	private static HashMap<String,Integer> map = new HashMap<String, Integer>();
	
	/** It holds all the distinct word, count pairs from the distinct file */
	private static TreeMap<String,Integer> distinct = new TreeMap<String, Integer>();
	private static ValueComparator vc =  new ValueComparator(distinct);
	private static TreeMap<String,Integer> distinctSorted = new TreeMap<String, Integer>(vc);

	/**
	 * 
	 * @param dest - refers to the name of the file to be created in the seed folder
	 * @param sourceDir - refers to the feature directory, which contain files for individual articles
	 * @param rcd - refers to number of articles to be processed for each category
	 * @param category - string array of the FARK tags to be processed
	 * @param docFrequency - indicates whether count is summed over no. of document or count in documents
	 * @param top - indicates considering only top 35K words
	 * 
	 * @throws NumberFormatException
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void compute(String dest, String srcDir, long rcd, String[] category, boolean docFrequency,boolean top)
	{
		BufferedReader br = null;
		BufferedWriter bw = null;
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		
		// Distinct file with high frequency seed words in folder file/seed
		String filename = dest;
		// Folder file for feature file to be processed for all articles
		String folder = srcDir;
		// No of instances to be processed for each tag
		long records = rcd;
		
		File seedFile = new File(parentDirectoryPath +(new StringBuilder()).
				append(File.separator).append(ConfigurationConstants.STATS_DIRECTORY_PATH). 
				append(File.separator).append(ConfigurationConstants.UNIQUE_SEED_DIRECTORY).
				append(File.separator).append(filename).toString());
		System.out.println("Processing data for Seed Words: "+filename);
		
		String[] tags = category;
		int tagCount = tags.length;
		
		// Map to keep count of documents processed for each tag
		HashMap<String,Integer> countTag = new HashMap<String, Integer>();
		for(String s: tags)
			countTag.put(s, 0);
		
		long articlesObserved = 0;
		long articlesProcessed = 0;
		
		// Retrieve all the records from the database with the required criteria
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of dataset: "+articleList.size());
		
		StringBuilder dir = new StringBuilder(parentDirectoryPath +(new StringBuilder()).
				append(File.separator).append(ConfigurationConstants.FILE_DIRECTORY_PATH). 
				append(File.separator).append(folder).toString());
		
		try {
			bw = new BufferedWriter(new FileWriter(seedFile));
			for(AbstractDB article:articleList)	{
				String id = ""+((ArticleDetails)article).getId();
				File articleFile = new File(dir + new StringBuilder().append(File.separator).append(id).append(".txt").toString());
                
                if(articleFile.exists())	{	
                	++articlesObserved;
        			if(articlesProcessed == tagCount*records)
                    	break;
        			
                	String articleTag = ((ArticleDetails)article).getFarkTag().toLowerCase();
                	if(countTag.keySet().contains(articleTag))	{
                		if(countTag.get(articleTag) < records)
                			countTag.put(articleTag, countTag.get(articleTag) + 1);
                		else
                			continue;
        			}
                	else
                		continue;
                	
                	++articlesProcessed;
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
        			int countKey = 0;
        			// Update the count of distinct words in tree map
        			for(String str: map.keySet())	{
        				countKey = docFrequency?map.get(str):1;
        				if((value = distinct.get(str))!= null)
        					distinct.put(str,value+countKey);
        				else
        					distinct.put(str,countKey);
        			}
        			map.clear();
            	} 
            }
			System.out.println("Total records considered: "+articlesObserved);
			System.out.println("Total records processed: "+articlesProcessed);
			distinctSorted.putAll(distinct);
			long distinctWords = 0;
			for(String str: distinctSorted.keySet())	{
				
				// Excluding all the words containing special characters and numbers
				if(!str.matches(".*[0-9].*") && !str.matches(".*[\\W].*") && !str.contains("_"))	{
					boolean flag = false;
					// Excluding all the stop words
					for(String sw: StopWords.stopWords){
						if(str.compareTo(sw)==0){
							flag = true;
							break;
						}
					}
					
					// Writing to file all the seed words
					if(str.length() <= 15 && !flag){
						bw.write(str+" "+distinct.get(str));
						bw.write("\n");
						distinctWords++;
						if(top && distinctWords >= 35000)
							break;
					}
				}
			}
			bw.close();
			System.out.println("Distinct Words: "+distinctWords);
		}
		catch(IOException e)	{
			e.printStackTrace();
		}
		
		map.clear();
		distinct.clear();
		distinctSorted.clear();
	}
	public static void main(String args[])
	{
		// Computing over the number of documents (i.e. document count)
		compute("DocAmusing5000","unigramWord",5000,"amusing".split(" "),false, false);
		compute("DocCool5000","unigramWord",5000,"cool".split(" "),false, false);
		compute("DocObvious5000","unigramWord",5000,"obvious".split(" "),false, false);
		compute("DocInteresting5000","unigramWord",5000,"interesting".split(" "),false, false);
		compute("DocTags5000","unigramWord",5000,"amusing cool interesting obvious".split(" "),false, false);
		compute("DocTags5000-Top35K","unigramWord",5000,"amusing cool interesting obvious".split(" "),false, true);
		
		// Computing over the occurrence in the documents(i.e. total count)
		compute("CountAmusing5000","unigramWord",5000,"amusing".split(" "),true, false);
		compute("CountCool5000","unigramWord",5000,"cool".split(" "),true, false);
		compute("CountObvious5000","unigramWord",5000,"obvious".split(" "),true, false);
		compute("CountInteresting5000","unigramWord",5000,"interesting".split(" "),true, false);
		compute("CountTags5000","unigramWord",5000,"amusing cool interesting obvious".split(" "),true, false);
		compute("CountTags5000-Top35K","unigramWord",5000,"amusing cool interesting obvious".split(" "),true, true);
		
		computeSeedCategory("CountCat20K", "Count", "Amusing Cool Interesting Obvious".split(" "), 20000);
		computeSeedCategory("DocCat20K", "Doc", "Amusing Cool Interesting Obvious".split(" "), 20000);
	}
	
	private static void computeSeedCategory(String filename, String type, String[] category, long instances)	{
		BufferedReader br = null;
		HashSet<String> seed = new HashSet<String>();
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		File file = new File(parentDirectoryPath +(new StringBuilder()).
				append(File.separator).append(ConfigurationConstants.STATS_DIRECTORY_PATH). 
				append(File.separator).append(ConfigurationConstants.UNIQUE_SEED_DIRECTORY).
				append(File.separator).append(filename).toString());
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		
			for(String tag: category)	{
				File seedFile = new File(parentDirectoryPath +(new StringBuilder()).
					append(File.separator).append(ConfigurationConstants.STATS_DIRECTORY_PATH). 
					append(File.separator).append(ConfigurationConstants.UNIQUE_SEED_DIRECTORY).
					append(File.separator).append(type+tag+"5000").toString());
			
				br = new BufferedReader(new FileReader(seedFile));
				String line = null;
				long count = 0;
				while((line = br.readLine())!=null){
					seed.add(line.split(" ")[0]);
					count++;
					if(count == instances)
						break;
				}
				br.close();
			}
			for(String keyword: seed)
				bw.write(keyword+"\n");
			bw.close();
		}
		catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		} 
		catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}
}
class ValueComparator implements Comparator<String>	{

	Map<String,Integer> value;
	public ValueComparator(Map<String,Integer> arg)	{
		value = arg;
	}
	
	@Override
	// Sorting in descending order
	public int compare(String arg0, String arg1) {
		// TODO Auto-generated method stub
		if(value.get(arg0) > value.get(arg1))
			return -1;
		else if(value.get(arg0) == value.get(arg1))	{
			if(arg0.compareTo(arg1) < 0)
				return 1;
			else if(arg0.compareTo(arg1) > 0)
				return -1;
			else
				return 0;
		}
		else
			return 1;
	}
}