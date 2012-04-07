package taggers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import constants.ConfigurationConstants;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import entities.AbstractDB;
import entities.ArticleDetails;

public class POSTagger extends Tagger {

	MaxentTagger tagger;
	
	public POSTagger() throws IOException, ClassNotFoundException
	{
		tagger = new MaxentTagger("taggers/bidirectional-distsim-wsj-0-18.tagger");
	}
	public StringBuilder tagArticles(AbstractDB article) throws IOException, ClassNotFoundException
	{
		StringBuilder strPOS = new StringBuilder();
		System.out.println("POS Tagging started");
		//File f = new File(ConfigurationConstants.FILE_DIRECTORY_PATH + ((ArticleDetails)article).getId()+".txt");
		String articleContent = ((ArticleDetails)article).getArticleContent();
		
		System.gc();
		System.out.println("Available Memory: "+Runtime.getRuntime().freeMemory());
		System.out.println("Total Memory: "+Runtime.getRuntime().totalMemory());
		System.out.println("Max memory: "+Runtime.getRuntime().maxMemory());
			
		System.out.println("Sentences: "+articleContent.length());
		String temp[] = articleContent.split("\\.");
		for(String sentence: temp)
			strPOS.append(tagger.tagString(sentence) +"\n");
					
		strPOS.setLength(strPOS.length()-1);
		System.out.println("POS tagging completed");
		System.out.println("POS file writing started");
		
		//BufferedWriter posWrite = new BufferedWriter(new FileWriter(f));
		//posWrite.write(strPOS.toString());
		//posWrite.close();
		return strPOS;
	}
}

