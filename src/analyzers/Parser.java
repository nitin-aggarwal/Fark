package analyzers;

import java.io.IOException;
import java.util.List;

import services.RetrieveDataSrv;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;
import factory.FeatureFactory;
import features.Feature;

class Parser {

	
	/**
	 * Fetch articles from database and then parse the article content using
	 * Stanford Parser and compute semantic features
	 */
	public static void parseArticles() {
		
		String[] tags = { "amusing", "cool", "obvious", "interesting" };
		
		// Fetch all the articles from the ArticleDetails table, for the above specified tags
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of article dataset: " + articleList.size());

		// Fetch all the articles for which parsing has been done
		List<Integer> articleIds = RetrieveDataSrv.retrieveAttr("ParseDetails", "id");
		System.out.println("Size of parse dataset: " + articleIds.size());
		
		for (AbstractDB article : articleList) {
			
			boolean flag = false;
			int articleId = ((ArticleDetails) article).getId();
			System.out.println("Processing article: "+articleId);

			// If the article has already been parsed, we skip the parsing
			for(Integer parseId : articleIds)	{
				if(articleId == parseId)	{
					flag = true;
					break;
				}
			}
			if(flag)
				continue;
			calculateFeatures(article, "parser");
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
		parseArticles();
	}
}