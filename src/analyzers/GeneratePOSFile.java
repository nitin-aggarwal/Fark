package analyzers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import services.RetrieveDataSrv;
import taggers.POSTagger;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;
import features.ngrams.NGrams;

class GeneratePOSFile {

	private static POSTagger tagger;
	
	// Contains articleId for the articles that are already tagged
	private static HashSet<Integer> posTagged = new HashSet<Integer>();

	/**
	 * Fetch articles from database and then tag the article content using
	 * Stanford POS tagger and dump in a file in posDocs folder
	 */

	public static void tagArticles() {
		long count = 0;
		String[] tags = { "amusing", "cool", "obvious", "interesting" };

		// Fetch all the articles form the database with required tags
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords(
				"ArticleDetails", tags);
		System.out.println("Size of dataset: " + articleList.size());

		for (AbstractDB article : articleList) {
			try {
				int articleId = ((ArticleDetails) article).getId();
				System.out.println("Tagging article: " + articleId);

				// If article is already tagged, skip the tagging
				if (posTagged.contains(articleId))
					continue;

				// POS tagged the article
				StringBuilder strPOS = tagger.tagArticles(article);

				// dump the tagged text in text file
				writePOS(strPOS, article, "posDocs");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			if ((++count % 10) == 0)
				System.out.println("Articled procesed: " + count);
		}
	}

	/**
	 * Write the POS tagged article content in a text file
	 * 
	 * @param strPOS
	 * @param article
	 * @param ngram
	 * @throws IOException
	 */
	private static void writePOS(StringBuilder strPOS, AbstractDB article,
			String ngram) throws IOException {

		// Create a file in a suitable directory
		File file = NGrams.getFileHandle(ngram,
				((ArticleDetails) article).getId() + ".txt");

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file));

			if (ConfigurationConstants.debugMode)
				System.out.println(strPOS);
			bw.write(strPOS.toString());
			bw.flush();
		} catch (IOException e) {
			System.out.println("Exception Inside POS file writing");
			e.printStackTrace();
		} finally {
			bw.close();
		}
	}

	/**
	 * Extract all the articles, that are already POS tagged
	 * @param featureDirectory
	 */
	private static void extractPOSFiles(String featureDirectory) {
		// TODO Auto-generated method stub
		try {
			StringBuilder parentDirectoryPath = new StringBuilder(
					System.getProperty("user.dir"));
			parentDirectoryPath.append(File.separator).append("files")
					.append(File.separator).append(featureDirectory);
			File dir = new File(parentDirectoryPath.toString());
			String[] files = dir.list();
			System.out.println("files: " + files.length);
			for (String s : files)
				posTagged.add(Integer.parseInt(s.replace(".txt", "")));
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			return;
		}

	}

	public static void main(String args[]) {

		extractPOSFiles("posDocs");
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