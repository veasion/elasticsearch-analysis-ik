import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.plugin.analysis.ik.AnalysisIkPlugin;
import org.wltea.analyzer.dic.Dictionary;

import java.io.File;

/**
 * Test
 *
 * @author luozhuowei
 * @date 2023/12/27
 */
public class IkAnalyzerTest {

    public static void main(String[] args) throws Exception {
        String analyzer = "ik_max_word_name";
        String text = "你好，我是罗卓伟，有什么可以帮助你的吗？";

        Dictionary.TEST_DIR_PATH = System.getProperty("user.dir") + File.separator + "config";
        AnalyzerProvider<? extends Analyzer> analyzerProvider = new AnalysisIkPlugin().getAnalyzers().get(analyzer).get(null, null, "", Settings.EMPTY);
        Analyzer ikAnalyzer = analyzerProvider.get();
        TokenStream tokenStream = ikAnalyzer.tokenStream("", text);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            TypeAttribute type = tokenStream.getAttribute(TypeAttribute.class);
            CharTermAttribute charTerm = tokenStream.getAttribute(CharTermAttribute.class);
            char[] buffer = charTerm.buffer();
            System.out.println(new String(buffer, 0, charTerm.length()) + " [" + type.type() + "]");
        }
    }

}
