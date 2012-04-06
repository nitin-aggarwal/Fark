package extractors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import beans.ArticleInfo;

public class YahooExtractor extends Extractor{
    
private static YahooExtractor extractor = null;
	
	private YahooExtractor()
	{   
		
	}
	
		
	public static Extractor getInstance()
	{
		if(extractor == null)
			extractor = new YahooExtractor();
		return extractor;
	}
	
    
    public ArticleInfo extractArticle(String URL) {
        
        // connect to JSOUP
        Document doc = null;
        ArticleInfo articleInfo = new ArticleInfo();
        try 
        {
            doc = Jsoup.connect(URL).timeout(10000).ignoreHttpErrors(true).get();
            Elements all = doc.getAllElements().select("div[class^=yom-mod yom-art-content");
            
            StringBuilder text = new StringBuilder();
            if(all.first() != null)
            for (Element e : all.first().getAllElements().select("div.bd").first().getAllElements())
            {
            	if (e.nodeName().compareTo("p") == 0)
                {
                	for(Element ele: e.getAllElements())
                		text.append(ele.text()+" ");
                }
            }
            Elements e1 = doc.select("title");
            StringBuilder temp = new StringBuilder(text.toString()
                .replaceAll("</p>", "</p>\n").replaceAll("&quot;", "\"")
                .replaceAll("\\<.*?\\>", "").replaceAll("\\s(\\s)+","").replaceAll("&nbsp;","").trim());
            
            // Errors throw null wont get inserted in DB.
            if(e1.html().contains("403") || e1.html().contains("Apache Tomcat/6.0.18") || e1.html().contains("ERROR") || e1.html().equals("FARK.com:")|| temp.length() <= 300)
                return null;
            else
            	articleInfo.setTitle(e1.text().replaceAll("- Yahoo! News", "").replaceAll("[|] .*",""));
            
            articleInfo.setContent(temp.toString());
            return articleInfo;
        }
        catch (Exception e) {
            // Exceptions throw null wont get inserted in DB.
            //    e.printStackTrace();
            return null;
        }
    }
}