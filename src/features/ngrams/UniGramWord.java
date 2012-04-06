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

public class UniGramWord extends NGrams{
	
private static UniGramWord uniGramWord = null;
	
	private UniGramWord()
	{
		
	}
	
	public void calculateFeatureVector(StringBuilder strPOS, AbstractDB article , File file) throws IOException {
		
		// write code for calculating unigram words here..
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		Integer count;
		//String content = ((ArticleDetails)article).getArticleContent().trim();
		//String[] splitter = temp.split("[/\\s]"); 
		//Pattern p = Pattern.compile("[?.,\"()<>](\\s)*");
		String temp = strPOS.toString().trim();
		String[] spacesSplitter = temp.split("\\s");
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new FileWriter(file));
		
			for(String spaceSeperated: spacesSplitter)
			{
				String[] splitter = spaceSeperated.split("/");
				if(splitter.length == 2)
				{
					String word = splitter[0].toLowerCase();
					if((count = map.get(word)) != null) 
						map.put(word, ++count);
					else
						map.put(word, 1);
				}
			}
		/*
		for(String word:spacesSplitter)
		{
			if((count = map.get(word.toLowerCase())) != null) 
				map.put(word.toLowerCase(), ++count);
			else
				map.put(word.toLowerCase(), 1);
		}
		*/
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
		}
		catch(IOException e)
		{
			System.out.println("Exception Inside UnigramWord>>>>>>>>>>>");
			e.printStackTrace();
		}
		finally
		{
			bw.close();
		}
	}
	
	public static UniGramWord getInstance()
	{
		if(uniGramWord == null)
			uniGramWord = new UniGramWord();
		return uniGramWord;
	}


}
