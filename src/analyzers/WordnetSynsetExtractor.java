package analyzers;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import services.RetrieveDataSrv;
import taggers.POSTagger;
import constants.ConfigurationConstants;
import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import entities.AbstractDB;
import entities.ArticleDetails;


public class WordnetSynsetExtractor {

	/**
	 * @param args
	 */
	private static HashMap<String,Object> considerableTags = new HashMap<String, Object>();
	private static HashMap<String,Object> seedWordSet = new HashMap<String,Object>(); 
	private static Object dummyObject = new Object();
	private static List<HashMap<String,ArrayList<String>>> mapList = new ArrayList<HashMap<String,ArrayList<String>>>();
	private static WordNetDatabase database = WordNetDatabase.getFileInstance();
	private static HashMap<String,ArrayList<String>> uniqueSynset= null;
	private static Set<String> synsets = new HashSet<String>(); 
	private static HashMap<String, Object> repetitiveVerbs = new HashMap<String,Object>();
	private static BufferedWriter bw = null;
	private static BufferedReader br = null;
	private static int count = 0;
	
	static
	{
		for(String tag:ConfigurationConstants.VALID_TAGS)
		{
			considerableTags.put(tag, dummyObject);
		}
		for(String verb:ConfigurationConstants.REPETITIVE_VERBS)
			repetitiveVerbs.put(verb, dummyObject);
		
		System.setProperty("wordnet.database.dir", "lib/dict");
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		String word = "amusing";
		//getPOSWordsFromArticles("amusing");
		//calculateAll("amusingSynset.txt");
		//getPOSWordsFromArticles("cool");
		//calculateAll("coolSynset.txt");
		//getPOSWordsFromArticles("interesting");
		//calculateAll("interestingSynset.txt");
		//getPOSWordsFromArticles("obvious");
		//calculateAll("obviousSynset.txt");
				
		
		
		
		// for getting keywords.
		
//		Set<String> uniqueCommonSet = readFiles("unionUniqueSynset.txt");
//		HashMap<String,String> synsetMap = makeSynsetMapFromFiles("amusingSynset.txt");
//		Set<String> synonymSet = iterateAndGetSynonyms(uniqueCommonSet,synsetMap,"amusingKeywords.txt");

		
		//for removing space seperated noun-words
//		String fileName = "coolKeywords.txt";
//		Set<String> amusingSet = readFiles(fileName);
//		iterateAndRemoveProperNouns(amusingSet,fileName);
//		
		
		
		
		Set<String> interestingSet = readFiles("interestingKeywords.txt");
		
		Set<String> obviousSet = readFiles("obviousKeywords.txt");
						
		Set<String> coolSet = readFiles("coolKeywords.txt");
		
		Set<String> amusingSet = readFiles("amusingKeywords.txt");
				
		amusingSet.addAll(coolSet);
		amusingSet.addAll(interestingSet);
		amusingSet.addAll(obviousSet);
		writeSynsets(amusingSet, "unionUniqueKeywords.txt");
		
		
		//for making unique sets
		
		//Set<String> interestingSet = readFiles("uniqueInterestingSynset.txt");
		
		//Set<String> obviousSet = readFiles("uniqueObviousSynset.txt");
				
		//Set<String> coolSet = readFiles("uniqueCoolSynset.txt");
				
		
		//Set<String> unique = new HashSet<String>();
		/*unique.addAll(amusingSet);
		System.out.println("Amusing:: "+amusingSet.size());
		System.out.println("unique:: "+unique.size());
		System.out.println("cool:: "+coolSet.size());
		unique.retainAll(coolSet);
		System.out.println(unique.size());
		*/
		
		//Set<String> amusingSet = readFiles("interestingAmusing.txt");
		//Set<String> obviousSet = readFiles("obviousSynset.txt");
		
		//Set<String> coolSet = readFiles("coolSynset.txt");
			
		
		//amusingSet.removeAll(unique);
		//amusingSet.retainAll(interestingSet);
		
		

		
		
		//amusingSet.addAll(interestingSet);
		//amusingSet.addAll(coolSet);
		//amusingSet.addAll(obviousSet);
		
		//obviousSet.removeAll(amusingSet);
		//obviousSet.removeAll(interestingSet);
		//obviousSet.removeAll(coolSet);
		//iterateAndPrint(amusingSet);
		//System.out.println(amusingSet.size());
		
		
		
		
	}	 
	
	
	
	
	
	
	
