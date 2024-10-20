package extractor.textParser;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import extractor.AnalysisOptions;

public abstract class TextParser {
	public AnalysisOptions OPT;
	public void setOption(AnalysisOptions opt) {
		this.OPT = opt;
	}
	public boolean isSpliterInit = false;
	public StanfordCoreNLP spliter;
	public abstract void spliterInit();
	public boolean isParserInit = false;
	public StanfordCoreNLP parser;
	public abstract void parserInit();
	
	private boolean isTextInit = false;
	private String text;
    private ArrayList<Integer> coreMapIndexs;
    private ArrayList<SemanticGraph> graphList;
    private ArrayList<Tree> treeList;
    private ArrayList<IndexedWord> nodeList;
    private HashMap<Integer,Integer> beginPosTokenIndexMap;
    private ArrayList<TextCorefChain> corefChainList;
    
    public abstract void parseAnnotation(Annotation document,String text);
    
    public abstract void splitAnnotation(Annotation document,String text);
    
    public void initText(String text){
    	this.text = text;
    	coreMapIndexs = new ArrayList<Integer>();
    	graphList = new ArrayList<SemanticGraph>();
    	treeList = new ArrayList<Tree>();
    	nodeList = new ArrayList<IndexedWord>();
    	beginPosTokenIndexMap = new HashMap<Integer,Integer>();
    	Annotation document = new Annotation(text);
    	parseAnnotation(document,text);
	    List<CoreMap> sentences = (List<CoreMap>)document.get(CoreAnnotations.SentencesAnnotation.class);
	    for(CoreMap sentence: sentences) {
	    	SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
	    	graphList.add( graph );
	    	Tree tree = sentence.get(TreeAnnotation.class);
	    	treeList.add( tree ); 
	    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	    		coreMapIndexs.add(graphList.size()-1);
	    		int beginPosition = token.beginPosition();
	    		beginPosTokenIndexMap.put(beginPosition, nodeList.size());
	    		IndexedWord node = graph.getNodeByIndexSafe(token.index());
	    		if( node!=null) {
	    			node.setNER( token.ner() );
		    		nodeList.add( node );
	    		}
	    	}
        }
	    //开始处理指代消解：
	    corefChainList = new ArrayList<TextCorefChain>();
	    Map<Integer, CorefChain> corefChainMap = document.get(CorefChainAnnotation.class);
	    if( corefChainMap!=null && corefChainMap.size()>0 ) {
	    	// 一个 entry 为一条指代
	   	 	for(Map.Entry<Integer, CorefChain> entry:corefChainMap.entrySet() ) {
	   	 		TextCorefChain corefChain = new TextCorefChain();
	   		    for (CorefChain.CorefMention m:entry.getValue().getMentionsInTextualOrder()) {
	   		    	List<CoreLabel> tokens = sentences.get (m.sentNum-1).get (CoreAnnotations.TokensAnnotation.class);
	   		    	int tokenStartIndex = m.startIndex-1;
	   		    	int tokenEndIndex = m.endIndex-2;
	   		    	ArrayList<CoreLabel> coref = new ArrayList<CoreLabel>();
	   		    	for(int i=tokenStartIndex;i<=tokenEndIndex;i++) {
	   		    		CoreLabel token = tokens.get(i);
						coref.add(token);
					}
					corefChain.addCoref(coref);
	   		    }
	   		    //链条上只有一个节点，则没有意义
	   		    if( corefChain.getChainSize()>1 ) {
	   		    	corefChainList.add(corefChain);
	   		    }
	   	 	}
	    }
   	 	isTextInit = true;
    }
    
    public String getText() {
		return text;
	}

    public boolean isTextInit() {
		return isTextInit;
	}
    
    public boolean isIndexLegalForNodeList(int index) {
    	if( nodeList==null ) {
    		return false;
    	}
    	if( index>=0 && index<nodeList.size() ) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    public ArrayList<IndexedWord> getNodeList() {
    	return nodeList;
	}
    
    public IndexedWord getNodeByIndex(int index) {
    	if( isIndexLegalForNodeList(index) ) {
    		return nodeList.get(index);
    	}else {
    		return null;
    	}
	}
    
    public SemanticGraph getGraphByNode(IndexedWord node) {
    	int beginPosition = node.beginPosition();
    	int nodeIndex = beginPosTokenIndexMap.get(beginPosition);
    	int depIndex = coreMapIndexs.get(nodeIndex);
    	return graphList.get(depIndex);
    }
    
    public Tree getTreeByNode(IndexedWord node) {
    	int beginPosition = node.beginPosition();
    	int nodeIndex = beginPosTokenIndexMap.get(beginPosition);
    	int depIndex = coreMapIndexs.get(nodeIndex);
    	return treeList.get(depIndex);
    }
    
    public int getTreeNodeIndex(Tree treeNode) {
    	String treeNodeLabel = treeNode.label().toString();
    	return getTreeNodeIndex(treeNodeLabel);
	}
    
    public int getTreeNodeIndex(String nodeLabel) {
		int numStartIndex = nodeLabel.lastIndexOf("-")+1;
		int numEndIndex = nodeLabel.length();
		String indexString = nodeLabel.substring(numStartIndex, numEndIndex);
		int index = Integer.parseInt( indexString );
		return index;
	}
    
    public Tree getTreeLeafByNode(IndexedWord node) {
    	Tree tree = getTreeByNode(node);
		List<Tree> leafList = tree.getLeaves();
		for(Tree leaf:leafList) {
			int nodeIndex = getTreeNodeIndex(leaf) ;
			if( nodeIndex==node.index() ) {
				return leaf;
			}
		}
		return null;
	}
    
    public int getNodeIndexByBeginPos(int beginPos) {
    	if( beginPosTokenIndexMap.containsKey(beginPos) ) {
    		return beginPosTokenIndexMap.get(beginPos);
    	}else {
    		return -1;
    	}
    }
    
    public int getNodeIndexByNode(IndexedWord node) {
    	if( beginPosTokenIndexMap.containsKey(node.beginPosition()) ) {
    		return beginPosTokenIndexMap.get(node.beginPosition());
    	}else {
    		return -1;
    	}
    }
    
    //一个节点可以被指代消解
    public boolean hasCoReference(IndexedWord node) {
    	for(TextCorefChain chain:corefChainList) {
    		if( chain.isNodeInThisChain(node) ) {
    			return true;
    		}
    	}
    	return false;
    }
    
    //拿到一个节点的指代对象列表
    public ArrayList< ArrayList<CoreLabel> > getCoReferList(IndexedWord node){
    	for(TextCorefChain chain:corefChainList) {
    		ArrayList< ArrayList<CoreLabel> > corefList = chain.getCorefList(node);
    		if( corefList!=null ) {
    			return corefList;
    		}
    	}
    	return null;
    }
    
    public abstract String[] getPartitionSymbol();
   	public boolean isPartitionSymbolByIndex(int index) {
   		String[] partitionSymbol = getPartitionSymbol();
   		String tag = nodeList.get(index).tag();
   		for(int i=0;i<partitionSymbol.length;i++) {
			if( tag.equals(partitionSymbol[i]) ) {
			   return true;
		    }
		}
   		return false;
   	}
   	
   	public boolean havePartitionInBetween(IndexedWord node1,IndexedWord node2) {
   		int nodeIndex1 = getNodeIndexByNode(node1);
   		int nodeIndex2 = getNodeIndexByNode(node2);
   		int start = Math.min(nodeIndex1, nodeIndex2);
   		int end = Math.max(nodeIndex1, nodeIndex2);
   		for(int i=start+1;i<end;i++) {
   			if( isPartitionSymbolByIndex(i) ) {
   				return true;
   			}
   		}
   		return false;
   	}

	public HashMap<Integer, Integer> getBeginPosTokenIndexMap() {
		return beginPosTokenIndexMap;
	}

	public void setBeginPosTokenIndexMap(HashMap<Integer, Integer> beginPosTokenIndexMap) {
		this.beginPosTokenIndexMap = beginPosTokenIndexMap;
	}

	public ArrayList<SemanticGraph> getGraphList() {
		return graphList;
	}

	public void setGraphList(ArrayList<SemanticGraph> graphList) {
		this.graphList = graphList;
	}

}
