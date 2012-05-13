package analyzers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import services.RetrieveDataSrv;
import entities.AbstractDB;
import entities.ArticleDetails;
import factory.FeatureFactory;
import features.Feature;

class PageRanker {

	
	/**
	 * Fetch articles from database and then compute keywords using
	 * PageRank Algorithm
	 */
	private static void pageRankArticles() {
		
		String[] tags = { "amusing" , "cool", "obvious", "interesting" };
		int tagCount = tags.length;
		long records = 5000;
		
		// Map to keep count of documents processed for each tag
		HashMap<String,Integer> countTag = new HashMap<String, Integer>();
		for(String s: tags)
			countTag.put(s, 0);
		
		long articlesObserved = 0;
		long articlesProcessed = 0;
		
		// Retrieve all the records from the database with the required criteria
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of dataset: "+articleList.size());
		
		for (AbstractDB article : articleList) {
			
			++articlesObserved;
			if(articlesProcessed == tagCount*records)
            	break;
			
        	String articleTag = ((ArticleDetails)article).getFarkTag().toLowerCase();
        	if(countTag.keySet().contains(articleTag))	{
        		if(countTag.get(articleTag) < records)
        			countTag.put(articleTag, countTag.get(articleTag) + 1);
        		else
        			continue;
			}
        	else
        		continue;
			
			calculateFeatures(article, "pageRank");
			++articlesProcessed;
		}
		System.out.println("Total records considered: "+articlesObserved);
		System.out.println("Total records processed: "+articlesProcessed);
	}
	
	private static void calculateFeatures(AbstractDB article, String featureType) {

		Feature feature;
		feature = FeatureFactory.createFeatureVector(featureType);
		try {
			feature.calculateFeatureVector(article,null,null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		pageRankArticles();
		Feature feature;
		feature = FeatureFactory.createFeatureVector("pageRank");
		feature.print();
		feature.writeFile("pageRankUnique0");
	}
}