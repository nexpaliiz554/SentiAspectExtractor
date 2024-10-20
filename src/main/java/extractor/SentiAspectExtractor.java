package extractor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import extractor.textExtractor.ChineseTextExtractor;
import extractor.textExtractor.EnglishTextExtractor;
import extractor.textExtractor.TextExtractor;

//The main class of this project
public class SentiAspectExtractor {
	public AnalysisOptions opt;
	private String inputFileName = "";
	private String outputFileName = "";
	private String sResultsFileExtension = "_out.txt";
	public static void main(String[] args) throws IOException {
		SentiAspectExtractor extractor = new SentiAspectExtractor();
		extractor.initialiseAndRun(args);
	}
	
	public void initialiseAndRun(String[] args) {
		opt = new AnalysisOptions();
		boolean[] argumentRecognised = new boolean[args.length];
		for(int i = 0; i < args.length; i++ ) {
			if ( args[i].equalsIgnoreCase("-help") ) {
				help();
				argumentRecognised[i] = true;
	        }
			else if ( args[i].equalsIgnoreCase("-inputfile") ) {
				this.inputFileName = args[i + 1];
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
	        }
			else if( args[i].equalsIgnoreCase("-outputfile") ) {
				this.outputFileName = args[i + 1];
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
			}
			else if( args[i].equalsIgnoreCase("-dict") ) {
				opt.dictPath = args[i + 1];
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
			}
			else if( args[i].equalsIgnoreCase("-explain") ) {
				opt.isExplain = true;
				argumentRecognised[i] = true;
			}
			else if( args[i].equalsIgnoreCase("-coreextendtype") ) {
				opt.coreExtendType = Integer.parseInt(args[i + 1]);
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
			}
			else if( args[i].equalsIgnoreCase("-outputformat") ) {
				opt.outputFormat = Integer.parseInt(args[i + 1]);
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
			}
			else if( args[i].equalsIgnoreCase("-implicitopiniondealtype") ) {
				opt.implicitOpinionDealType = Integer.parseInt(args[i + 1]);
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
			}
			else if( args[i].equalsIgnoreCase("-istextpreprocessing") ) {
				opt.isTextPreprocessing = Boolean.parseBoolean(args[i + 1]);
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
			}
			else if( args[i].equalsIgnoreCase("-lang") ) {
				opt.language = args[i + 1];
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
			}
			else if( args[i].equalsIgnoreCase("-handlespa") ) {
				opt.isHandleSPA = Boolean.parseBoolean(args[i + 1]);
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
			}
			else if( args[i].equalsIgnoreCase("-analysisparseresult") ) {
				opt.setUpAnalysisParseResult();
				argumentRecognised[i] = true;
			}
			else if( args[i].equalsIgnoreCase("-usesubjectpredicativerule") ) {
				opt.isUseSubjectPredicativeRule = Boolean.parseBoolean(args[i + 1]);
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
			}
			else if( args[i].equalsIgnoreCase("-usemodifierrule") ) {
				opt.isUseModifierRule = Boolean.parseBoolean(args[i + 1]);
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
			}
			else if( args[i].equalsIgnoreCase("-useobjectrule") ) {
				opt.isUseObjectRule = Boolean.parseBoolean(args[i + 1]);
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
			}
			else if( args[i].equalsIgnoreCase("-isconsiderverbaspect") ) {
				opt.isUseObjectRule = Boolean.parseBoolean(args[i + 1]);
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
			}
			else if( args[i].equalsIgnoreCase("-setloadpretag") ) {
				opt.preTagFilePath = args[i + 1];
				argumentRecognised[i] = true;
				argumentRecognised[i + 1] = true;
			}
	    }
		for(int i = 0; i < args.length; i++) {
			if ( !argumentRecognised[i] ) {
				System.out.println("Unrecognised command - wrong spelling or case?: " + args[i]);
	            return;
	        }
	    }
		
		if( !FileIOs.isFileExists(this.inputFileName) ) {
			System.out.println("Input file is not set or does not exist!: " + this.inputFileName);
			return;
		}
		
		if( this.outputFileName==null || this.outputFileName.length()==0 ) {
			this.outputFileName = FileIOs.getNextAvailableFilename(FileIOs.s_ChopFileNameExtension(this.inputFileName), sResultsFileExtension);
		}
		
		//开始解析aspect
		init();
		extractSentiAspect ();
		
	}
	
