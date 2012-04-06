package factory;

import features.ngrams.BiGram;
import features.ngrams.NGrams;
import features.ngrams.TriGram;
import features.ngrams.UniGram;
import features.ngrams.UniGramWord;

public class NGramFactory {

	public static NGrams createFeatureVector(String feature)
	{
		if(feature.equals("unigramPOS"))
			return UniGram.getInstance();
		else if(feature.equals("unigramWord"))
			return UniGramWord.getInstance();
		else if(feature.equals("bigramPOS"))
			return BiGram.getInstance();
		else if(feature.equals("trigramPOS"))
			return TriGram.getInstance();
		else
			return null;
	}
}
