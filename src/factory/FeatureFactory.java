package factory;

import features.Feature;
import features.PageRank;
import features.ParserPCFG;
import features.SentenceCohesion;
import features.ngrams.BiGram;
import features.ngrams.TriGram;
import features.ngrams.UniGram;
import features.ngrams.UniGramWord;

public class FeatureFactory {

	public static Feature createFeatureVector(String feature)
	{
		if(feature.equals("unigramPOS"))
			return UniGram.getInstance();
		else if(feature.equals("unigramWord"))
			return UniGramWord.getInstance();
		else if(feature.equals("bigramPOS"))
			return BiGram.getInstance();
		else if(feature.equals("trigramPOS"))
			return TriGram.getInstance();
		else if(feature.equals("parser"))
			return ParserPCFG.getInstance();
		else if(feature.equals("coherence"))
			return SentenceCohesion.getInstance();
		else if(feature.equals("pageRank"))
			return PageRank.getInstance();
		else
			return null;
	}
}
