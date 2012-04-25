package analyzers;

import java.util.Iterator;
import java.util.List;

import parsers.LexParser;
import services.InsertDataSrv;
import services.RetrieveDataSrv;
import constants.ConfigurationConstants;
import edu.stanford.nlp.trees.Tree;
import entities.AbstractDB;
import entities.ArticleDetails;
import entities.ParseDetails;

class Parser {

	private static LexParser parser;
	private static ParseDetails pd;
	long sentencesLimit = 10;
	
	// Parse Tree Attributes
	static double countNP = 0; 		// Noun
	static double countVP = 0; 		// verb
	static double countAdjP = 0; 	// Adjective
	static double countCP = 0; 		// Conjunction
	static double countS = 0; 		// Sentences
	static double depth = 0; 		// Depth
	static double score = 0; 		// Tree Grammar Score
	static double size = 0;			// Size
	static double sentences = 0;	// Sentences


	/**
	 * Fetch articles from database and then parse the article content using
	 * Stanford Parser and compute semantic features
	 */
	public static void parseArticles() {
		
		String[] tags = { "amusing", "cool", "obvious", "interesting" };
		
		List<AbstractDB> articleList = RetrieveDataSrv.retrieveRecords("ArticleDetails", tags);
		System.out.println("Size of article dataset: " + articleList.size());

		List<Integer> articleIds = RetrieveDataSrv.retrieveAttr("ParseDetails", "id");
		System.out.println("Size of parse dataset: " + articleIds.size());
		
		for (AbstractDB article : articleList) {
			
			boolean flag = false;
			int articleId = ((ArticleDetails) article).getId();
			if(ConfigurationConstants.debugMode)
				System.out.println("Processing article: "+articleId);

			for(Integer parseId : articleIds)	{
				if(articleId == parseId)	{
					flag = true;
					break;
				}
			}
			if(flag)
				continue;
			
			InsertDataSrv.beginTransaction();
			
			pd = new ParseDetails();
			pd.setId(articleId);
			
			// Parse the article content
			List<Tree> parseSentences = parser.parseArticle(article);
			compute(parseSentences);
			
			pd.setNounPhrases(countNP);
			pd.setVerbPhrases(countVP);
			pd.setAdjPhrases(countAdjP);
			pd.setConjPhrases(countCP);
			pd.setSentPhrases(countS);
			pd.setDepth(depth);
			pd.setScore(score);
			pd.setSize(size);
			pd.setSentences(sentences);
			
			InsertDataSrv.save(pd);
			InsertDataSrv.commit();
		}
	}

	private static void compute(List<Tree> parseTree) {
		
		countNP = 0; 		
		countVP = 0; 	
		countAdjP = 0; 	
		countCP = 0; 		
		countS = 0; 		
		depth = 0; 		
		score = 0; 		
		size = 0;		
		sentences = 0;	

		for(Tree parse: parseTree)	{
		
			depth += parse.depth();
			score += parse.score();
			size += parse.size();
			// parse.pennPrint();
	
			Iterator<Tree> itr = parse.iterator();
			String tempStr = "";
			while (itr.hasNext()) {
				Tree obj = itr.next();
				String str = obj.nodeString().toString();
				String[] splitter = str.split(" ");
	
				tempStr = splitter[0];
	
				if (tempStr.toString().compareTo("NP") == 0)
					countNP++;
				else if (tempStr.toString().compareTo("VP") == 0)
					countVP++;
				else if (tempStr.toString().compareTo("ADJP") == 0)
					countAdjP++;
				else if (tempStr.toString().compareTo("CONJP") == 0)
					countCP++;
				else if (tempStr.toString().compareTo("S") == 0)
					countS++;
	
			}
			sentences++;;
		}
		System.out.println("Parse terminates");
		System.out.println("Noun Phrases: " + countNP);
		System.out.println("Verb Phrases: " + countVP);
		System.out.println("Adjective Phrases: " + countAdjP);
		System.out.println("Conjuctive Phrases: " + countCP);
		System.out.println("Sentence Phrases: " + countS);
		System.out.println("Depth: " + depth);
		System.out.println("Score: " + score);
		System.out.println("Size: " + size);
		System.out.println("Sentences Processed: " + sentences);

	}

	public static void main(String args[]) {
		parser = new LexParser();
		parseArticles();
	}
}