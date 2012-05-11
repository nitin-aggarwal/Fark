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
import entities.CohesionDetails;
import entities.ParseDetails;

/**
 * Combined UNIGRAM POS, Cohesion and semantic features
 */
public class GenerateWekaCombine1 
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
		
		File file = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("wekaInputSparse").append(File.separator).append(filename).toString());
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
		
		for(String str: featureList)
		{
			bw.write("@attribute \""+str+"\" numeric");
			bw.write("\n");
		}
		// Cohesion
		bw.write("@attribute \"posADJ\" numeric\n");
		bw.write("@attribute \"posAVB\" numeric\n");
		bw.write("@attribute \"posNP\" numeric\n");
		bw.write("@attribute \"posVB\" numeric\n");
		bw.write("@attribute \"wordESW\" numeric\n");
		bw.write("@attribute \"wordISW\" numeric\n");
		// Parse
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
		
		// Fetch all the articles for which parsing has been done
		//List<AbstractDB> cohesionList = RetrieveDataSrv.retrieveDBRecords("CohesionDetails", tags);
		//System.out.println("Size of cohesion dataset: " + cohesionList.size());
		//Iterator<AbstractDB> itr = cohesionList.iterator();
		
		StringBuilder dir = new StringBuilder(parentDirectoryPath +(new StringBuilder()).append(File.separator).append(folder).toString());
		
		long count = 0;
		long totalCount = 0;
		
		// Map to keep count of documents processed for each tag
		HashMap<String,Integer> countTag = new HashMap<String, Integer>();
		for(String s: tags)
			countTag.put(s, 0);
		CohesionDetails cd = new CohesionDetails();
		ParseDetails pd = new ParseDetails();
		
		for(AbstractDB article:articleList)
		{
			try 
			{
				String id = ""+((ArticleDetails)article).getId();
				//System.out.println("ArticleId: "+id);
                File articleFile = new File(dir+new StringBuilder().append(File.separator).append(id).append(".txt").toString());
                
                cd = ((ArticleDetails)article).getCohesionDetail();
                pd = ((ArticleDetails)article).getParseDetail();
                if(cd == null || pd == null)
                	continue;
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
        			double sentences = pd.getSentences();
                	
        			StringBuilder result = new StringBuilder("{");
        			int varCount = 0;
        			for(String str: featureList)
        			{
        				value = map.get(str);
        				if(value != null)
        					result.append(varCount+" "+value).append(",");
        				varCount++;
        			}
        			result.append(varCount+" "+cd.getPosADJ()).append(",");
        			result.append(++varCount+" "+cd.getPosAVB()).append(",");
        			result.append(++varCount+" "+cd.getPosNP()).append(",");
        			result.append(++varCount+" "+cd.getPosVB()).append(",");
        			result.append(++varCount+" "+cd.getWordESW()).append(",");
        			result.append(++varCount+" "+cd.getWordISW()).append(",");
        			result.append(++varCount+" "+pd.getAdjPhrases()/sentences).append(",");
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
		
		featureList.clear();
	}
	public static void main(String args[])
	{
		try {

			// 4-way classification
			
			// UNIGRAM Words
			
			compute("Combine2.arff","UnigramPOS5000","unigramPOS",5000,"amusing cool interesting obvious".split(" "));
			
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