package extractors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import beans.ArticleInfo;

public class DailyMailExtractor extends Extractor {
    
	
	private static DailyMailExtractor extractor = null;
	
	private DailyMailExtractor()
	{   
		
	}
	
	public static Extractor getInstance()
	{
		if(extractor == null)
			extractor = new DailyMailExtractor();
		return extractor;
	}
    
    public  ArticleInfo extractArticle(String URL) {
        
        // connect to JSOUP
        Document doc = null;
        ArticleInfo articleInfo = new ArticleInfo();
        try 
        {
        	doc = Jsoup.connect(URL).timeout(10000).ignoreHttpErrors(true).get();
            Elements all = doc.getAllElements().not("script").not("div");
            int pCount = 0;
            int ctdOther = 0;
            StringBuilder text = new StringBuilder();
            for (Element e : all.not("script").not("div"))
            {
                if(e.nodeName().compareTo("p") == 0)
                {
                	if(e.className().compareTo("MsoNormal") == 0 || e.className().compareTo("") == 0)
	                {
	                    if (pCount >= 1 && ctdOther > 5)
	                    {
	                        pCount = 0;
	                        text.setLength(0);
	                    }
	                    text.append(e.text()+" ");
	                    pCount++;
	                    ctdOther = 0;
	                }
                }
                else 
                {
                    ctdOther++;
                }
                if (pCount > 5 && ctdOther > 5)
                {
                    break;
                }
            }// Errors throw null wont get inserted in DB.
            
            Elements e1 = doc.select("title");
            if(e1.html().contains("403") || e1.html().contains("404") || e1.html().contains("Apache Tomcat/6.0.18") || e1.html().contains("ERROR") || e1.html().equals("FARK.com:")|| text.length() <= 300)
                 return null;
            else
            	articleInfo.setTitle(e1.text().replaceAll("[|] .*",""));
            
            articleInfo.setContent(text.toString());
            return articleInfo;
        }
        catch (Exception e) {
            // Exceptions throw null wont get inserted in DB.
            //    e.printStackTrace();
            return null;
        }
    }
}