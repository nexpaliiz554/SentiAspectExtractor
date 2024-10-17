package extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import extractor.textParser.TextParser;

public class ParsedText {
	private String inputText;
	private boolean isParsed;
	private String text;
	private ArrayList<IndexedWord> nodeList;
	private ArrayList<SemanticGraph> graphList;
	private ArrayList<Opinion> opinion_list;
	private ArrayList< ArrayList<Aspect> > aspectListOfOpinion;
	private TextParser textParser;
	public ParsedText(String inputText, boolean isParsed, String text, ArrayList<Opinion> opinion_list,ArrayList<ArrayList<Aspect>> aspectListOfOpinion) {
		super();
		this.inputText = inputText;
		this.isParsed = isParsed;
		this.text = text;
		this.opinion_list = opinion_list;
		this.aspectListOfOpinion = aspectListOfOpinion;
	}
	public ParsedText(String inputText, boolean isParsed) {
		this(inputText,isParsed,null,null,null);
	}
	public void setParseInfo(ArrayList<IndexedWord> nodeList, ArrayList<SemanticGraph> graphList) {
		this.nodeList = nodeList;
		this.graphList = graphList;
	}
	public String getInputText() {
		return inputText;
	}
	public boolean isParsed() {
		return isParsed;
	}
	public String getText() {
		return text;
	}
	public ArrayList<Opinion> getOpinion_list() {
		return opinion_list;
	}
	public ArrayList<ArrayList<Aspect>> getAspectListOfOpinion() {
		return aspectListOfOpinion;
	}
	public TextParser getTextParser() {
		return textParser;
	}
	public void setTextParser(TextParser textParser) {
		this.textParser = textParser;
	}
	public void setText(String text) {
		this.text = text;
	}
	public ArrayList<IndexedWord> getNodeList() {
		return nodeList;
	}
	public void setNodeList(ArrayList<IndexedWord> nodeList) {
		this.nodeList = nodeList;
	}
	public void setOpinion_list(ArrayList<Opinion> opinion_list) {
		this.opinion_list = opinion_list;
	}
	public void setAspectListOfOpinion(ArrayList<ArrayList<Aspect>> aspectListOfOpinion) {
		this.aspectListOfOpinion = aspectListOfOpinion;
	}
	public SemanticGraph getGraphByNode(IndexedWord node) {
		for(SemanticGraph graph: graphList) {
			if (graph.containsVertex(node)) {
				return graph;
			}
		}
		return null;
	}
}
