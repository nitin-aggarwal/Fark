package taggers;

import java.io.IOException;

import constants.ConfigurationConstants;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import entities.AbstractDB;
import entities.ArticleDetails;

public class POSTagger {

	MaxentTagger tagger;
	
	public POSTagger() throws IOException, ClassNotFoundException
	{
		tagger = new MaxentTagger("taggers/bidirectional-distsim-wsj-0-18.tagger");
	}
	
	/**
	 * Compute the POS tagged string for the article content
	 * @param article
	 * @return POS tagged string
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public StringBuilder tagArticles(AbstractDB article) throws IOException, ClassNotFoundException
	{
		StringBuilder strPOS = new StringBuilder();
		if(ConfigurationConstants.debugMode)
			System.out.println("POS Tagging started");
		
		String articleContent = ((ArticleDetails)article).getArticleContent();
		
		System.gc();
		if(ConfigurationConstants.debugMode)	{
			System.out.println("Available Memory: "+Runtime.getRuntime().freeMemory());
			System.out.println("Total Memory: "+Runtime.getRuntime().totalMemory());
			System.out.println("Max memory: "+Runtime.getRuntime().maxMemory());
			
			System.out.println("Sentences: "+articleContent.length());
		}
		String temp[] = articleContent.split("\\.");
		for(String sentence: temp)
			strPOS.append(tagger.tagString(sentence) +"\n");
					
		strPOS.setLength(strPOS.length()-1);
		if(ConfigurationConstants.debugMode)	{
			System.out.println("POS tagging completed");
			System.out.println("POS file writing started");
		}
		
		return strPOS;
	}
}

