package analyzers;

import java.io.IOException;
import java.util.List;

import services.RetrieveDataSrv;
import entities.AbstractDB;
import entities.ArticleDetails;
import factory.FeatureFactory;
import features.Feature;

class Cohesion {

	
	/**
	 * Fetch articles from database and then parse the article content using
	 * Stanford Parser and compute semantic features
	 */
	public static void cohesionArticles() {
		
		String[] tags = { "amusing", "cool", "obvious", "interesting" };
		
		// Fetch all the articles from the ArticleDetails table, for the above specified tags
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of article dataset: " + articleList.size());

		// Fetch all the articles for which parsing has been done
		List<Integer> articleIds = RetrieveDataSrv.retrieveAttr("CohesionDetails", "id");
		System.out.println("Size of coherent dataset: " + articleIds.size());
		
		for (AbstractDB article : articleList) {
			
			boolean flag = false;
			int articleId = ((ArticleDetails) article).getId();
			System.out.println("Processing article: "+articleId);

			// If the article coherence has already been computed, we skip the computation
			for(Integer parseId : articleIds)	{
				if(articleId == parseId)	{
					flag = true;
					break;
				}
			}
			if(flag)
				continue;
			calculateFeatures(article, "coherence");
		}
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