package taggers;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.Locale;

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
		}
		
		// Sentences are split using BreakIterator instead on the basis of '.'
		// So each line in a file would represent one sentence
		BreakIterator boundary = BreakIterator.getSentenceInstance(Locale.US);
        boundary.setText(articleContent);
        int start = boundary.first();
	     for (int end = boundary.next();end != BreakIterator.DONE;start = end, end = boundary.next()) {
	    	 strPOS.append(tagger.tagString(articleContent.substring(start,end)) +"\n");
	     }	
					
		strPOS.setLength(strPOS.length()-1);
		if(ConfigurationConstants.debugMode)	{
			System.out.println("POS tagging completed");
		}
		
		return strPOS;
	}
}

