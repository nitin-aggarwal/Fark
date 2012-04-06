package extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import beans.ArticleInfo;

public class CNNExtractor extends Extractor {

	private static CNNExtractor extractor = null;
	
	private CNNExtractor()
	{   
		
	}
	
		
	public static Extractor getInstance()
	{
		if(extractor == null)
			extractor = new CNNExtractor();
		return extractor;
	}
	
public ArticleInfo extractArticle(String URL) {
        
        // connect to JSOUP
        Document doc = null;
        Elements all = null;
        ArticleInfo articleInfo = new ArticleInfo();
        if(URL.contains("2010"))
        	System.out.println("2010");
        try 
        {
            doc = Jsoup.connect(URL).timeout(10000).ignoreHttpErrors(true).get();
             all = doc.select("div[class=cnn_strycntntlft]");
			 if(all.size() == 0)
				 all = doc.select("div[class=cnnContentContainer]");
			 //if(all.size())
			 if(all.size() == 0)
				return null;
            StringBuilder text = new StringBuilder();
            for (Element e : all.first().getAllElements())
            {
            	Elements pall = e.getAllElements();
                if (e.nodeName().compareTo("p") == 0 && pall.size()<=3)//&& e.className() == "")
                {
                	text.append(e.text()).append(" ");
                }
            }
            Elements e1 = doc.select("title");
            if(e1.html().contains("403") || e1.html().contains("Apache Tomcat/6.0.18") || e1.html().contains("ERROR") || e1.html().equals("FARK.com:")|| text.length() <= 300)
                return null;
           else
           	articleInfo.setTitle(e1.text().replaceAll("[|] .*","").replaceAll("[-] .*",""));
           
          //  String finall = text.toString().replaceAll("(.)*(CNN)(.)*[:(--)]","");
            String content = null;
           // System.out.println(text);
            //System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            if(text.toString().contains("(CNN)"))
            	content = text.substring(text.toString().indexOf("(CNN)")+9);
            else
            	content = text.toString();
            if(content == null)
            	return null;
           articleInfo.setContent(content);
           return articleInfo;
        }
        catch (Exception e) {
            // Exceptions throw null wont get inserted in DB.
     //       e.printStackTrace();
            return null;
        }
    }
}
