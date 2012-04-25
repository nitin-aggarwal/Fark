package analyzers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import services.InsertDataSrv;
import services.RetrieveDataSrv;
import beans.ArticleInfo;
import constants.ConfigurationConstants;
import entities.AbstractDB;
import entities.ArticleDetails;
import extractors.Extractor;
import factory.ExtractorFactory;

public class Crawler {

	/**
	 * @param args
	 */

	private static Crawler crawler;
	private static Calendar calendar;
	private static final String PREFIX_URL = "http://www.fark.com/"
			+ ConfigurationConstants.FARK_CATEGORY + "/archives/";
	private static StringBuilder URL;
	private static ArticleDetails ad;
	private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	private Crawler() {

	}

	static {
		crawler = getInstance();
		if (ConfigurationConstants.INSERTION_FLAG) {
			Date startingDate = null;

			try {
				startingDate = formatter
						.parse(ConfigurationConstants.STARTING_DATE);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			calendar = Calendar.getInstance();
			calendar.setTime(startingDate);
			Date date = calendar.getTime();
			String str = formatter.format(date);
			URL = new StringBuilder(PREFIX_URL + str);
			crawler.retrieveInputStream(URL);

			if (ConfigurationConstants.debugMode)
				System.out.println(str);
		}
	}

	/**
	 * 
	 * @return instance of Crawler
	 */
	public static Crawler getInstance() {
		if (crawler == null)
			crawler = new Crawler();
		return crawler;
	}

	/**
	 * decides whether there is another URL to process or not
	 * 
	 * @return
	 */
	private static boolean hasNextURL() {

		calendar.add(Calendar.DATE, ConfigurationConstants.WEEK_DIFFERENCE);
		Date endingdate = calendar.getTime();
		String endDate = formatter.format(endingdate);

		if (!endDate.equals(ConfigurationConstants.END_DATE)) {
			calendar.add(Calendar.DATE, -ConfigurationConstants.WEEK_DIFFERENCE);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Computes the next URL
	 * 
	 * @return
	 */
	private static StringBuilder nextURL() {
		calendar.add(Calendar.DATE, ConfigurationConstants.WEEK_DIFFERENCE);
		Date date = calendar.getTime();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String str = formatter.format(date);
		URL = new StringBuilder(PREFIX_URL + str);
		return URL;
	}

	public void retrieveInputStream(StringBuilder feed) {
		URL url;
		try {
			System.out.println("");
			System.out.println(feed.toString());
			System.out.println("");

			url = new URL(feed.toString());

			HttpURLConnection httpSource = (HttpURLConnection) url
					.openConnection();
			InputStream is = httpSource.getInputStream();
			InputStreamReader irs = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(irs);
			String read = br.readLine();
			StringBuilder sb = new StringBuilder(read);
			while (read != null) {
				read = br.readLine();
				sb.append(read);
			}
			parseInput(sb);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseInput(StringBuilder inputHtml) {

		// begin the DB transaction
		InsertDataSrv.beginTransaction();
		Document doc = Jsoup.parse(inputHtml.toString());
		Elements tableElements = doc.select("table[class=headlineTable]");
		// there can be more than one table
		for (Element tableElement : tableElements) {
			Elements textElements = tableElement
					.select("td[class=headlineText]");
			Elements topicElements = tableElement
					.select("td[class=headlineTopic]");
			Elements comments = tableElement
					.select("td[class=headlineComments]");
			Elements source = tableElement
					.select("td[class=headlineSourceImage]");
			for (int i = 0; i < textElements.size(); i++) {
				ad = new ArticleDetails();
				ad.setFarkCategory(ConfigurationConstants.FARK_CATEGORY);

				Element textElement = textElements.get(i);
				Elements articleSourceElements = textElement
						.getElementsByTag("a");

				// Main Article Title Given by Author.
				Element articleElement = articleSourceElements.get(0);
				String farkHeadline = articleElement.text();
				ad.setFarkHeadline(farkHeadline);

				// Article URL
				String articleURL = articleElement.attr("href");
				ad.setArticleURL(articleURL);

				// Now Source URL
				Element articleSourceURLElement = articleSourceElements.get(1);
				String articleSourceURL = articleSourceURLElement.text();
				ad.setSourceURL(articleSourceURL);

				// Category given by the user e.g amusing etc.
				Element topicElement = topicElements.get(i);
				Elements image = topicElement.getElementsByTag("img");
				Element imageElement = image.get(0);
				String farkTag = imageElement.attr("alt");
				ad.setFarkTag(farkTag);

				// number of comments
				Element comment = comments.get(i);
				Elements commentElements = comment.getElementsByTag("a");
				Element commentElement = commentElements.get(0);
				// sometimes its not a number
				try {
					int numberOfComments = Integer.parseInt(commentElement
							.text().substring(1,
									commentElement.text().length() - 1));
					ad.setNumberOfComments(numberOfComments);
				} catch (NumberFormatException e) {
					ad.setNumberOfComments(1000);
				}

				// source...sometimes its an image and sometimes its a link
				Element articleSourceElement = source.get(i);
				Elements articleSourceImageElements = articleSourceElement
						.getElementsByTag("img");
				if (articleSourceImageElements.size() != 0) {
					Element articleSourceImageElement = articleSourceImageElements
							.get(0);
					String articleSource = articleSourceImageElement
							.attr("title");
					ad.setSourceName(articleSource);
				} else {
					articleSourceImageElements = articleSourceElement
							.getElementsByTag("a");
					Element articleSourceImageElement = articleSourceImageElements
							.get(0);
					String articleSource = articleSourceImageElement.text();
					articleSource = articleSource.substring(1,
							articleSource.length() - 1);
					ad.setSourceName(articleSource);
				}
				ad.setArticleContent(null);
				ad.setArticleTitle(null);
				if (ConfigurationConstants.debugMode)
					System.out.println("Article::::" + ad);
				InsertDataSrv.save(ad);
			}
		}
		InsertDataSrv.commit();
	}

	public static void main(String[] args) {

		if (ConfigurationConstants.INSERTION_FLAG) {
			while (hasNextURL()) {
				nextURL();
				crawler.retrieveInputStream(URL);
			}
			// close the entity manager
			InsertDataSrv.close();
		}

		if (ConfigurationConstants.ARTICLE_CONTENT_INSERTION) {
			if (ConfigurationConstants.SITE_TYPE.compareTo("siteTag") == 0) {
				for (String site : ConfigurationConstants.sites)
					crawler.insertArticleContentFromSiteAndTag(site, "amusing");
			} else if (ConfigurationConstants.SITE_TYPE.compareTo("all") == 0) {
				crawler.insertArticleContentbyTag("interesting");
			}
		}
		InsertDataSrv.close();
	}

	private void insertArticleContentbyTag(String tag) {

		List<AbstractDB> articleDetailList = null;
		RetrieveDataSrv.beginTransaction();
		articleDetailList = RetrieveDataSrv.retrieveRecordsbyTag(
				"ArticleDetails", tag);
		long count = 1;
		for (AbstractDB article : articleDetailList) {
			// System.out.println(((ArticleDetails)article).getArticleURL());
			String site = ((ArticleDetails) article).getSourceURL();
			String articleURL = getParsedURL(((ArticleDetails) article)
					.getArticleURL());
			Extractor extractor = ExtractorFactory.createExtractor(site);
			ArticleInfo ai = extractor.extractArticle(articleURL);
			if (ai != null) {
				((ArticleDetails) article).setArticleTitle(ai.getTitle());
				((ArticleDetails) article).setArticleContent(ai.getContent());

				if (ConfigurationConstants.debugMode) {
					System.out.println(ai.getContent());
					System.out.println(ai.getTitle());
				}
			}

			if (count % 100 == 0) {
				System.out.println("");
				System.out.println("*******  Committed  *********>");
				InsertDataSrv.commit();
				RetrieveDataSrv.beginTransaction();

			}
			if (ConfigurationConstants.debugMode) {
				System.out.println(articleURL);
				System.out.println("");
			}
			count++;
		}
		InsertDataSrv.commit();

	}

	private void insertArticleContentFromSiteAndTag(String site, String tag) {

		List<AbstractDB> articleDetailList = null;
		// RetrieveDataSrv.beginTransaction();
		articleDetailList = RetrieveDataSrv.retrieveRecords("ArticleDetails",
				site, tag);
		long count = 1;
		for (AbstractDB article : articleDetailList) {
			// System.out.println(((ArticleDetails)article).getArticleURL());

			String articleURL = getParsedURL(((ArticleDetails) article)
					.getArticleURL());
			Extractor extractor = ExtractorFactory.createExtractor(site);
			ArticleInfo ai = extractor.extractArticle(articleURL);
			if (ai != null) {
				((ArticleDetails) article)
						.setArticleTitle(ai.getTitle().trim());
				((ArticleDetails) article).setArticleContent(ai.getContent()
						.trim());

				if (ConfigurationConstants.debugMode) {
					System.out.println(ai.getContent());

					System.out
							.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					System.out.println("");
				}
			}
			// InsertDataSrv.save(article);

			if (count % 100 == 0) {
				if (ConfigurationConstants.debugMode) {
					System.out.println("");
					System.out
							.println(" ***********   committed  **********************");
				}
				InsertDataSrv.commit();
				RetrieveDataSrv.beginTransaction();
			}
			if (ConfigurationConstants.debugMode)
				System.out.println(articleURL);
			count++;

		}
		InsertDataSrv.commit();
	}
	
	private String getParsedURL(String articleURL) {

		String URL = articleURL.substring(articleURL.indexOf("l=") + 2);
		String finalURL[] = URL.split("%");
		return finalURL[0];
	}

}
