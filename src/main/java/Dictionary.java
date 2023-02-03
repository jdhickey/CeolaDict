
import java.io.FileWriter;
import java.io.IOException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

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

public class Dictionary {
    public static void main(String[] args) {
        JSONObject dictionary = openDictionary();

        System.out.println(newWord(dictionary));
        System.out.println(dictionary.toString(4));
    }

    static JSONObject newWord(JSONObject dictionary) {
        JSONObject entry = new JSONObject();
        System.out.println("Enter word.");
        String word = readString();

        JSONObject content = new JSONObject();

        System.out.println("Enter pronunciation");
        content.put("pronunciation", readString());

        System.out.println("Enter part of speech");
        content.put("pos", readString());

        System.out.println("Enter strength");
        content.put("strong", readBoolean());

        System.out.println("Enter meanings");
        content.put("meanings", readArray());

        System.out.println("Enter translations");
        content.put("translations", readArray());

        System.out.println("Enter related words");
        content.put("related", readArray());

        entry.put(word, content);
        dictionary.getJSONObject("C2E").put(word, content);
        writeDictionary(dictionary);

        return entry;
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
            FileWriter myWriter = new FileWriter("src/main/dictionary.json");
            myWriter.write(dictionary.toString(4));
            myWriter.close();
        } catch (IOException e) {}
    }

    static String readString() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
    static boolean readBoolean() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            try {
                return Boolean.parseBoolean(scanner.nextLine());
            } catch (RuntimeException e) {}
        }
    }
    static ArrayList<String> readArray() {
        ArrayList<String> list = new ArrayList<>();

        String word = readString();

        while (!word.equals("")) {
            list.add(word);
            word = readString();
        }

        return list;
    }
}