	public void help() {
		System.out.println("Introduction:");
		System.out.println("    SentiAspectExtractor is an independent project that can extract aspects from given text and" + "\n"
				         + "    a list of text opinions using several syntax-based rules. It mainly consists of five steps: " + "\n"
				         + "    1) preprocessing text, " + "\n"
				         + "    2) selecting representative nodes for input opinion expressions (for multi-word opinion)," + "\n"
				         + "    3) extracting aspect for representative opinion node based on a set of syntactic rules, " + "\n"
				         + "    4) extending aspect, " + "\n"
				         + "    5) trimming aspects.");
		System.out.println();
		System.out.println("Here we will explain some parameters:");
		System.out.println("    -inputFile\tSet the input file address.");
		System.out.println("    -outputFile\tSet the output file path. If not set, the analysis result will be output in the same directory as the input file.");
		System.out.println("    -dict\tSet the address of the dictionary to be used for analysis.");
		System.out.println("    -explain\tSet whether to output the reason for extracting the aspect when outputting the result.");
		
		System.out.println("    -coreExtendType\tSet the expansion method for noun aspects.");
		String placeHolder = String.format("%" + "    -coreextendtype".length() + "s", "") ;
		System.out.println( placeHolder + "\t" + "0 corresponds to AER-3.1, 1 corresponds to AER-3.2, 2 corresponds to AER-3.3.\n"
		                  + placeHolder + "\t" + "The default parameter is 0." );
		
		System.out.println("    -outputFormat\tSet the output format of the result");
		placeHolder = String.format("%" + "    -outputformat".length() + "s", "") ;
		System.out.println( placeHolder + "\t" + "0 represents the text-based output format, such as '[4,5]:[0,1] , [5,6] ;'");
		System.out.println( placeHolder + "\t" + "1 represents the JSON output format, such as '{\"opinion\": [1,2], \"aspect\": [3,4]}'");
		System.out.println( placeHolder + "\t" + "The default parameter is 1.");
		
		System.out.println( "    -implicitOpinionDealType\tSet the handling method for illegal or implicit opinions.");
		placeHolder = String.format("%" + "    -implicitOpinionDealType".length() + "s", "") ;
		System.out.println( placeHolder + "\t" + "0: Output 'cannot_deal'");
		System.out.println( placeHolder + "\t" + "1: Output [-1, -1] as an aspect");
		System.out.println( placeHolder + "\t" + "2: Do not output this opinion");
		System.out.println( placeHolder + "\t" + "The default parameter is 1.");
		
		System.out.println( "    -isTextPreprocessing\tSet whether to perform text preprocessing. The default parameter is true.");
		
		System.out.println( "    -Lang\tSet which language to handle.");
		placeHolder = String.format("%" + "    -Lang".length() + "s", "") ;
		System.out.println( placeHolder + "\t" + "en: English");
		System.out.println( placeHolder + "\t" + "ch: Chinese");
		System.out.println( placeHolder + "\t" + "The default parameter is 'en'.");
		
		System.out.println( "    -handleSPA\tWhether to handle cases with SHAP potential aspects(SPA).");
		placeHolder = String.format("%" + "    -handleSPA".length() + "s", "") ;
		System.out.println( placeHolder + "\t" + "When set to handle SPA (SHAP potential aspect), it allows input opinion indices to be marked " + "\n"
				          + placeHolder + "\t" + "with the '(SPA)' to indicate that the opinion may potentially be an aspect. When SPA is inputted " + "\n"
				          + placeHolder + "\t" + "into SentiAspectExtractor, it directly starts from Step-4 Extending Aspects, instead of being " + "\n"
				          + placeHolder + "\t" + "processed like regular opinions." );
		System.out.println( placeHolder + "\t" + "true: Handle SPA");
		System.out.println( placeHolder + "\t" + "false: Not handle SPA");
		System.out.println( placeHolder + "\t" + "The default parameter is 'true'.");
		
		System.out.println( "    -analysisParseResult\tAnalyze the rule distribution of all extracted aspects.");
		
		System.out.println( "    -usesubjectpredicativerule\tWhether to use Subject Structure Based Aspect Extraction Pattern. (Default is 'true')");
		System.out.println( "    -usemodifierrule\tWhether to use Modifying Relationship Based Aspect Extraction Pattern. (Default is 'true')");
		System.out.println( "    -useobjectrule\tWhether to use Opinion Object Based Aspect Extraction Pattern. (Default is 'true')");
		
		System.out.println( "    -setLoadPreTag\tSentiAspectExtractor will read the POS analysis results for 4o from the path specified by this parameter and analysis based on it.");
	}
	
