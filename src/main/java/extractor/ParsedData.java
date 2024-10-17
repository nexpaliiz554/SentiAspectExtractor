package extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import extractor.coreNLPRules.CoreNLPRules;

public class ParsedData {
	public AnalysisOptions opt;
	public CoreNLPRules NLPRule;
	public static float coefficient_for_threshold = (float) 0.01;
	public static int base_for_threshold = 3;
	ArrayList<ParsedText> parseResultList;
	
	public ParsedData(AnalysisOptions opt,CoreNLPRules NLPRule) {
		super();
		this.opt = opt;
		this.NLPRule = NLPRule;
		this.parseResultList = new ArrayList<ParsedText>();
	}
	
	public void addParsedText(ParsedText pt) {
		parseResultList.add(pt);
	}

	public ArrayList<ParsedText> getParseResultList() {
		return parseResultList;
	}

	public void setParseResultList(ArrayList<ParsedText> parseResultList) {
		this.parseResultList = parseResultList;
	}
	
	public void addDefaultAspectAccordingFrequency() {
		// Step 1: Count the occurrence frequency of all mined aspects
	    Map<String, Integer> aspectFrequencyMap = new HashMap<>();  // Store the number of times an aspect appears
	    for (ParsedText parsedText : parseResultList) {
	        ArrayList<ArrayList<Aspect>> aspectListOfOpinion = parsedText.getAspectListOfOpinion();
	        if (aspectListOfOpinion != null) {
	            for (ArrayList<Aspect> aspectList : aspectListOfOpinion) {
	                if (aspectList != null) {
	                    for (Aspect aspect : aspectList) {
	                        String aspectText = aspect.getAspectListString(); 
	                        aspectFrequencyMap.put(aspectText, aspectFrequencyMap.getOrDefault(aspectText, 0) + 1);
	                    }
	                }
	            }
	        }
	    }
	    
	    // Step 2: Find frequently aspects
	    int threshold = Math.max((int) Math.ceil(parseResultList.size() * coefficient_for_threshold), base_for_threshold);
	    Set<String> frequentAspects = new HashSet<>();
	    for (Map.Entry<String, Integer> entry : aspectFrequencyMap.entrySet()) {
	        if (entry.getValue() >= threshold) {
	            frequentAspects.add(entry.getKey());
	        }
	    }
	    
	    // Step 3: Search for these frequent aspects in the text and add them to the opinion [-2, -2]
	    for (ParsedText parsedText : parseResultList) {
	    	ArrayList<Opinion> opinion_list = parsedText.getOpinion_list();
	    	ArrayList< ArrayList<Aspect> > aspectListOfOpinion = parsedText.getAspectListOfOpinion();
	        // Check if there are frequent aspects in the text:
	        ArrayList<Aspect> defaultAspectList = findFrequentAspectsInText(parsedText,frequentAspects);
	        if (!defaultAspectList.isEmpty()) {
	            Opinion defaultOpinion = new Opinion();  
	            defaultOpinion.setOriIndexArr(new int[] {-2, -2});  
	            opinion_list.add(defaultOpinion);
	            aspectListOfOpinion.add(defaultAspectList);  
	        }
	    }
	}
	
	private ArrayList<Aspect> findFrequentAspectsInText(ParsedText parsedText, Set<String> frequentAspects){
		ArrayList<Aspect> defaultAspectList = new ArrayList<>();
		ArrayList<IndexedWord> nodeList = parsedText.getNodeList();
		
		// Traverse each frequent aspect
	    for (String frequentAspect : frequentAspects) {
	        // Check for matching frequentAspect
	        ArrayList<IndexedWord> matchedNodes = matchAspectInNodeList(frequentAspect, nodeList);
	        if (!matchedNodes.isEmpty()) {  // If a matching aspect is found
	            // Check for longer matching aspects
	            boolean isMatch = true;
	            for (String otherAspect : frequentAspects) {
	                if ( !otherAspect.equals(frequentAspect) && otherAspect.contains(frequentAspect) ) {
	                    ArrayList<IndexedWord> longerMatchedNodes = matchAspectInNodeList(otherAspect, nodeList);
	                    if (!longerMatchedNodes.isEmpty()) {
	                        isMatch = false;  // If a longer aspect is found, mark it as a mismatch
	                        break;
	                    }
	                }
	            }
	            if (isMatch) {
	            	Aspect newAspect = new Aspect(matchedNodes.get(0), "[R-32] Frequent Aspect");
	            	SemanticGraph graph = parsedText.getGraphByNode(matchedNodes.get(0));
	            	ArrayList<IndexedWord> aspectNodeList = refineAspectScope(matchedNodes,graph);
	            	newAspect.setAspectNodeList(aspectNodeList);
                    int[] aspectOriIndex = new int[2];
        	        aspectOriIndex[0] = aspectNodeList.get(0).get(IndexedWordKeys.StartOriIndex.class);
        	        aspectOriIndex[1] = aspectNodeList.get(aspectNodeList.size()-1).get(IndexedWordKeys.EndOriIndex.class)+1;
        	        newAspect.setOriIndexArr(aspectOriIndex);
        	        defaultAspectList.add(newAspect);
                }
	        }
        }
	    return defaultAspectList;
	}
	
