package taggers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import services.RetrieveDataSrv;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;
import factory.NGramFactory;
import features.ngrams.NGrams;

class Tagger
{
	private static Tagger tagger;
	private static HashMap<String,Object> uniqueFeatureMap = new HashMap<String,Object>();
	
	public static void tagArticles()
	{
		String[] tags = {"amusing","cool","obvious","interesting"};
		long count = 0;	
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of dataset: "+articleList.size());
		for(AbstractDB article:articleList)
		{
			try 
			{
				// POS Tagging computed
				System.out.println(((ArticleDetails)article).getId());
				StringBuilder strPOS = ((POSTagger)tagger).tagArticles(article);
				
				//Features - "unigramPOS","unigramWord","bigramPOS", "trigramPOS"
				calculateFeatures(strPOS,article,"unigramPOS");
				calculateFeatures(strPOS,article,"bigramPOS");
				calculateFeatures(strPOS,article,"trigramPOS");
				calculateFeatures(strPOS,article,"unigramWord");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			if((++count % 10) == 0)
				System.out.println("Articled procesed: "+count);
		}
		//uniqueFeatures(uniqueFeatureMap);
		
	}
	
	
	private static void uniqueFeatures(HashMap<String,Object> map)
	{
		BufferedWriter bw = null;
		try
		{
			File file = NGrams.getFileHandle(ConfigurationConstants.UNIQUE_FEATURE_DIRECTORY,ConfigurationConstants.NGRAM_FEATURES+".txt");
			if(file != null)
			{
				bw = new BufferedWriter(new FileWriter(file));
				Set<Entry<String,Object>> entrySet = map.entrySet();
				Iterator<Entry<String,Object>> iterator = entrySet.iterator();
				while(iterator.hasNext())
				{
					Entry<String, Object> entry = iterator.next();
					//System.out.println(entry.getKey()+" "+entry.getValue());
					bw.write((String)entry.getKey());
					bw.write("\n");
				}
				bw.flush();
				bw.close();
			}
		}	
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	
	private static void calculateFeatures(StringBuilder strPOS, AbstractDB article, String ngram) {
		
			 NGrams feature;
			 feature = NGramFactory.createFeatureVector(ngram);
			 File file = NGrams.getFileHandle(ngram,((ArticleDetails)article).getId()+".txt");
			 try
			 {
				 if(file != null)
					 feature.calculateFeatureVector(strPOS,article,file,null);//uniqueFeatureMap);
			 }
			 catch(Exception e)
			 {
				 e.printStackTrace();
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