package extractor;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CustomPosAnnotator implements Annotator {
	// Used to store the mapping from the original text to the predicted text.
    public Map<String, String> textToPredTextMap;
    // Used to store the mapping from the predicted text to POS tagging.
    public Map<String, List<Map<String, String>>> predTextToPosMap;

    public CustomPosAnnotator(String name, Properties props, String filePath) throws ParseException {
        textToPredTextMap = new HashMap<>();
        predTextToPosMap = new HashMap<>();
        loadPreProcessedData(filePath); // Load data from a file.
    }

    // Load preprocessed data.
    private void loadPreProcessedData(String filePath) throws ParseException {
    	JSONParser parser = new JSONParser();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                JSONObject json = (JSONObject) parser.parse(line);
                String text = (String) json.get("text");         
                String predText = (String) json.get("pred_text"); 
                JSONArray posArray = (JSONArray) json.get("POS");
                textToPredTextMap.put(text, predText);
                List<Map<String, String>> posList = new ArrayList<>();
                for (Object obj : posArray) {
                    JSONArray posInfo = (JSONArray) obj;
                    Map<String, String> tokenData = new HashMap<>();
                    tokenData.put("index", String.valueOf(posInfo.get(0)));  
                    tokenData.put("word", (String) posInfo.get(1));          
                    tokenData.put("pos", (String) posInfo.get(2));           
                    posList.add(tokenData);
                }
                predTextToPosMap.put(predText, posList);     
            }
        } catch (IOException | org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void annotate(Annotation annotation) {
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
            boolean is_success = false;
            // Obtain POS annotation through pred_text
            if (predTextToPosMap.containsKey(sentenceText)) {
                List<Map<String, String>> posList = predTextToPosMap.get(sentenceText);
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                int currentOffset = 0;
                for (int i = 0; i < tokens.size(); i++) {
                    CoreLabel token = tokens.get(i);
                    String tokenWord = token.word();
                    // Set the starting and ending positions
                    int tokenBegin = currentOffset;
                    int tokenEnd = currentOffset + tokenWord.length();
                    // Update the starting and ending positions of the token
                    token.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, tokenBegin);
                    token.set(CoreAnnotations.CharacterOffsetEndAnnotation.class, tokenEnd);
                    // Update the current offset to prepare for the next token
                    currentOffset = tokenEnd;
                    // Check if the token and preprocessing results are consistent, and replace the POS
                    if (i < posList.size() && posList.get(i).get("word").equals(tokenWord)) {
                        token.set(CoreAnnotations.PartOfSpeechAnnotation.class, posList.get(i).get("pos"));
                        is_success = true;
                    }
                }
            }           	
        }
    }
    
    // Retrieve preprocessed text
    public String getPreProcessedText(String originalText) {
    	if ( textToPredTextMap.containsKey(originalText) ){
    		return textToPredTextMap.get(originalText);
    	}else {
    		return null;
    	}
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(CoreAnnotations.PartOfSpeechAnnotation.class);
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.singleton(CoreAnnotations.TokensAnnotation.class);
    }

	public Map<String, List<Map<String, String>>> getPredTextToPosMap() {
		return predTextToPosMap;
	}

	public void setPredTextToPosMap(Map<String, List<Map<String, String>>> predTextToPosMap) {
		this.predTextToPosMap = predTextToPosMap;
	}

	public Map<String, String> getTextToPredTextMap() {
		return textToPredTextMap;
	}

	public void setTextToPredTextMap(Map<String, String> textToPredTextMap) {
		this.textToPredTextMap = textToPredTextMap;
	}
}
