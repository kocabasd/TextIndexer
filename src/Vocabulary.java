import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;


public class Vocabulary {
	
	@SuppressWarnings({ "deprecation", "resource" })
	public static String Stemming(String input) throws IOException {
	    StringBuilder sb = new StringBuilder();
		Version matchVersion = Version.LUCENE_CURRENT;
		Analyzer analyzer = new StandardAnalyzer(matchVersion);
	    TokenStream stream = analyzer.tokenStream("field", new StringReader(input));
	    stream = new LowerCaseFilter(matchVersion, stream);
	    stream = new StopFilter(matchVersion, stream, StandardAnalyzer.STOP_WORDS_SET);
	    stream = new PorterStemFilter(stream);

	    // get the CharTermAttribute from the TokenStream
	    CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
	    try {
	      stream.reset();
	      while (stream.incrementToken()) {
	          if (sb.length() > 0) {
	              sb.append(" ");
	          }
	          sb.append(termAtt.toString());
	      }	      
	      stream.end();
	    }
	    finally {
	      stream.close();
	    }
	    return sb.toString();	    
	}
}
