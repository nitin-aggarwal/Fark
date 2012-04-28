package parsers;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import constants.ConfigurationConstants;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.Tree;
import entities.AbstractDB;
import entities.ArticleDetails;

public class LexParser {

	LexicalizedParser lp;
	
	// Processing parsing features on the basis of maximum 100 sentences
	static long sentencesLimit = 100;
	static int sentences = 0;

	public LexParser() {
		lp = new LexicalizedParser("parsers/englishPCFG.ser.gz");
		lp.setOptionFlags("-maxLength", "1000", "-retainTmpSubcategories");
	}

	public List<Tree> parseArticle(AbstractDB article) {
		// TODO Auto-generated method stub

		sentences = 0;
		System.out.println("Parsing started");
		
		String articleContent = ((ArticleDetails)article).getArticleContent();
		
		System.gc();
		if(ConfigurationConstants.debugMode)	{
			System.out.println("Available Memory: "+Runtime.getRuntime().freeMemory());
			System.out.println("Total Memory: "+Runtime.getRuntime().totalMemory());
			System.out.println("Max memory: "+Runtime.getRuntime().maxMemory());
		}
		
		List<Tree> parse = new ArrayList<Tree>();
		
		Reader input = new StringReader(articleContent);
		DocumentPreprocessor dp = new DocumentPreprocessor(input);
		for (List<HasWord> sentence : dp) {
			if(sentences >= sentencesLimit)
				break;
			parse.add(lp.apply(sentence));
			sentences++;
		}
		
		if(ConfigurationConstants.debugMode)
			System.out.println("Parse terminates");
		
		return parse;
	}

}
