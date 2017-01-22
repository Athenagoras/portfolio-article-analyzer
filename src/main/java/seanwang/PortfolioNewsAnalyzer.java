package seanwang;

import com.sun.deploy.util.StringUtils;

import java.util.*;
import java.lang.*;
import java.net.URL;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLDocument;
import de.l3s.boilerpipe.sax.HTMLFetcher;

public class PortfolioNewsAnalyzer {
    private HashSet<String> portfolio;
    private static final String modelPath = "edu\\stanford\\nlp\\models\\pos-tagger\\english-left3words\\english-left3words-distsim.tagger";
    private static MaxentTagger tagger;
    public PortfolioNewsAnalyzer(){
        this.portfolio = new HashSet<String>();
        this.tagger = new MaxentTagger(modelPath);
    }
    
    public void addPortfolioCompany(String company) {
        this.portfolio.add(company);
    }

    public boolean analyzeArticle(String urlString) throws
            java.io.IOException,
            org.xml.sax.SAXException,
            de.l3s.boilerpipe.BoilerpipeProcessingException
    {
        String articleText = this.extractFromUrl(urlString);
        String tagged = this.tagPos(articleText);
        HashSet<String> properNounsSet = this.extractProperNouns(tagged);
        boolean mentioned = this.arePortfolioCompaniesMentioned(properNounsSet);
        return mentioned;
    }

    public boolean arePortfolioCompaniesMentioned(HashSet<String> articleProperNouns){
        return !Collections.disjoint(articleProperNouns, this.portfolio);
    }
    
    public static HashSet<String> extractProperNouns(String taggedOutput) {
        HashSet<String> propNounSet = new HashSet<String>();
        String[] split = taggedOutput.split(" ");
        List<String> propNounList = new ArrayList<String>();
        for (String token: split ){
            String[] split_token = token.split("_");
            if(split_token[1].equals("NNP")){
                propNounSet.add(split_token[0]);
                propNounList.add(split_token[0]);
            } else {
                if (!propNounList.isEmpty()) {
                    propNounSet.add(StringUtils.join(propNounList, " "));
                    propNounList.clear();
                }
            }
        }
        if (!propNounList.isEmpty()) {
            propNounSet.add(StringUtils.join(propNounList, " "));
            propNounList.clear();
        }
        return propNounSet;
    }

    public static String tagPos(String input) {
        String output = tagger.tagString(input);
        return output;
    }

    public static String extractFromUrl(String userUrl) throws
            java.io.IOException,
            org.xml.sax.SAXException,
            de.l3s.boilerpipe.BoilerpipeProcessingException  {
        final HTMLDocument htmlDoc = HTMLFetcher.fetch(new URL(userUrl));
        final TextDocument doc = new BoilerpipeSAXInput(htmlDoc.toInputSource()).getTextDocument();
        String content = CommonExtractors.ARTICLE_EXTRACTOR.getText(doc);
        return content;
    }
}
