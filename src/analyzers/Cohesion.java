package analyzers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import services.RetrieveDataSrv;
import entities.AbstractDB;
import entities.ArticleDetails;
import factory.FeatureFactory;
import features.Feature;

class Cohesion {
	/**
	 * Fetch articles from database and then compute lexical cohesion
	 * over POS tags and words
	 */
	private static void cohesionArticles() {
		
		String[] tags = { "amusing", "cool", "obvious", "interesting" };
		
		// Map to keep count of documents processed for each tag
		HashMap<String,Integer> countTag = new HashMap<String, Integer>();
		for(String s: tags)
			countTag.put(s, 0);
		
		long articlesObserved = 0;
		long articlesProcessed = 0;
		int tagCount = tags.length;
		long records = 5000;

		// Fetch all the articles from the ArticleDetails table, for the above specified tags
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of article dataset: " + articleList.size());

		// Fetch all the articles for which parsing has been done
		List<Integer> articleIds = RetrieveDataSrv.retrieveAttr("CohesionDetails", "id");
		System.out.println("Size of coherent dataset: " + articleIds.size());
		
		for (AbstractDB article : articleList) {
			int articleId = ((ArticleDetails) article).getId();
			
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
			
			// If the article coherence has already been computed, we skip the computation
			if(articleIds.contains(new Integer(articleId)))
				continue;
			
			// Process the article for cohesion
			++articlesProcessed;
			calculateFeatures(article, "coherence");
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
		cohesionArticles();
	}
}