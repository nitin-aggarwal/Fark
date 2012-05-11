package features;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import parsers.LexParser;
import services.InsertDataSrv;
import constants.ConfigurationConstants;
import edu.stanford.nlp.trees.Tree;
import entities.AbstractDB;
import entities.ArticleDetails;
import entities.ParseDetails;

public class ParserPCFG extends Feature	{
	
	private static ParserPCFG parserPCFG = null;
	
	private static LexParser parser;
	private static ParseDetails pd;
	
	
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

	private ParserPCFG() {
	}

	public static ParserPCFG getInstance() {
		if (parserPCFG == null)
		{
			parserPCFG = new ParserPCFG();
			parser = new LexParser();
		}
		return parserPCFG;
	}

	/**
	 * Compute PARSE - SEMANTIC features for article content strPOS, 
	 * and create an entry in ParseDetails file in database
	 */
	@Override
	public void calculateFeatureVector(AbstractDB object, StringBuilder strPOS, File file)
			throws IOException {
		// TODO Auto-generated method stub
		List<Tree> parseSentences = null;
		// Parse the article content
		try	{
			parseSentences = parser.parseArticle(object);
		}
		catch(OutOfMemoryError e)	{
			System.gc();
			System.out.println("Parsing failed");
			return;
		}
		InsertDataSrv.beginTransaction();
		
		pd = new ParseDetails();
		pd.setId(((ArticleDetails) object).getId());
		
		compute(parseSentences);
		
		// Update the attributes
		pd.setNounPhrases(countNP);
		pd.setVerbPhrases(countVP);
		pd.setAdjPhrases(countAdjP);
		pd.setConjPhrases(countCP);
		pd.setSentPhrases(countS);
		pd.setDepth(depth);
		pd.setScore(score);
		pd.setSize(size);
		pd.setSentences(sentences);
		
		// Update the row in the database for the article
		InsertDataSrv.save(pd);
		InsertDataSrv.commit();
		
		
	}
	
	/**
	 * Computes different attribute on the basis of parse tree generated
	 * with the help of Stanford parser
	 * @param parseTree
	 */
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
		if(ConfigurationConstants.debugMode)
		{
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
	}

}