	private static void iterateAndRemoveProperNouns(Set<String> set,String fileName) {
		// TODO Auto-generated method stub
		Iterator<String> iterator = set.iterator();
		String  word = null;
		openFile("w", fileName);
		while(iterator.hasNext())
		{
			word = iterator.next();
			String[] words = word.split(" ");
			
			if(!(words.length > 1))
			{
				write(word+"\n");
			}
		}
	}







	private static Set<String> iterateAndGetSynonyms(Set<String> uniqueCommonSet,
			HashMap<String, String> synsetMap,String fileName) {
		Set<String> synonymSet = new HashSet<String>();
		Iterator<String> iterator = uniqueCommonSet.iterator();
		while(iterator.hasNext())
		{
			String definition = iterator.next();
			String word = synsetMap.get(definition);
			if(word != null)
			{
				grabSynonymsFromWordnet(word,definition,synonymSet);
			}
		}
		writeSynsets(synonymSet,fileName);
		return synonymSet;
	}







	private static void writeSynsets(Set<String> synonymSet,String fileName) {
		// TODO Auto-generated method stub
	
		openFile("w",fileName);
		
		Iterator<String> iterator = synonymSet.iterator();
		while(iterator.hasNext())
		{
			write(iterator.next()+"\n");
		}
		System.out.println(synonymSet.size());
		close("w");
		
	}







	private static void grabSynonymsFromWordnet(String word, String definition,Set<String> synonymSet) {
		// TODO Auto-generated method stub
		Synset[] synsets = database.getSynsets(word,null);
		
		for(Synset synset:synsets)
		{
			if(synset.getDefinition().equalsIgnoreCase(definition))
			{
				String[] synonyms = synset.getWordForms();
				for(String synonym:synonyms)
				{
					synonymSet.add(synonym);
				}
				break;
			}
		}
		
	}