	private ArrayList<IndexedWord> refineAspectScope(ArrayList<IndexedWord> nodelist, SemanticGraph graph){
		ArrayList<IndexedWord> refinedAspectNodeList = new ArrayList<IndexedWord>();
		ArrayList<IndexedWord> compoundNodeList = new ArrayList<IndexedWord>();
		for(IndexedWord node:nodelist) {
			ArrayList<IndexedWord> compoundNodes = NLPRule.getChildCompoundNode(node, graph);	
			if( !compoundNodes.isEmpty() ) {
				compoundNodeList.addAll(compoundNodes);
			}
		}
		compoundNodeList.addAll(nodelist);
		int startTokenIndex = nodelist.get(0).index();
		int endTokenIndex = nodelist.get(0).index();
		for(IndexedWord compoundNode:compoundNodeList) {
			startTokenIndex = Math.min(startTokenIndex, compoundNode.index());
			endTokenIndex = Math.max(endTokenIndex, compoundNode.index());
		}
		for(int i=startTokenIndex;i<=endTokenIndex;i++) {
			IndexedWord node = graph.getNodeByIndex(i);
			refinedAspectNodeList.add(node);
		}
		return refinedAspectNodeList;
	}
	
	private ArrayList<IndexedWord> matchAspectInNodeList(String frequentAspect, ArrayList<IndexedWord> nodeList) {
	    String[] aspectWords = frequentAspect.split(" ");
	    int aspectLength = aspectWords.length;
	    ArrayList<IndexedWord> matchingNodes = new ArrayList<>();
	    for (int i = 0; i <= nodeList.size() - aspectLength; i++) {
	        boolean isMatch = true;
	        matchingNodes.clear();
	        for (int j = 0; j < aspectLength; j++) {
	            IndexedWord currentWord = nodeList.get(i + j);
	            String currentLemma = currentWord.lemma();
	            // Check if Lemma matches and if the part of speech is a noun
	            if ( !(currentLemma.equals(aspectWords[j]) && NLPRule.isNoun(currentWord)) ) {
	                isMatch = false;
	                break;
	            }
	            matchingNodes.add(currentWord);  // Add matching words to the list
	        }
	        if (isMatch) {
	            return new ArrayList<>(matchingNodes);  // Return a list of matching nodes
	        }
	    }
	    return new ArrayList<>();  // No match returned empty list
	}
	
	public void formatOutput_Zero(String outputFileName) {
		String output_text = "";
		for(ParsedText rs:parseResultList) {
			if( !rs.isParsed() ) {
				output_text += rs.getInputText();
			}
			else {
				String rsForText = "";
				int[] nullArr = {-1,-1};
				String nullArrString = transIntArrToString(nullArr);
				ArrayList<Opinion> opinion_list = rs.getOpinion_list();
				ArrayList< ArrayList<Aspect> > aspectListOfOpinion = rs.getAspectListOfOpinion();
				for(int i=0;i<opinion_list.size();i++) {
					Opinion op = opinion_list.get(i);
					int[] oriIndexOPArr = op.getOriIndexArr();
					String oriIndexOPStr = transIntArrToString(oriIndexOPArr);
					ArrayList<Aspect> aspectList = aspectListOfOpinion.get(i);
					String rsForOpinion = "" ;
					// implicit opinion 
					if( aspectList==null ) {
						if( opt.implicitOpinionDealType==0 ) {
							rsForOpinion += oriIndexOPStr+":cannot_deal";
						}else if( opt.implicitOpinionDealType==1 ) {
							rsForOpinion += oriIndexOPStr+":"+nullArrString;
						}else if( opt.implicitOpinionDealType==2 ) {
							continue;
						}
					}
					//explicit opinion:
					else {
						if( aspectList.size()==0 ) {
							rsForOpinion += oriIndexOPStr + ":" + nullArrString;
							if( opt.isExplain ) {
								rsForOpinion += "null";
							}
						}else {
							rsForOpinion += oriIndexOPStr + ":";
							for(int k=0;k<aspectList.size();k++) {
								Aspect ap = aspectList.get(k);
								int[] aspectOriIndexArr = ap.getOriIndexArr();
								rsForOpinion += transIntArrToString(aspectOriIndexArr);
								if( opt.isExplain ) {
									rsForOpinion += ap.getReasonForSelection();
								}
								if( k!=aspectList.size()-1 ) {
									rsForOpinion += " , ";
								}
							}
						}
					}
					rsForText += rsForOpinion+"; ";
				}
				output_text += rs.getInputText()+"\t"+rsForText;
			}
			output_text += "\n";
		}
		FileIOs.writeStringToFile(outputFileName, output_text, false);
	}
	
