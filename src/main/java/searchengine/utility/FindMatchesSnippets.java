package searchengine.utility;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FindMatchesSnippets implements Callable {
    String search;
    String text;
    StringBuilder snippet;

    public FindMatchesSnippets(String search, String text) {
        this.search = search;
        this.text = text;
        this.snippet = new StringBuilder();
    }

    @Override
    public Object call() throws Exception {

        Document document = Jsoup.parse(text);
        String regex = ".{0,30}\\s" + search + "\\s.{0,30}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(document.text().toLowerCase(Locale.ROOT));

        while (matcher.find()) {
            String substring = matcher.group();

            snippet.append(substring.replaceAll(search, "<b>" + search + "</b>"));
        }

        return snippet.toString();

    }

}

