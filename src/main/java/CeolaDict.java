
import java.io.FileWriter;
import java.io.IOException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
public class CeolaDict {

    public static JSONObject dictionaryJSON;
    public static Set<Word> dictionary = new TreeSet<>();
    public static void main(String[] args) {
        dictionaryJSON = openDictionary();

        Iterator<String> keys = dictionaryJSON.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONArray entry = dictionaryJSON.getJSONArray(key);

            for (Object item : entry) {
                JSONObject object = (JSONObject) item;
                dictionary.add(new Word(
                        key,
                        object.getString("pronunciation"),
                        makeArrayList(object.getJSONArray("part of speech")),
                        makeArrayList(object.getJSONArray("related")),
                        makeArrayList(object.getJSONArray("translations")),
                        makeArrayList(object.getJSONArray("meanings")),
                        object.getBoolean("weak")
                ));
            }
        }

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
            CeolaDict.dictionaryJSON = dictionary;
            FileWriter myWriter = new FileWriter("src/main/dictionary.json");
            myWriter.write(dictionary.toString(4));
            myWriter.close();
        } catch (IOException e) {
            System.out.println("Writing failed");
        }
    }

    public static String[] makeArray(JSONArray arr){
        ArrayList<String> out = new ArrayList<>();

        for (Object val : arr) {
            if (val instanceof String) {
                out.add((String) val);
            }
        }

        return out.toArray(new String[0]);
    }
    public static ArrayList<String> makeArrayList(JSONArray arr){
        ArrayList<String> out = new ArrayList<>();

        for (Object val : arr) {
            if (val instanceof String) {
                out.add((String) val);
            }
        }

        return out;
    }
}
