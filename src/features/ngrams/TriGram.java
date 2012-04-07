package features.ngrams;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import entities.AbstractDB;

public class TriGram extends NGrams {

	
private static TriGram triGram = null;
	
	private TriGram()
	{
		
	}
	
	public static TriGram getInstance()
	{
		if(triGram == null)
			triGram = new TriGram();
		return triGram;
	}

	
	@Override
	public void calculateFeatureVector(StringBuilder strPOS, AbstractDB article, File file, HashMap<String,Object> uniqueFeatureMap) throws IOException{

		HashMap<String,Integer> map = new HashMap<String,Integer>();
		StringBuilder bigramString = new StringBuilder();
		Integer count;
		String temp = strPOS.toString().trim();
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new FileWriter(file));
		
			int taggerCounter = 0;
			System.out.println(strPOS);
			String[] spacesSplitter = temp.split("\\s");
			int counter = 0;
			for(String spaceSeperated: spacesSplitter)
			{
				String[] splitter = spaceSeperated.split("/");
				if(splitter.length == 2)
				{
					if(counter != 0)
						bigramString.append(" "+splitter[1].toUpperCase());
					else
						bigramString.append(splitter[1].toUpperCase());
				}
				counter++;
			}
		
			String bigramPOSString = bigramString.toString();
			String[] posTags = bigramPOSString.split("\\s");
			int increment = 0;
			for(String posTag = posTags[increment]; increment < posTags.length - 2; increment++)
			{
				String biGramtag = posTag + " " + posTags[increment + 1] + " "+ posTags[increment+2];
				if((count = map.get(biGramtag)) != null) 
					map.put(biGramtag, ++count);
				else
					map.put(biGramtag, 1);
			}
		
			Set<Entry<String,Integer>> entrySet = map.entrySet();
			Iterator<Entry<String,Integer>> iterator = entrySet.iterator();
	
			while(iterator.hasNext())
			{
				Entry entry = iterator.next();
				System.out.println(entry.getKey()+": "+entry.getValue());
				bw.write(entry.getKey()+" "+entry.getValue());
				bw.write("\n");
			}
			bw.flush();
			updateUniqueFeatureMap(map,uniqueFeatureMap);
		}
		catch(IOException e)
		{
			System.out.println("Exception Inside Trigram>>>>>");
			e.printStackTrace();
		}
		finally
		{
			bw.close();
		}
		
	}
}


