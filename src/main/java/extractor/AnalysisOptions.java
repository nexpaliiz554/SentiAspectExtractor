package extractor;

//Set various parameters during the analysis process
public class AnalysisOptions {
	public String dictPath;
	public String en_dictPath = System.getProperty("user.dir")+"/src/main/resources/dictionary/";
	public String ch_dictPath = System.getProperty("user.dir")+"/src/main/resources/dictionary_ch/";
	public String peopleWordListFileName;
	public String peopleWordListPath;
	public String timeWordListFileName;
	public String timeWordListPath;
	public String vagueWordListFileName;
	public String vagueWordListPath;
	public String verbAspectWordListFileName;
	public String verbAspectWordListPath;
	public String implicitAspectWordListFileName;
	public String implicitAspectWordListPath;
	public String preTagFilePath = "";
	
	
	public boolean isTextPreprocessing = true; //是否进行文本预处理
	public boolean isExplain = false;  //是否解释提取aspect的理由
	public boolean isUseCoreference = true;  //是否使用指代消解
	public boolean isSuppByLocaDistribution = true;  //是否以词和词之间的相对位置辅助分析
	public String language = "EN";//处理的语言 EN:english;  CH:chinese
	public boolean isHandleSPA = true;//是否处理SPA, 即将输入的opinion下标直接当作aspect处理；
	public boolean isAnalysisParseResult = false; //是否分析解析结果
	
	public boolean isUseSubjectPredicativeRule = true; //是否使用主语结构方面提取规则
	public boolean isUseModifierRule = true; //是否使用修饰关系方面提取规则
	public boolean isUseObjectRule = true; //是否使用宾语关系方面提取规则
	
	public boolean isConsiderVerbAspect = false; //是否考虑动词aspect
	
	/*如何扩展核心aspect节点
	 * 0:仅利用 compound 进行扩展；
	 * 1:利用 compound + nmod进行扩展；
	 * 2:利用选区分析进行扩展；(aspect范围最广)
	 * 默认为: 0；
	 * */
	public int coreExtendType = 0;  //是否以词和词之间的相对位置辅助分析
	
	/*以何种格式输出aspect：
	 * 0:原始输出格式，针对opinion，以列表形式输出；e.g. [4,5]:[0,1] , [5,6] ;
	 * 1:json输出格式，以opinion aspect 列表形式输出；e.g. {"opinion": [1,2], "aspect": [3,4]}
	 * 默认为: 1
	 * */
	public int outputFormat = 1;
	
	/*如何处理 illegal opinion / implicit opinion：
	 * 0: 输出“cannot_deal”
	 * 1: 只输出[-1,-1]作为aspect
	 * 2: 不输出该opinion
	 * 默认为: 1
	 * */
	public int implicitOpinionDealType = 1;
	
	public boolean isEN() {
		return language.toLowerCase().equals("en");
	}
	
	public boolean isCH() {
		return language.toLowerCase().equals("ch");
	}
	
	public void setUpAnalysisParseResult() {
		this.isAnalysisParseResult = true;
		this.isExplain = true;
	}
	
	public AnalysisOptions() {
		dictPath = en_dictPath;
		peopleWordListFileName = "peopleWordList.txt";
		timeWordListFileName = "timeWordList.txt";
		vagueWordListFileName = "vagueWordList.txt";
		verbAspectWordListFileName = "verbAspectList.txt";
		implicitAspectWordListFileName = "implicitAspectWordList.txt";
	}
	
	public void updatePath() {
		peopleWordListPath = dictPath + peopleWordListFileName;
		timeWordListPath = dictPath + timeWordListFileName;
		vagueWordListPath = dictPath + vagueWordListFileName;
		verbAspectWordListPath = dictPath + verbAspectWordListFileName;
		implicitAspectWordListPath = dictPath + implicitAspectWordListFileName;
	}
	

}