	public TextExtractor textExtractor;
	
	public void init() {
		if ( opt.isEN() ) {
			textExtractor = new EnglishTextExtractor();
			opt.dictPath = (opt.dictPath == null) ? opt.en_dictPath : opt.dictPath;
		}else if( opt.isCH() ) {
			textExtractor = new ChineseTextExtractor();
			opt.dictPath = (opt.dictPath == null || opt.dictPath.equals(opt.en_dictPath)) ? opt.ch_dictPath : opt.dictPath;
		}
		opt.updatePath();
		textExtractor.setOption(opt);
		textExtractor.init();
	} 
	
	public void extractSentiAspect() {
		System.out.println(LocalDateTime.now()+" Aspect analysis starts!");
		List<String> inputTextList = FileIOs.readFileGetStringList(this.inputFileName);
		ParsedData pd = new ParsedData(opt,textExtractor.getNLPRule());
		for(int i=0;i<inputTextList.size();i++) {
			String inputText = inputTextList.get(i);
			String[] elems = inputText.split("\t");
			boolean isParsed = elems.length>=2;
			ParsedText parsedText = new ParsedText(inputText,isParsed);
			if( !isParsed ) {
				pd.addParsedText( parsedText );
				continue;
			}
			String text = elems[0];
			String opListString = elems[1];
			ArrayList<Opinion> opinion_list = getOpinionListFromString(opListString);
			//Process the text to be analyzed
			parsedText.setText(text);
			textExtractor.parseText(parsedText);
			//Store the aspect list corresponding to each opinion:
			ArrayList< ArrayList<Aspect> > aspectListOfOpinion = new ArrayList< ArrayList<Aspect> >();
			for(int j=0;j<opinion_list.size();j++) {
				Opinion op = opinion_list.get(j);
				// implicit opinion 
				if( !op.isLegalOriIndexArr() ) {
					aspectListOfOpinion.add(null);
				}
				//explicit opinion:
				else {
					ArrayList<Aspect> aspectList = textExtractor.extractForOpinion(op);
					aspectListOfOpinion.add(aspectList);
				}
			}
			//Store parsing results
			parsedText.setOpinion_list(opinion_list);
			parsedText.setAspectListOfOpinion(aspectListOfOpinion);
			pd.addParsedText( parsedText );
		}
		
		//Use frequently aspects from the dataset as a supplement for texts where no explicit aspect has been extracted.
		pd.addDefaultAspectAccordingFrequency();
		
		if( opt.outputFormat==0 ) {
			pd.formatOutput_Zero(this.outputFileName);
		}
		else if( opt.outputFormat==1 ) {
			pd.formatOutput_One(this.outputFileName);
		}
		
		if( opt.isAnalysisParseResult ) {
			pd.analysisParseResult();
		}
		
		System.out.println(LocalDateTime.now()+" Aspect analysis is over!");
		System.out.println("Output in:" + this.outputFileName);
	}
	
	private ArrayList<Opinion> getOpinionListFromString(String OPListString){
    	ArrayList<Opinion> opinion_list = new ArrayList<Opinion>();
		String[] elems = OPListString.split(";");
		for(String elem:elems) {
			boolean is_potential_aspect = elem.indexOf("(SPA)")!=-1;
			elem = elem.replace("(SPA)","");
			if( !opt.isHandleSPA ) {
				is_potential_aspect = false;
			}
			int[] indexArr = getIndexArr(elem);
			if( indexArr==null ) {
				continue;
			}
			Opinion opinion = new Opinion();
			opinion.setOriIndexArr(indexArr);
			opinion.setIsPotentialAspect(is_potential_aspect);
			opinion_list.add(opinion);
		}
		return opinion_list;
	}
	
	private int[] getIndexArr(String indexString) {
		indexString = indexString.trim();
		if( indexString.length()==0 ) {
			return null;
		}
		String[] indexStringArr = indexString.split(",");
		int startIndex = Integer.parseInt(indexStringArr[0]);
		int endIndex = Integer.parseInt(indexStringArr[1]);
		int[] indexArr = new int[2];
		indexArr[0] = startIndex;
		indexArr[1] = endIndex;
		return indexArr;
	}

}


