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

public class UniGram extends NGrams {

	private static UniGram uniGram = null;
	
	private UniGram()
	{
		
	}
	
	public static UniGram getInstance()
	{
		if(uniGram == null)
			uniGram = new UniGram();
		return uniGram;
	}

	public void calculateFeatureVector(StringBuilder strPOS, AbstractDB article , File file) throws IOException {
		
		// write code here for unigram calculation.
		// the following regular expression is working,,,
		// just count for 
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		Integer count;
		String temp = strPOS.toString().trim();
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new FileWriter(file));
		
			//String[] splitter = temp.split("[/\\s]"); 
			int taggerCounter = 0;
			System.out.println(strPOS);
			String[] spacesSplitter = temp.split("\\s");
		
			for(String spaceSeperated: spacesSplitter)
			{
				String[] splitter = spaceSeperated.split("/");
				if(splitter.length == 2)
				{
					String tag = splitter[1].toUpperCase();
					if((count = map.get(tag)) != null) 
						map.put(tag, ++count);
					else
						map.put(tag, 1);
				}
			}
		
			Set<Entry<String,Integer>> entrySet = map.entrySet();
			Iterator<Entry<String,Integer>> iterator = entrySet.iterator();
	
			while(iterator.hasNext())
			{
				Entry entry = iterator.next();
				System.out.println(entry.getKey()+" "+entry.getValue());
				bw.write(entry.getKey()+" "+entry.getValue());
				bw.write("\n");
			}
			bw.flush();
		}	
		catch(IOException e)
		{
			System.out.println("Exception Inside Unigram>>>>>");
			e.printStackTrace();
		}
		finally
		{
			bw.close();
		}
		
		
	}
	
	
	
}
