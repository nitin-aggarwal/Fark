package factory;

import extractors.CNNExtractor;
import extractors.DailyMailExtractor;
import extractors.Extractor;
import extractors.YahooExtractor;

public class ExtractorFactory {

	public static Extractor createExtractor(String site)
	{
		if(site.equals("news.yahoo.com"))
			return YahooExtractor.getInstance();
		else if(site.equals("dailymail.co.uk"))
			return DailyMailExtractor.getInstance();
		else if(site.equals("cnn.com"))
			 return CNNExtractor.getInstance();
		else
			return Extractor.getInstance();
	}
}
