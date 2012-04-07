package taggers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import services.RetrieveDataSrv;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;
import factory.NGramFactory;
import features.ngrams.NGrams;

class Tagger
{
	private static Tagger tagger;
	
	
	public static void tagArticles()
	{
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecordsbyTag("ArticleDetails", "amusing");
		for(AbstractDB article:articleList)
		{
			try 
			{
				StringBuilder strPOS = ((POSTagger)tagger).tagArticles(article);
				calculateFeatures(strPOS,article);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private static void calculateFeatures(StringBuilder strPOS, AbstractDB article) {
		
		NGrams feature;
		for(String featureName:ConfigurationConstants.NGRAM_FEATURES)
		{
			 feature = NGramFactory.createFeatureVector(featureName);
			 File file = feature.getFileHandle(featureName,((ArticleDetails)article).getId()+".txt");
			 try
			 {
				 if(file != null)
					 feature.calculateFeatureVector(strPOS,article,file);
			 }
			 catch(Exception e)
			 {
				 e.printStackTrace();
			 }
		}
	}

	public static void main(String args[])
	{
		if(ConfigurationConstants.TAGGING_TYPE.equals("pos"))
		{
			try 
			{
				tagger = new POSTagger();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
		
		tagArticles();
	}
	
}