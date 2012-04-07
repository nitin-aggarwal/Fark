package features.ngrams;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import constants.ConfigurationConstants;

import entities.AbstractDB;

/*
 * This class is for features computation related to
 * Bi-gram POS
 */
public class BiGram extends NGrams {

	
private static BiGram biGram = null;
	
	private BiGram()
	{
		
	}
	
	public static BiGram getInstance()
	{
		if(biGram == null)
			biGram = new BiGram();
		return biGram;
	}

	@Override
	public void calculateFeatureVector(StringBuilder strPOS, AbstractDB article, File file,HashMap<String,Object> uniqueFeatureMap) throws IOException{
		// TODO Auto-generated method stub

		HashMap<String,Integer> map = new HashMap<String,Integer>();
		StringBuilder bigramString = new StringBuilder();
		Integer count;
		String temp = strPOS.toString().trim();
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new FileWriter(file));
		
			if(ConfigurationConstants.debugMode)
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
			for(int increment = 0; increment < posTags.length - 1; increment++)
			{
				String biGramtag = posTags[increment] + " " + posTags[increment + 1];
				if((count = map.get(biGramtag)) != null) 
					map.put(biGramtag, ++count);
				else
					map.put(biGramtag, 1);
			}
		
			Set<Entry<String,Integer>> entrySet = map.entrySet();
			Iterator<Entry<String,Integer>> iterator = entrySet.iterator();
	
			while(iterator.hasNext())
			{
				Entry<String, Integer> entry = iterator.next();
				if(ConfigurationConstants.debugMode)
					System.out.println(entry.getKey()+": "+entry.getValue());
				
				bw.write(entry.getKey()+" "+entry.getValue());
				bw.write("\n");
			}
			bw.flush();
			
			if(uniqueFeatureMap != null)
				updateUniqueFeatureMap(map,uniqueFeatureMap);
		}
		catch(IOException e)
		{
			System.out.println("Exception Inside Bigram Feature Computation");
			e.printStackTrace();
		}
		finally
		{
			bw.close();
		}
	}

}
