package searchengine.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Slf4j
public class LemmaСonverter {

    private HashMap<String, Integer> result;

    public HashMap<String, Integer> convertTextToLemmas(String text) {
        result = new HashMap<>();
        LuceneMorphology luceneMorph = null;
        try {
            luceneMorph = new RussianLuceneMorphology();
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        String[] splitText = text.toLowerCase(Locale.ROOT).replaceAll("[^А-Яа-яЁё\\s]", "")
                .split("\\s");

        for (String s : splitText) {
            if (s.isEmpty() || !luceneMorph.checkString(s)) continue;

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

}
