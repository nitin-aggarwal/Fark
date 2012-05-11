package analyzers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import services.RetrieveDataSrv;
import taggers.POSTagger;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;
import factory.FeatureFactory;
import features.Feature;
import features.ngrams.NGrams;

class Tagger {
	
	/**
	 * Fetch articles from database and then 
	 * tag the article content using Stanford POS tagger and compute NGRAM features
	 */
	public static void tagArticles() {
		
		String[] tags = { "amusing", "cool", "obvious", "interesting" };
		
		// Map to keep count of documents processed for each tag
		HashMap<String,Integer> countTag = new HashMap<String, Integer>();
		for(String s: tags)
			countTag.put(s, 0);
		
		long totalCount = 0;
		long count = 0;
		int tagCount = tags.length;
		long records = 5050;

		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords(
				"ArticleDetails", tags);
		System.out.println("Size of dataset: " + articleList.size());

		for (AbstractDB article : articleList) {
			try {
				
				String filename = ((ArticleDetails) article).getId() + ".txt";
				System.out.println(filename);
				
				totalCount = 0;
				// Total count for parsed articles
	        	for(String tag: countTag.keySet())
	        		totalCount += countTag.get(tag);
	        	
	        	if(totalCount == tagCount*records)
	        		break;
	        	
	        	// count refers to articles processed (includes the one not parsed)
	        	++count;
				if(totalCount % 100 == 0)	{
					System.out.println("Processed: "+count);
					for(String str: countTag.keySet())
						System.out.println(str +" : "+countTag.get(str));
				}
				
	        	String articleTag = ((ArticleDetails)article).getFarkTag().toLowerCase();
	        	int tempCount = 0;
	        	if(countTag.keySet().contains(articleTag))
				{
	        		if(countTag.get(articleTag) < records)
	        		{
	        			tempCount = countTag.get(articleTag);
	        			countTag.remove(articleTag);
	        			countTag.put(articleTag, tempCount + 1);
	        		}
	        		else
	        			continue;
				}
	        	else
	        		continue;
				
	        	// POS tagged the article
				// Case 1 (POS tagging not done)
				//StringBuilder strPOS = tagger.tagArticles(article);
				
				// Case 2 (POS tagging done)
				StringBuilder strPOS = new StringBuilder();
				
				StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
				parentDirectoryPath.append(File.separator).append("files"); 
				
				File f = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).append("posDocs").append(File.separator).append(filename).toString());
				
				if(!f.exists())
					continue;
				
				String tempStr = "";
				BufferedReader br = new BufferedReader(new FileReader(f));
				while ((tempStr = br.readLine()) != null) {
					strPOS.append(tempStr + " ");
				}
				br.close();
				
				// Calculate NGRAM features, and dump them in text files
				calculateFeatures(strPOS, article, "unigramPOS");
				calculateFeatures(strPOS, article, "bigramPOS");
				calculateFeatures(strPOS, article, "trigramPOS");
				calculateFeatures(strPOS, article, "unigramWord");

			} catch (IOException e) {
				e.printStackTrace();
			} 
			if ((++count % 10) == 0)
				System.out.println("Articled procesed: " + count);
		}
	}

	private static void calculateFeatures(StringBuilder strPOS,
			AbstractDB article, String ngram) {

		Feature feature;
		feature = FeatureFactory.createFeatureVector(ngram);
		File file = NGrams.getFileHandle(ngram,
				((ArticleDetails) article).getId() + ".txt");
		try {
			if (file != null)
				feature.calculateFeatureVector(null,strPOS, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		if (ConfigurationConstants.TAGGING_TYPE.equals("pos")) {
			try {
				new POSTagger();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		tagArticles();
	}

}