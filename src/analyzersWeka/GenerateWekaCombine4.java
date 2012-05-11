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

/**
 * Combined stop words, UNIGRAM words, Syntactic and other combined syntactic features
 */
public class GenerateWekaCombine4 
{
	Object dummyObject = new Object();
	
	/**
	 * It holds all the word, count pairs for an article at a time
	 */
	private static HashMap<String,Integer> unigramMap = new HashMap<String, Integer>();
	private static HashMap<String,Integer> bigramMap = new HashMap<String, Integer>();
	private static HashMap<String,Integer> unigramWMap = new HashMap<String, Integer>();
	
	/**
	 * It contains all the features i.e. distinct words or POS tags
	 */
	private static LinkedList<String> featureList1 = new LinkedList<String>();
	private static LinkedList<String> featureList2 = new LinkedList<String>();
	
	
	public static void compute(String dest, String uniq1,String uniq2, long rcd, String[] category) throws NumberFormatException, IOException, SQLException, ClassNotFoundException
	{
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		parentDirectoryPath.append(File.separator).append("files"); 
		
		// Weka file created in wekaInput folder
		String filename = dest;
		
		// Distinct file from uniqueNGramFeatures folder
		String unique1 = uniq1;
		String unique2 = uniq2;
		
		long records = rcd;
		
		File file = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("wekaInputSparse").append(File.separator).append(filename).toString());
		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(file));
		
		bw.write("@relation "+filename);
		bw.write("\n");
		
		File uniqueFile1 = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).
				append(ConfigurationConstants.UNIQUE_FEATURE_DIRECTORY).append(File.separator).append(unique1).toString());
		File uniqueFile2 = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).
				append(ConfigurationConstants.UNIQUE_FEATURE_DIRECTORY).append(File.separator).append(unique2).toString());
		
		BufferedReader br = null;
		
		// Create a linked list for all the unique distinct features
		try	{
			if(uniqueFile1.exists())	{
				br = new BufferedReader(new FileReader(uniqueFile1));
				String line;
				while((line = br.readLine())!= null)	{
					featureList1.add(line);
				}
				br.close();
			}
			else	{
				System.out.println("Distinct file provided does not exist");
				return;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		featureList1.size();
		
		for(String str: featureList1)
		{
			bw.write("@attribute \"A"+str+"\" numeric");
			bw.write("\n");
		}
		
		// Create a linked list for all the unique features
		try	{
			if(uniqueFile2.exists())	{
				br = new BufferedReader(new FileReader(uniqueFile2));
				String line;
				while((line = br.readLine())!= null)	{
					featureList2.add(line);
				}
				br.close();
			}
			else	{
				System.out.println("Distinct file provided does not exist");
				return;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		featureList2.size();
		
		for(String str: featureList2)
		{
			bw.write("@attribute \"B"+str+"\" numeric");
			bw.write("\n");
		}
		
		// Cohesion
		bw.write("@attribute \"posADJ\" numeric\n");
		bw.write("@attribute \"posAVB\" numeric\n");
		bw.write("@attribute \"posNP\" numeric\n");
		bw.write("@attribute \"posVB\" numeric\n");
		bw.write("@attribute \"wordESW\" numeric\n");
		bw.write("@attribute \"wordISW\" numeric\n");
		
		// Combined Syntactic
		bw.write("@attribute \"coarseAdj\" numeric\n");
		bw.write("@attribute \"coarseAdverbs\" numeric\n");
		bw.write("@attribute \"coarseNouns\" numeric\n");
		bw.write("@attribute \"coarsePronouns\" numeric\n");
		bw.write("@attribute \"coarseVerb\" numeric\n");
		
		bw.write("@attribute \"properNouns\" numeric\n");
		bw.write("@attribute \"question\" numeric\n");
		bw.write("@attribute \"phraselVerbs\" numeric\n");
		
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
		
		StringBuilder dir1 = new StringBuilder(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("unigramPOS").toString());
		StringBuilder dir2 = new StringBuilder(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("bigramPOS").toString());
		
		long count = 0;
		long totalCount = 0;
		
		// Map to keep count of documents processed for each tag
		HashMap<String,Integer> countTag = new HashMap<String, Integer>();
		for(String s: tags)
			countTag.put(s, 0);
		CohesionDetails cd = new CohesionDetails();
		
		for(AbstractDB article:articleList)
		{
			try 
			{
				String id = ""+((ArticleDetails)article).getId();
				//System.out.println("ArticleId: "+id);
                File uniPOSFile = new File(dir1+new StringBuilder().append(File.separator).append(id).append(".txt").toString());
                File biPOSFile = new File(dir2+new StringBuilder().append(File.separator).append(id).append(".txt").toString());
                
                cd = ((ArticleDetails)article).getCohesionDetail();
                if(cd == null)
                	continue;
                if(uniPOSFile.exists() && biPOSFile.exists())
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
                	
                	br = new BufferedReader(new FileReader(uniPOSFile));
                	
                	// Read the article UNIGRAM POS counts in the map
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
    					unigramMap.put(feature.toString(), Integer.parseInt(words[words.length-1]));
            		}
            		br.close();
            		
            		br = new BufferedReader(new FileReader(biPOSFile));
                	
            		// Read the article BIGRAM POS counts in the map
            		while((line = br.readLine())!= null)
            		{
            			String[] words = line.split("\\s");
    					StringBuilder feature = new StringBuilder();
    					for(int i = 0 ; i < words.length-2;i++)
    					{
    						feature.append(words[i]+" ");
    					}
    					feature.append(words[words.length-2]);
    					bigramMap.put(feature.toString(), Integer.parseInt(words[words.length-1]));
            		}
            		br.close();
            			
            		br = new BufferedReader(new FileReader(biPOSFile));
                	
            		// Read the article stop words counts in the map
            		while((line = br.readLine())!= null)
            		{
            			String[] words = line.split("\\s");
    					StringBuilder feature = new StringBuilder();
    					for(int i = 0 ; i < words.length-2;i++)
    					{
    						feature.append(words[i]+" ");
    					}
    					feature.append(words[words.length-2]);
    					unigramWMap.put(feature.toString(), Integer.parseInt(words[words.length-1]));
            		}
            		br.close();
            		Integer value;
        			
        			StringBuilder result = new StringBuilder("{");
        			int varCount = 0;
        			for(String str: featureList1)
        			{
        				value = unigramWMap.get(str);
        				if(value != null)
        					result.append(varCount+" "+value).append(",");
        				varCount++;
        			}
        			for(String str: featureList2)
        			{
        				value = unigramWMap.get(str);
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
        			
        			
        			// Combined Syntactic
        			long posCount = 0 ;
        			Integer mapCount = 0;
        			//coarseAdjectives
        			for(String str: "JJ JJR JJS".split(" "))
        				posCount += ((mapCount = unigramMap.get(str))==null)?0:mapCount;
        			result.append(++varCount+" "+posCount).append(",");
        			
        			posCount = 0 ;
        			//coarseAdverbs
        			for(String str: "RB RBR RBS".split(" "))
        				posCount += ((mapCount = unigramMap.get(str))==null)?0:mapCount;
        			result.append(++varCount+" "+posCount).append(",");
        			
        			posCount = 0 ;
        			//coarseNouns
        			for(String str: "NN NNS NNP NNPS".split(" "))
        				posCount += ((mapCount = unigramMap.get(str))==null)?0:mapCount;
        			result.append(++varCount+" "+posCount).append(",");
        			posCount = 0 ;
        			
        			posCount = 0 ;
        			//coarsePronouns
        			for(String str: "PRP PRP$ WP WP$".split(" "))
        				posCount += ((mapCount = unigramMap.get(str))==null)?0:mapCount;
        			result.append(++varCount+" "+posCount).append(",");
        			posCount = 0 ;
        			
        			posCount = 0 ;
        			//coarseVerbs
        			for(String str: "VB VBD VBG VBN VBP VBZ".split(" "))
        				posCount += ((mapCount = unigramMap.get(str))==null)?0:mapCount;
        			result.append(++varCount+" "+posCount).append(",");
        			
        			posCount = 0 ;
        			//proper Nouns
        			for(String str: "NNP NNPS".split(" "))
        				posCount += ((mapCount = unigramMap.get(str))==null)?0:mapCount;
        			result.append(++varCount+" "+posCount).append(",");
        			
        			posCount = 0 ;
        			//question tags
        			for(String str: "WDT WP WP$ WRB".split(" "))
        				posCount += ((mapCount = unigramMap.get(str))==null)?0:mapCount;
        			result.append(++varCount+" "+posCount).append(",");
        			
        			posCount = 0 ;
        			//phrasal verbs
        			for(String str: "VB RP,VBD RP,VBG RP,VBN RP,VBP RP,VBZ RP".split(","))
        				posCount += ((mapCount = bigramMap.get(str))==null)?0:mapCount;
        			result.append(++varCount+" "+posCount).append(",");
        			
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
        			
        			unigramMap.clear();
        			bigramMap.clear();
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
		
		featureList1.clear();
		featureList2.clear();
	}
	public static void main(String args[])
	{
		try {

			// 4-way classification
			
			// UNIGRAM Words
			
			compute("Combine7.arff","pageRankUnique","stopWords3",5000,"amusing cool interesting obvious".split(" "));
			compute("Combine8.arff","textRankUnique","stopWords3",5000,"amusing cool interesting obvious".split(" "));
			
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