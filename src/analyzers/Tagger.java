package analyzers;

import java.io.File;
import java.io.IOException;
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
	
	private static POSTagger tagger;

	/**
	 * Fetch articles from database and then 
	 * tag the article content using Stanford POS tagger and compute NGRAM features
	 */
	public static void tagArticles() {
		long count = 0;
		String[] tags = { "amusing", "cool", "obvious", "interesting" };

		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords(
				"ArticleDetails", tags);
		System.out.println("Size of dataset: " + articleList.size());

		for (AbstractDB article : articleList) {
			try {
				System.out.println(((ArticleDetails) article).getId());

				// POS tagged the article
				StringBuilder strPOS = tagger.tagArticles(article);

				// Calculate NGRAM features, and dump them in text files
				calculateFeatures(strPOS, article, "unigramPOS");
				calculateFeatures(strPOS, article, "bigramPOS");
				calculateFeatures(strPOS, article, "trigramPOS");
				calculateFeatures(strPOS, article, "unigramWord");

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
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
				tagger = new POSTagger();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		tagArticles();
	}

}