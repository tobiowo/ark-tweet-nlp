package cmu.arktweetnlp.impl.features;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import cmu.arktweetnlp.Twokenize;
import cmu.arktweetnlp.impl.features.FeatureExtractor.FeatureExtractorInterface;
import cmu.arktweetnlp.impl.features.FeatureExtractor.PositionFeaturePairs;
import cmu.arktweetnlp.util.BasicFileIO;
import edu.stanford.nlp.util.StringUtils;

/**
 * Brown word clusters: features are path prefixes down the tree. 
 **/
public class WordClusterPaths implements FeatureExtractorInterface {
	
	/** TODO this should be moved into config somehow **/
	public static String clusterResourceName = "/cmu/arktweetnlp/50mpaths2";
	
	public static HashMap<String,String> wordToPath;

	public WordClusterPaths() throws IOException {
//		log.info("Loading clusters");
		
		//read in paths file
		BufferedReader bReader = BasicFileIO.getResourceReader(clusterResourceName);
		String[] splitline = new String[3];
		String line=BasicFileIO.getLine(bReader);
		wordToPath = new HashMap<String,String>(); 
		while(line != null){
			splitline = line.split("\\t");
			wordToPath.put(splitline[1], splitline[0]);
			line = BasicFileIO.getLine(bReader);
		}			
//		log.info("Finished loading clusters");
	}
	
	public void addFeatures(List<String> tokens, PositionFeaturePairs pairs) {
		String bitstring = null;
		List<String> pos_tokens = new ArrayList<String>(tokens.size()+2);
	        pos_tokens.add("SSSSSSSSSSSSSSSS");
		for (int t=0; t < tokens.size(); t++) {
	            String tok = tokens.get(t);
		    String normaltok = FeatureUtil.normalize(tok);
		    bitstring = wordToPath.get(normaltok);
		    if (bitstring == null){
		    	for (String fuzz : FeatureUtil.fuzztoken(normaltok, true)){
		    		bitstring = wordToPath.get(fuzz);
		    		if (bitstring != null){
		    			//System.err.println(normaltok+"->"+fuzz);
		    			break;
		    		}
		    	}
		    }
		    
			if (bitstring != null){
				int i;
				bitstring = StringUtils.pad(bitstring, 16).replace(' ', '0');
				for(i=2; i<=16; i+=2){
					pairs.add(t, "BigCluster|" + bitstring.substring(0,i));
				}
				if (t<tokens.size()-1){
					for(i=4; i<=12; i+=4)
						pairs.add(t+1, "PrevBigCluster|" + bitstring.substring(0,i));
				}
				if (t>0){
					for(i=4; i<=12; i+=4)
						pairs.add(t-1, "NextBigCluster|" + bitstring.substring(0,i));
				}
				pos_tokens.add(bitstring.substring(0, 16));
			}
			else{
			    pairs.add(t, "BigCluster|none"+tok.charAt(0));
                            pos_tokens.add("nonenonenonenone");
			}
		}
		pos_tokens.add("EEEEEEEEEEEEEEEE");
		for (int i = 0; i < tokens.size(); i++){
		    pairs.add(i, "PrevNextBigCluster|" + pos_tokens.get(i)+" "+pos_tokens.get(i+2));
		    pairs.add(i, "PrevCurrBigCluster|" + pos_tokens.get(i)+" "+pos_tokens.get(i+1));
		    pairs.add(i, "CurrNextBigCluster|" + pos_tokens.get(i+1)+" "+pos_tokens.get(i+2));
		    pairs.add(i, "PrevNextBigCluster|" + pos_tokens.get(i).substring(0, 8)+" "+pos_tokens.get(i+2).substring(0, 8));
		    pairs.add(i, "PrevCurrBigCluster|" + pos_tokens.get(i).substring(0, 8)+" "+pos_tokens.get(i+1).substring(0, 8));
                    pairs.add(i, "CurrNextBigCluster|" + pos_tokens.get(i+1).substring(0, 8)+" "+pos_tokens.get(i+2).substring(0, 8));
		}
	}
}
