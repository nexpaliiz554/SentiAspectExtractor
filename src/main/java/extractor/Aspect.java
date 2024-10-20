package extractor;

import java.util.ArrayList;
import java.util.stream.Collectors;

import edu.stanford.nlp.ling.IndexedWord;

public class Aspect {
	private int[] oriIndexArr;
	private boolean isImplicitAspect;
	private String reasonForSelection;
	private IndexedWord coreAspectNode;
	private int aspectType = -1;
	private ArrayList<IndexedWord> aspectNodeList;
	private ArrayList<IndexedWord> aspectNounPhrase;
	private int[] tokenIndexArr;
	private int rightMostBoundary = -1;
	private int leftMostBoundary = -1;
	public Aspect() {
		this(null,null,-1);
	}
	
	public Aspect(IndexedWord coreAspectNode) {
		this(coreAspectNode,"",-1);
	}
	
	public Aspect(IndexedWord coreAspectNode,String reasonForSelection) {
		this(coreAspectNode,reasonForSelection,-1);
	}
	
	public Aspect(IndexedWord coreAspectNode,String reasonForSelection,int aspectType) {
		super();
		this.coreAspectNode = coreAspectNode;
		this.reasonForSelection = reasonForSelection;
		this.aspectType = aspectType;
		this.aspectNodeList = new ArrayList<IndexedWord>();
		if( coreAspectNode!=null ) {
			this.aspectNodeList.add(coreAspectNode);
		}
	}
	
	public Aspect copyAspect() {
		Aspect copyAs = new Aspect();
		copyAs.setReasonForSelection(this.reasonForSelection);
		copyAs.setCoreAspectNode(this.coreAspectNode);
		copyAs.setAspectNodeList(this.aspectNodeList);
		return copyAs;
	}
	
	public int getRightMostBoundary() {
		return rightMostBoundary;
	}

	public void setRightMostBoundary(int rightMostBoundary) {
		this.rightMostBoundary = rightMostBoundary;
	}
	
	public int getLeftMostBoundary() {
		return leftMostBoundary;
	}

	public void setLeftMostBoundary(int leftMostBoundary) {
		this.leftMostBoundary = leftMostBoundary;
	}
	
	public int[] getOriIndexArr() {
		return oriIndexArr;
	}

	public void setOriIndexArr(int[] oriIndexArr) {
		this.oriIndexArr = oriIndexArr;
	}
	
	public int[] getTokenIndexArr() {
		return tokenIndexArr;
	}

	public void setTokenIndexArr(int[] tokenIndexArr) {
		this.tokenIndexArr = tokenIndexArr;
	}
	
	public String getReasonForSelection() {
		return reasonForSelection;
	}

	public void setReasonForSelection(String reasonForSelection) {
		this.reasonForSelection = reasonForSelection;
	}
	
	public boolean isImplicitAspect(){
		return this.isImplicitAspect;
	}
	
	public void setImplicitAspect(boolean isImplicitAspect) {
		this.isImplicitAspect = isImplicitAspect;
	}
	
	public int getAspectType() {
		return aspectType;
	}

	public void setAspectType(int aspectType) {
		this.aspectType = aspectType;
	}

	public IndexedWord getCoreAspectNode() {
		return coreAspectNode;
	}

	public void setCoreAspectNode(IndexedWord coreAspectNode) {
		this.coreAspectNode = coreAspectNode;
	}
	
	public String getCoreAspectNodeTag() {
		if( coreAspectNode==null || coreAspectNode.tag()==null ) {
			return "";
		}
		return coreAspectNode.tag();
	}

	public ArrayList<IndexedWord> getAspectNodeList() {
		return aspectNodeList;
	}

	public void setAspectNodeList(ArrayList<IndexedWord> aspectNodeList) {
		this.aspectNodeList = aspectNodeList;
	}
	
	public int getAspectNodeListSize() {
		if( aspectNodeList==null ) {
			return 0;
		}
		return aspectNodeList.size();
	}
	
	public String getAspectListString() {
		return aspectNodeList.stream()
                .map(IndexedWord::lemma)  
                .collect(Collectors.joining(" "));  
	}
	
	public String getAspectListAndSelectReason() {
		String aspectListString = getAspectListString();
		
		return aspectListString+"  "+reasonForSelection;
	}
	
	public boolean isNodeInAspect(IndexedWord node) {
    	for(int i=0;i<aspectNodeList.size();i++) {
    		if( aspectNodeList.get(i).beginPosition()==node.beginPosition() ) {
    			return true;
    		}
    	}
    	return false;
    }
	
	public boolean isSubset(Aspect as2) {
		if( isImplicitAspect && as2.isImplicitAspect) {
			return true;
		}
		boolean hasNodeNotInAs2 = false;
		for(int i=0;i<aspectNodeList.size();i++) {
			IndexedWord myAspectNode = aspectNodeList.get(i);
			if( !as2.isNodeInAspect(myAspectNode) ) {
				hasNodeNotInAs2 = true;
				break;
			}
		}
		return !hasNodeNotInAs2;
	}
	
	public int[] getAspectBeginPosScopeArr() {
		int[] aspectScopeArr = new int[2];
		int startNodeBeginPos = -1;
		int endNodeBeginPos = -1;
		if( !isImplicitAspect ) {
			IndexedWord firstNode = aspectNodeList.get(0);
			IndexedWord lastNode = aspectNodeList.get(aspectNodeList.size()-1);
			startNodeBeginPos = firstNode.beginPosition();
			endNodeBeginPos = lastNode.beginPosition();
		}
		aspectScopeArr[0] = startNodeBeginPos;
		aspectScopeArr[1] = endNodeBeginPos;
		return aspectScopeArr;
	}	
	
	public ArrayList<IndexedWord> getAspectNounPhrase() {
		return aspectNounPhrase;
	}

	public void setAspectNounPhrase(ArrayList<IndexedWord> aspectNounPhrase) {
		this.aspectNounPhrase = aspectNounPhrase;
	}
	
}
