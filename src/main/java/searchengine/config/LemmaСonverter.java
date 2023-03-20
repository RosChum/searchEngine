package searchengine.config;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class LemmaСonverter {


    private HashMap<String, Integer> result;


    public HashMap<String, Integer> convertTextToLemmas(String text) throws IOException {

        result = new HashMap<>();

        LuceneMorphology luceneMorph = new RussianLuceneMorphology();

        String[] splitText = text.toLowerCase(Locale.ROOT).replaceAll("[^А-Яа-яЁё\\s]", "")
                .split("\\s");

        for (String s : splitText) {
            if (s.isEmpty()) continue;
            List<String> wordBaseForms = luceneMorph.getMorphInfo(s);
            wordBaseForms.stream().filter(word -> !word.matches(".*ЧАСТ.*|.*МЕЖД.*|.*ПРЕДЛ.*|.*СОЮЗ.*"))
                    .map(word -> word.substring(0, word.lastIndexOf('|'))).forEach(word -> {
                        if (!result.containsKey(word)) {
                            result.put(word, 1);
                        } else {
                            result.put(word, result.get(word) + 1);
                        }

                    });
        }

        return result;
    }

    public String clearingPageTags(String text) {

        Document document = Jsoup.parse(text);

        return document.text();
    }
}
