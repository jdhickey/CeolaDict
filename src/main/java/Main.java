
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

/*
dictionary format:
"mí" : [
      {
        "pronunciation" : "mi",
        "pos" : "aux",
        "strong" : true,
        "meanings": ["To have an obligation to, should"],
        "translations": ["should"],
        "related": [""]
      },
      {
        "pronunciation" : "mi",
        "pos" : "pronoun",
        "strong" : true,
        "meanings": ["1PL inclusive pronoun"],
        "translations": ["we"],
        "related": ["réanmí"]
      }
    ]
 */

public class Main {
    public static void main(String[] args) {

        System.out.println(Main.openDictionary());

    }

    static JSONObject openDictionary() {
        File file = new File("src/main/dictionary.json");
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            JSONObject dictionary = new JSONObject(content);
            return dictionary;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