	public void formatOutput_One(String outputFileName) {
		ArrayList<String> outputTextList = new ArrayList<String>();
		for(ParsedText rs:parseResultList) {
			String rsForText = "";
			ArrayList<JSONObject> jsonObjectList = new ArrayList<JSONObject>();
			if( !rs.isParsed() ) {
				;
			}
			else {
				int[] nullArr = {-1,-1};
				ArrayList<Opinion> opinion_list = rs.getOpinion_list();
				ArrayList< ArrayList<Aspect> > aspectListOfOpinion = rs.getAspectListOfOpinion();
				for(int i=0;i<opinion_list.size();i++) {
					Opinion op = opinion_list.get(i);
					int[] oriIndexOPArr = op.getOriIndexArr();
					ArrayList<Aspect> aspectList = aspectListOfOpinion.get(i);
					// implicit opinion 
					if( aspectList==null ) {
						if( opt.implicitOpinionDealType==0 ) {
							JSONObject object = new JSONObject();
							object.put("opinion_index",i);
							object.put("opinion", transIntArrToList(oriIndexOPArr) );
							object.put("aspect","cannot_deal");
							//object.put("is_aspect_derived_pronoun",  false );
							jsonObjectList.add(object);
						}else if( opt.implicitOpinionDealType==1 ) {
							JSONObject object = new JSONObject();
							object.put("opinion_index",i);
							object.put("opinion", transIntArrToList(oriIndexOPArr) );
							object.put("aspect", transIntArrToList(nullArr) );
							//object.put("is_aspect_derived_pronoun",  false );
							jsonObjectList.add(object);
						}else if( opt.implicitOpinionDealType==2 ) {
							continue;
						}
					}
					//explicit opinion:
					else {
						if( aspectList.size()==0 ) {
							JSONObject object = new JSONObject();
							object.put("opinion_index",i);
							object.put("opinion",transIntArrToList(oriIndexOPArr) );
							object.put("aspect", transIntArrToList(nullArr) );
							//object.put("is_aspect_derived_pronoun",  false );
							if( opt.isExplain ) {
								object.put("explain","null");
							}
							jsonObjectList.add(object);
						}else {
							for(int k=0;k<aspectList.size();k++) {
								Aspect ap = aspectList.get(k);
								int[] aspectOriIndexArr = ap.getOriIndexArr();
								JSONObject object = new JSONObject();
								object.put("opinion_index",i);
								object.put("opinion", transIntArrToList(oriIndexOPArr) );
								object.put("aspect",  transIntArrToList(aspectOriIndexArr) );
								//object.put("is_aspect_derived_pronoun",  ap.isImplicitAspect() );
								if( opt.isExplain ) {
									object.put("explain",ap.getReasonForSelection());
								}
								jsonObjectList.add(object);
							}
						}
					}
				}
			}
			rsForText = jsonObjectList.toString();
			outputTextList.add(rsForText);
		}
		String rsText = outputTextList.toString();
		FileIOs.writeStringToFile(outputFileName, rsText, false);
	}
	
	public void analysisParseResult() {
		int aspect_sum = 0;
		HashMap<String,Integer> aspectReason_count_map = new HashMap<String,Integer>();
		for(ParsedText rs:parseResultList) {
			if( !rs.isParsed() ) {
				continue;
			}
			for(ArrayList<Aspect> aspects_of_opinion : rs.getAspectListOfOpinion()) {
				if( aspects_of_opinion==null ) {
					continue;
				}
				for(Aspect aspect : aspects_of_opinion) {
					aspect_sum++;
					String reason = aspect.getReasonForSelection();
					int end_index = reason.indexOf("]");
					reason = reason.substring(0,end_index+1);
					if( !aspectReason_count_map.containsKey(reason) ) {
						aspectReason_count_map.put(reason, 0);
					}
					int count = aspectReason_count_map.get(reason);
					aspectReason_count_map.put(reason, ++count);
				} 
			}
		}
		System.out.println("Sum Aspect: " + aspect_sum);
		List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(aspectReason_count_map.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                return entry2.getValue().compareTo(entry1.getValue());
            }
        });
        for (Map.Entry<String, Integer> entry : entryList) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
	}
	
	private ArrayList<Integer> transIntArrToList(int[] arr) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i=0;i<arr.length;i++) {
			list.add(arr[i]);
		}
		return list;
	}
	
	private String transIntArrToString(int[] arr) {
		String rs = "[";
		for(int i=0;i<arr.length;i++) {
			rs += arr[i];
			if( i!=arr.length-1 ) {
				rs += ",";
			}
		}
		rs += "]"; 
		return rs;
	}
	
	private String transIntArrToString_WithoutBracket(int[] arr) {
		String rs = "";
		for(int i=0;i<arr.length;i++) {
			rs += arr[i];
			if( i!=arr.length-1 ) {
				rs += ",";
			}
		}
		return rs;
	}
}
