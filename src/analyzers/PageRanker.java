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
	public static void pageRankArticles() {
		
		String[] tags = { "amusing" , "cool", "obvious", "interesting" };
		
		// Map to keep count of documents processed for each tag
		HashMap<String,Integer> countTag = new HashMap<String, Integer>();
		for(String s: tags)
			countTag.put(s, 0);
		
		long totalCount = 0;
		long count = 0;
		int tagCount = tags.length;
		long records = 5000;

		// Fetch all the articles from the ArticleDetails table, for the above specified tags
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of article dataset: " + articleList.size());

		for (AbstractDB article : articleList) {
			
			int articleId = ((ArticleDetails) article).getId();
			System.out.println("Processing article: "+articleId);
			
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

			calculateFeatures(article, "pageRank");
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
		pageRankArticles();
		Feature feature;
		feature = FeatureFactory.createFeatureVector("pageRank");
		feature.print();
		feature.writeFile("pageRankUnique");
	}
}