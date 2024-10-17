package extractor.textParser;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.json.simple.parser.ParseException;

import edu.stanford.nlp.io.EncodingPrintWriter.out;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CorefAnnotator;
import edu.stanford.nlp.pipeline.DependencyParseAnnotator;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import extractor.CustomPosAnnotator;

/**
 * Chinese text parser
 */
public class ChineseTextParser extends TextParser {
	
	public StanfordCoreNLP parser_for_preSetPOS;
	public StanfordCoreNLP spliter_for_preSetPOS;

	public CustomPosAnnotator customPosAnnor;
	public ParserAnnotator depAnnor;
	//public DependencyParseAnnotator deppAnnor;
	public NERCombinerAnnotator nerAnnor;
	public CorefAnnotator corefAnnor;
	
	@Override
	public void spliterInit() {
		if( !isSpliterInit ) {
			Properties props = new Properties();
			Properties props_for_preSetPOS = new Properties();
			try {
				props.load( this.getClass().getResourceAsStream("/StanfordCoreNLP-chinese.properties") );
				props_for_preSetPOS.load( this.getClass().getResourceAsStream("/StanfordCoreNLP-chinese.properties") );
			} catch (IOException e) {
				e.printStackTrace();
			}
		    props.setProperty("annotators","tokenize, ssplit");
		    spliter = new StanfordCoreNLP(props);
		    if( OPT.preTagFilePath.length()!=0 ) {
		    	props.setProperty("tokenize.whitespace", "true");
		    	spliter_for_preSetPOS = new StanfordCoreNLP(props);
		    }
		    isSpliterInit = true;
		}
	}
	@Override
	public void parserInit() {
		if( !isParserInit ) {
			Properties props = new Properties();
			Properties props_for_preSetPOS = new Properties();
			try {
				props.load( this.getClass().getResourceAsStream("/StanfordCoreNLP-chinese.properties") );
				props_for_preSetPOS.load( this.getClass().getResourceAsStream("/StanfordCoreNLP-chinese.properties") );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( OPT.isUseCoreference ) {
				props.setProperty("annotators","tokenize, ssplit, pos, lemma, parse, depparse, ner, coref");
			}
			else {
				props.setProperty("annotators","tokenize, ssplit, pos, lemma, parse, depparse, ner");
			}
			parser = new StanfordCoreNLP(props);
			//存在预设的分析结果
			if( OPT.preTagFilePath.length()!=0 ) {
				props_for_preSetPOS.setProperty("annotators","tokenize, ssplit, pos, lemma");
				props_for_preSetPOS.setProperty("tokenize.whitespace", "true");
	    		// 初始化 StanfordCoreNLP
				parser_for_preSetPOS = new StanfordCoreNLP(props_for_preSetPOS);
	            // 替换pos标注器为自定义的CustomPosAnnotator
	            try {
	            	customPosAnnor = new CustomPosAnnotator("pos", props_for_preSetPOS, OPT.preTagFilePath);
		            parser_for_preSetPOS.addAnnotator(customPosAnnor);
		            depAnnor = new ParserAnnotator("parse", props_for_preSetPOS);
//		            deppAnnor = new DependencyParseAnnotator(props);
		            nerAnnor = new NERCombinerAnnotator(props_for_preSetPOS);
		            corefAnnor = new CorefAnnotator(props_for_preSetPOS);
				} catch (IOException | ParseException e1) {
					e1.printStackTrace();
				}
	        }
		    isParserInit = true;
		    System.out.println("Initialize TextParser!");
		}
	}
	
	@Override
	public String[] getPartitionSymbol() {
		return new String[] {"PU"};
	}
	
	@Override
	public void parseAnnotation(Annotation document, String text) {
		if( OPT.preTagFilePath.length()!=0 ) {
			if( !customPosAnnor.getPredTextToPosMap().containsKey(text) ) {
				parser.annotate(document);
			}else {
				parser_for_preSetPOS.annotate(document);
				// 手动调用解析器，再次运行依赖解析和解析树生成
				parser_for_preSetPOS.addAnnotator(depAnnor);
		    	//parser.addAnnotator(deppAnnor); //此时不采用depparse
				parser_for_preSetPOS.addAnnotator(nerAnnor); // 命名实体识别 (NER)
				parser_for_preSetPOS.addAnnotator(corefAnnor);  // 共指消解                
				parser_for_preSetPOS.annotate(document);
			}
		}
		else {
	    	parser.annotate(document);
		}
	}
	
	@Override
	public void splitAnnotation(Annotation document, String text) {
		if( OPT.preTagFilePath.length()!=0 ) {
			if( !customPosAnnor.getPredTextToPosMap().containsKey(text) ) {
				spliter.annotate(document);
			}else {
				spliter_for_preSetPOS.annotate(document);
			}
		}
		else {
			spliter.annotate(document);
		}
	}
		
}
