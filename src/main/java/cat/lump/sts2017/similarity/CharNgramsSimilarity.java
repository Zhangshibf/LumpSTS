package cat.lump.sts2017.similarity;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.ibm.icu.text.Transliterator;

import cat.lump.aq.basics.check.CHK;
import cat.lump.aq.basics.log.LumpLogger;
import cat.lump.ie.textprocessing.Decomposition;
import cat.lump.ie.textprocessing.ngram.CharacterNgrams;
import cat.lump.sts2017.dataset.FeatureDumper;
import cat.lump.sts2017.dataset.StsBufferedReader;
import cat.lump.sts2017.dataset.StsInstance;
import edu.stanford.nlp.international.arabic.Buckwalter;

public class CharNgramsSimilarity {
  
  private static final int FEATURE_NUMBER = 2;
  private static final String FEATURE_NAME = "grm";

  private static final int N_DEFAULT = 3;
  
  private final int N;
  
  private final File FILE;
  private final Locale LAN1;
  private final Locale LAN2;
  private final boolean SINGLE_LANGUAGE;
  private final boolean ONE_IS_ARABIC;
  
  private final Transliterator NORMALIZER = 
      Transliterator.getInstance("NFD; [:NonspacingMark:] Remove; NFC");
  
  private final Buckwalter BUCKWALTER = new Buckwalter(true);
    
  private static LumpLogger logger = 
      new LumpLogger(CharNgramsSimilarity.class.getSimpleName());
  
  public CharNgramsSimilarity(String input, int n, Locale lan) {
    this(input, n, lan, lan);
  }
  
  public CharNgramsSimilarity(String input, int n, Locale lan1, Locale lan2) {
    FILE = setFile(input);
    N = getN(n);
    LAN1 = lan1;
    LAN2 = lan2;
    if (LAN2.equals(LAN1)) {
      SINGLE_LANGUAGE = true;
      ONE_IS_ARABIC = false;
    } else {
      SINGLE_LANGUAGE = false;
      ONE_IS_ARABIC = (LAN1.getLanguage().equals("ar") || LAN2.getLanguage().equals("ar") )
          ? true
          : false;
    }
  }
  
  public double computeSimilarity(String str1, String str2) {
//    System.out.println(str2);
    if (ONE_IS_ARABIC) {
      if (LAN1.equals("ar")) {
        //TODO I THINK THIS IS REDUNDANT WRT normalize()
        str1 = BUCKWALTER.apply(str1);
      } else {
        str2 = BUCKWALTER.apply(str2);
      }
      str1 = removeVowels(str1);
      str2 = removeVowels(str2);
    }
    str1 = normalize(str1, LAN1);
    str2 = normalize(str2, LAN2);
    
//    System.out.println(str1);
//    System.out.println("k: " +str2);
    
    Decomposition cNgrams = new CharacterNgrams(N, true);
    Map<String, Integer> ngrams1 = getFreqs(cNgrams.getStrings(str1));
    Map<String, Integer> ngrams2 = getFreqs(cNgrams.getStrings(str2));
    double sim = 
        dotproduct(ngrams1, ngrams2) / 
        (magnitude(ngrams1) * magnitude(ngrams2));
    
    return sim;
  }
  
  public Map<String, Integer> getFreqs(List<String> texts) {
    Map<String, Integer> ngr = new LinkedHashMap<String, Integer>();
    for (String n : texts) {
      if (! ngr.containsKey(n)) {
        ngr.put(n, 0);
      }
      ngr.put(n, ngr.get(n)+1);
    }
    return ngr;
  }

  private double dotproduct(Map<String, Integer> grams1, Map<String, Integer> grams2) {
    double d = 0;
    Set<String> common = new HashSet<String>();
    common.addAll(grams1.keySet());
    common.retainAll(grams2.keySet());
    for (String s : common) {
      d += grams1.get(s) * grams2.get(s);
    }    
    return d;
  }
  
  private double magnitude(Map<String, Integer> grams) {
    double m = 0;
    for (Integer value : grams.values()) {
      m += value * value;
    }
    return Math.sqrt(m);
  }
  
  private int getN(int n) {
    CHK.CHECK(n > 0 && n <= 5, "I expect a value in (1,5] for n");
    return n;
  }
  
  private String removeVowels(String str) {
    return str.replaceAll("[aeiouAEIOU]", "");
  }
  
  /**
   * Removes diacritics; converts to lower case. If Arabic, applies
   * Buckwalter transliteration
   * @param str
   * @param lan
   * @return
   */
  private String normalize(String str, Locale lan) {
    if (lan.toString().equals("ar")) {
    return BUCKWALTER.apply(str).toLowerCase().replaceAll("\\s", "");
    } else {
      return NORMALIZER.transliterate(str).toLowerCase().replaceAll("\\s", "");
    }
  }
  
  private File setFile (String input) {
    File f = new File(input);
    CHK.CHECK(f.exists() && f.isFile() && f.canRead(), 
        String.format("The file %s does not exist or I cannot read it",
            f));
    return f;
  }
  
  
  
  protected static CommandLine loadOptions(String[] args){
    Options options = new Options();    
    
    options.addOption("f", "input", true, 
        "Input file");
    options.addOption("o", "output", true, 
        "Output File");
    options.addOption("l", "lan1", true, 
        "language (en, es, ar)");
    options.addOption("m", "lan2", true,
        "second language (en, es, ar; optional)");
    options.addOption("n", "ngram", true,
        String.format("[1,5] (default: %d)", N_DEFAULT));
    
    HelpFormatter formatter = new HelpFormatter();
    int widthFormatter = 88;
    CommandLineParser parser = new BasicParser();
    CommandLine cLine = null;

    try {     
        cLine = parser.parse( options, args );
    } catch( ParseException exp ) {
      logger.error( "Unexpected exception: " + exp.getMessage() );      
    } 
    
    if(!cLine.hasOption("f") || !cLine.hasOption("o") || !cLine.hasOption("l")) {
      logger.warn("Please, provide input/output file and at least one language");
    formatter.printHelp("x", options);
//    widthFormatter, command, header, options, footer, true);
      System.exit(1);
    }
    
    return cLine; 
  }
  
  
  public static void main(String[] args) throws IOException {
    CommandLine cLine = loadOptions(args);
    String inFile = cLine.getOptionValue("f");
    String ouFile = cLine.getOptionValue("o");
    String lan1 = cLine.getOptionValue("l");
    
    int n = (cLine.hasOption("n")) 
        ? Integer.valueOf(cLine.getOptionValue("n"))
        : N_DEFAULT;
    
    
    String lan2;
    CharNgramsSimilarity cns;

    if (cLine.hasOption("m")) {
      lan2 = cLine.getOptionValue("m");
      cns = new CharNgramsSimilarity(inFile, n, new Locale(lan1), new Locale(lan2));
    } else {
      cns = new CharNgramsSimilarity(inFile, n, new Locale(lan1));
    }
    FeatureDumper fd = new FeatureDumper(ouFile, FEATURE_NUMBER, n + FEATURE_NAME);
    StsBufferedReader cr = new StsBufferedReader(inFile);

    double sim;
    for (StsInstance instance = cr.readInstance(); instance != null; instance = cr.readInstance()) {
      sim = cns.computeSimilarity(instance.getText1(), instance.getText2());
//      System.out.println(sim);
      fd.writeLine(String.valueOf(sim));
       
    }
    fd.close();
  }
  
}
