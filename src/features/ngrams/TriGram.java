package features.ngrams;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import constants.ConfigurationConstants;

/*
 * This class is for features computation related to
 * Tri-gram POS
 */
public class TriGram extends NGrams {

	private static TriGram triGram = null;

	private TriGram() {
	}

	public static TriGram getInstance() {
		if (triGram == null)
			triGram = new TriGram();
		return triGram;
	}

	/**
	 * Compute TRIGRAM POS features for a POS tagged article content strPOS, and
	 * create a file in folder trigramPOS
	 */
	@Override
	public void calculateFeatureVector(StringBuilder strPOS, File file)
			throws IOException {

		HashMap<String, Integer> map = new HashMap<String, Integer>();
		StringBuilder trigramString = new StringBuilder();
		Integer count;
		String temp = strPOS.toString().trim();
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file));
			if (ConfigurationConstants.debugMode)
				System.out.println(strPOS);

			String[] spacesSplitter = temp.split("\\s");
			int counter = 0;
			for (String spaceSeperated : spacesSplitter) {
				String[] splitter = spaceSeperated.split("/");
				if (splitter.length == 2) {
					if (counter != 0)
						trigramString.append(" " + splitter[1].toUpperCase());
					else
						trigramString.append(splitter[1].toUpperCase());
				}
				counter++;
			}

			String trigramPOSString = trigramString.toString();
			String[] posTags = trigramPOSString.split("\\s");
			for (int increment = 0; increment < posTags.length - 2; increment++) {
				String triGramtag = posTags[increment] + " "
						+ posTags[increment + 1] + " " + posTags[increment + 2];
				if ((count = map.get(triGramtag)) != null)
					map.put(triGramtag, ++count);
				else
					map.put(triGramtag, 1);
			}

			Set<Entry<String, Integer>> entrySet = map.entrySet();
			Iterator<Entry<String, Integer>> iterator = entrySet.iterator();

			while (iterator.hasNext()) {
				Entry<String, Integer> entry = iterator.next();
				if (ConfigurationConstants.debugMode)
					System.out
							.println(entry.getKey() + ": " + entry.getValue());
				bw.write(entry.getKey() + " " + entry.getValue());
				bw.write("\n");
			}
			bw.flush();
		} catch (IOException e) {
			System.out.println("Exception Inside Trigram Feature Computation");
			e.printStackTrace();
		} finally {
			bw.close();
		}

	}
}
