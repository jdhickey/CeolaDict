
import java.io.FileWriter;
import java.io.IOException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
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

public class CeolaDict {

    static JSONObject dictionary;
    public static void main(String[] args) {
        dictionary = openDictionary();
        Window.main();
    }

    static JSONObject openDictionary() {
        File file = new File("src/main/dictionary.json");
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            return new JSONObject(content);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    static void writeDictionary(JSONObject dictionary) {
        try {
            CeolaDict.dictionary = dictionary;

            FileWriter myWriter = new FileWriter("src/main/dictionary.json");
            myWriter.write(dictionary.toString(4));
            myWriter.close();
        } catch (IOException e) {
            System.out.println("Writing failed");
        }
    }
}