	private static HashMap<String,String> makeSynsetMapFromFiles(String fileName) {
		// TODO Auto-generated method stub
		HashMap<String,String> synsetMap = new HashMap<String, String>(); 
		openFile("r",fileName);
		String line = null;
		try 
		{
			while((line = br.readLine()) != null)
			{
				if(line != null)
				{	
					String[] synset= line.split("::");
					for(String str:synset)
					{
						if(!str.equals("") && count > 0)
						{
						   synsetMap.put(synset[0],str);
						   break;
						}
						count++;
					}
					count = 0;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return synsetMap;
	}


	private static Set<String> readForMapFromFiles(String fileName) {
		// TODO Auto-generated method stub
		return null;
	}



	private static void iterateAndPrint(Set<String> amusingSet) {
		
		Iterator<String> iterator = amusingSet.iterator();
		openFile("w","unionUniqueSynset.txt");
		while(iterator.hasNext())
		{
			//System.out.println(iterator.next());
			write(iterator.next()+"\n");
		}
	
	}
	
	public static Set<String> getDefinition(String word)
	{
		Set<String> definitionWordSet = new HashSet<String>();
		Synset synset = getMaxTagCount(word);
		if(synset != null)	{
			String definition = synset.getDefinition();
			String[] definitionWords = definition.split(" ");
			for(String definitionWord:definitionWords)
			{
				definitionWordSet.add(definitionWord);
			}
		}
		return definitionWordSet;
	}



	private static Set<String> readFiles(String fileName) {
		// TODO Auto-generated method stub
		
		Set<String> synsets= new HashSet<String>();
		String synset = null;
		openFile("r",fileName);
		
		while((synset = read()) != null)
			synsets.add(synset);
		close("r");
		return synsets;
//		Iterator<String> iterator = synsets.iterator();
//		while(iterator.hasNext())
//		{
//			System.out.println(iterator.next());
//		}
		
	}


	private static void getPOSWordsFromArticles(String tag) {
		
		String[] tags = { tag };// "cool", "obvious", "interesting"
		
		try
		{
			//POSTagger tagger = new POSTagger();

			// Fetch all the articles form the database with required tags
			List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
			int count = 0;
			for(AbstractDB article:articleList)
			{
				uniqueSynset = new HashMap<String,ArrayList<String>>();
				int articleId = ((ArticleDetails) article).getId();
				System.out.println("*********************************************NEXT***************************************************");
				System.out.println("Tagging article: " + articleId);
				System.out.println(((ArticleDetails) article).getArticleContent());
				StringBuilder strPOS = findCorrespondingFile(""+articleId+".txt");
				if(strPOS != null)
				{
					extractAVN(strPOS);
					mapList.add(uniqueSynset);
					count++;
				}
				if(count == 5000)
					break;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception Inside Wordnet getPOSWordsFromArticles()");
			e.printStackTrace();
		}
		
		
	}



	
	private static StringBuilder findCorrespondingFile(String fileName) {
		// TODO Auto-generated method stub
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir")); 
		String str = null;
		StringBuilder strPOS  = new StringBuilder();
		BufferedReader br = null;
		parentDirectoryPath.append(File.separator).append("files").append(File.separator).
		 						append("posDocs").append(File.separator).append(fileName);
		 File f = new File(parentDirectoryPath.toString());
		try {
			if (f.exists()) {
				br = new BufferedReader(new FileReader(f));
				while ((str = br.readLine()) != null)
					strPOS.append(str);
				br.close();
			} else
				return null;
		}
		 
		 catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			 e.printStackTrace();
		}
		 catch(IOException e)
			{
				e.printStackTrace();
			}
		return strPOS;
	}



	private static void extractAVN(StringBuilder strPOS) {
		
		String stringPOS = strPOS.toString(); 
		String[] wordsWithTags = stringPOS.split(" ");
		for(String wordWithTag: wordsWithTags)
		{
			String[] word = wordWithTag.split("/");
			String wordName = word[0];
			String tag = word[1];
			if(considerableTags.get(tag) != null)
				uniqueSynset(wordName.toLowerCase(),tag);
			
		}
	}

	public static StringBuilder tagArticle(AbstractDB article,POSTagger tagger) {
		
		// POS tagged the article
		StringBuilder strPOS = null;
		try
		{
			strPOS = tagger.tagArticles(article);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return strPOS;
		// dump the tagged text in text file
		//writePOS(strPOS, article, "posDocs");
	} 




	public static void uniqueSynset(String word,String tag)
	{
		Synset[] synset = null;
		Synset[] adjectiveSynsets = null;
		if(repetitiveVerbs.get(word) == null)
		{	
			if(tag.startsWith("N"))
				synset = database.getSynsets(word,SynsetType.NOUN);
			else if(tag.startsWith("J"))
			{
				synset = database.getSynsets(word, SynsetType.ADJECTIVE);
				adjectiveSynsets = database.getSynsets(word, SynsetType.ADJECTIVE_SATELLITE);
				updateUniqueSynset(adjectiveSynsets,word);
			}
			else if(tag.startsWith("V"))
			{
				synset = database.getSynsets(word,SynsetType.VERB);
			}
			updateUniqueSynset(synset,word);
		}	
	}
	
	
	private static void updateUniqueSynset(Synset[] synsets , String word) {
		// TODO Auto-generated method stub
		
		ArrayList<String> wordsInSynset = null;
		for(Synset s: synsets)
		{
			if((wordsInSynset = uniqueSynset.get(s.getDefinition())) == null)
			{
				wordsInSynset = new ArrayList<String>();
				wordsInSynset.add(word);
				uniqueSynset.put(s.getDefinition(),wordsInSynset);
			}
			else
			{
				if(!wordsInSynset.contains(word))
					wordsInSynset.add(word);
				uniqueSynset.put(s.getDefinition(),wordsInSynset);
			}
		}
		
	}

	
	private static Synset getMaxTagCount(String word)
	{
		Synset[] synsets = database.getSynsets(word,null);
		Synset targetSynset = null;
		int max = 0;
		for(Synset synset:synsets)
		{
			//System.out.println(synset.getDefinition());
			int frequencyCount;
			try
			{
				frequencyCount = synset.getTagCount(word);
			
			//System.out.println(frequencyCount);
			if(max < frequencyCount)
			{
				targetSynset = synset;
				max = frequencyCount;
			}
			//System.out.println();
			}
			catch(Exception e)
			{
				//e.printStackTrace();
			}
		}
		if(max == 0 && synsets.length != 0)
		{
			targetSynset = synsets[0];
		}
		return targetSynset;
	}

	private static void  calculateTagCount(String word,WordNetDatabase database)
	{
		Synset[] synsets = database.getSynsets(word,null);
		Synset targetSynset = null;
		int max = 0;
		for(Synset synset:synsets)
		{
			System.out.println(synset.getDefinition());
			int frequencyCount;
			try
			{
				frequencyCount = synset.getTagCount(word);
			
			System.out.println(frequencyCount);
			if(max < frequencyCount)
			{
				targetSynset = synset;
				max = frequencyCount;
			}
			System.out.println();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		if(max == 0)
		{
			targetSynset = synsets[0];
		}
		
		String[] finalSynset = targetSynset.getWordForms();
		for(String str:finalSynset)
		{
			System.out.println(str);
			//String[] wordsInSynset = synset.getWordForms();
			if(seedWordSet.get(str) == null)
    		{
	    		seedWordSet.put(str,dummyObject);
	    		System.out.println("inserted>>>  "+str);
	    		//calculateTagCount(str,database);
	    		//System.out.println(str);
	    	//	break;
	    	}
		}
		printSeedWordSet();
		
	}
	
	
	
	private static void getCategory(String word,WordNetDatabase database)
	{
		
		Synset[] synsets = database.getSynsets(word, SynsetType.NOUN);
		for (int i = 0; i < synsets.length; i++) {
			
		    NounSynset nounSynset = (NounSynset)(synsets[i]);
		  //  NounSynset[] topics = nounSynset.getHyponyms();
		    NounSynset topic = nounSynset;
		    {
		    	int max = 0; 
		    	String temp = null;
		    	String arr[] = topic.getWordForms();
		    	for(String str : arr)
		    	{
		    		System.out.println(str);
		    		System.out.println(topic.getTagCount(str));
		    		if(max < topic.getTagCount(str))
		    		{
		    			max = topic.getTagCount(str);
		    			temp = str;
		    		}
		    	}
		    	
		    	
		    		if(temp != null)
		    		{
		    			if(seedWordSet.get(temp) == null)
		    		{
			    		seedWordSet.put(temp,dummyObject);
			    		System.out.println(temp);
			    		getCategory(temp,database);
			    		//System.out.println(str);
			    	//	break;
			    	}
		    		}	
		    		
		    	//System.exit(0);
		    }
		} 
	}

	private static void calculateSeedWordSet(String word, WordNetDatabase database) {
		// TODO Auto-generated method stub
		
		Synset[] synsets = database.getSynsets(word,null);
		for(int i = 0; i < synsets.length; i++) {
		    String[] arr = synsets[i].getWordForms();
		    for(String str : arr)
		    {
		    	if(seedWordSet.get(str) == null)
		    	{
		    		seedWordSet.put(str,dummyObject);
		    		System.out.println(str);
		    		calculateSeedWordSet(str,database);
		    	}
		    	
		
		    }
		    
	}
		
	}
	
	private static void mapsizes()
	{
		int count = 0;
		for(HashMap<String,ArrayList<String>> map : mapList)
		{
			System.out.println(map.size());
			count = count+map.size();
		}
		System.out.println("Total Count:"+count);
	}
	
	//private static void printHypernym(String word, WordNetDatabase database)
	private static void calculateAll(String fileName)
	{
		int count = 0;
		ArrayList<String> wordList = null;
		StringBuffer sameMeaningWords = new StringBuffer();
		int perDocument = 0;
		openFile("w",fileName);
		for(HashMap<String,ArrayList<String>> map : mapList)
			synsets.addAll(map.keySet());
		
		Iterator<String> iterator = synsets.iterator();
		while(iterator.hasNext())
		{
			count = 0;
			String definition = iterator.next();
			//System.out.println(definition);
			perDocument = 0;
			sameMeaningWords = new StringBuffer();
			sameMeaningWords.append(definition);
			for(HashMap<String,ArrayList<String>> map : mapList)
			{
				sameMeaningWords.append("::");
				if( (wordList = map.get(definition)) != null)
				{
					//sameMeaningWords += mapNumber+".) ";
					for(String word:wordList)
					{
						sameMeaningWords.append(word);
						break;
					}
					perDocument = wordList.size();
					//sameMeaningWords.append(perDocument);
					count+= perDocument;
				}
				//for documents which do not have this synset
//				else
//					sameMeaningWords.append("null");
			}
			sameMeaningWords.append("::").append(count).append("\n");
			//System.out.println(sameMeaningWords);
			write(sameMeaningWords.toString());
			//System.out.println();
			//System.out.println();
			//System.out.println();
		}
		System.out.println("Set size: "+synsets.size());
		//write();
		close("w");
		
	}
	
	private static void close(String mode)
	{
		try {
			if(mode.equals("w"))
				bw.close();
			else
				br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static void write(String message)
	{
		try {
			bw.write(message);
			bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static void openFile(String mode,String fileName) {
		// TODO Auto-generated method stub
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir")); 
		parentDirectoryPath.append(File.separator).append("files").append(File.separator).append("wordnetSynset").append(File.separator).append(fileName);
		 try {
			File f = new File(parentDirectoryPath.toString());
			if(mode.equals("w"))
				bw = new BufferedWriter(new FileWriter(f));
			else
				br = new BufferedReader(new FileReader(f));
		} catch (IOException e) {
			System.out.println("Exception Inside POS file writing");
			e.printStackTrace();
		} 
	}
	
	private static String read()
	{
		String[] synset = null;
		int count = 0;
		String word = null;
		try {
			String line = br.readLine();
			if(line != null)
			{	
				synset= line.split("::");
				/*for(String str:synset)
				{
					if(!str.equals("") && count > 0)
					{
						word = str;
						break;
					}
					count++;
				}
				*/
			}
			else
				return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return synset[0];
	}

	
	public static void compareMaps()
	{
		int count = 0;
		ArrayList<String> wordList = null;
		int mapNumber = 0;
		String sameMeaningWords = null;
		for(HashMap<String,ArrayList<String>> map : mapList)
			synsets.addAll(map.keySet());
		
		Iterator<String> iterator = synsets.iterator();
		while(iterator.hasNext())
		{
			count = 0;
			String definition = iterator.next();
			mapNumber = 0;
			System.out.println(definition);
			for(HashMap<String,ArrayList<String>> map : mapList)
			{
				sameMeaningWords = "";
				if( (wordList = map.get(definition)) != null)
				{
					sameMeaningWords += mapNumber+".) ";
					for(String word:wordList)
					{
						sameMeaningWords += word+",";
					}
					count++;
				}
				mapNumber++;
			}
			System.out.println(definition + "::::::::: "+ count);
			System.out.println();
			System.out.println();
			System.out.println();
		}
		System.out.println("Set size: "+synsets.size());
	}

	
	public static void printUniqueSynset()
	{
		System.out.println("********************************************");
		Set<Entry<String,ArrayList<String>>> set = uniqueSynset.entrySet();
		Iterator<Entry<String,ArrayList<String>>> iterator = set.iterator();
		while(iterator.hasNext())
		{
			
			Entry<String,ArrayList<String>> entry = iterator.next();
			ArrayList<String> listofWordsinSynset = entry.getValue();
			System.out.print(entry.getKey()+"::::: ");
			for(String word: listofWordsinSynset)
				 System.out.print(word+", ");
			
			System.out.print(" :::: "+listofWordsinSynset.size()+"\n");
			
		}
		System.out.println(seedWordSet.size());
	}
	
	public static void printSeedWordSet()
	{
		System.out.println("********************************************");
		Set<Entry<String,Object>> set = seedWordSet.entrySet();
		Iterator<Entry<String,Object>> iterator = set.iterator();
		while(iterator.hasNext())
		{
			Entry<String,Object> entry = iterator.next();
			System.out.println(entry.getKey());
		}
		System.out.println(seedWordSet.size());
	}

}
//for(String str:arr)
//{
//	Synset[] synsets2 = database.getSynsets("fly",type);
//	System.out.println("    "+str + "        "+ " tag:  "+synsets[i].getTagCount(str));
//}
//System.out.println();
//System.out.println();

