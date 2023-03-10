
import java.io.FileWriter;
import java.io.IOException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The main class of the CeolaDict project. Used to open the dictionary JSON file and open the application window.
 */
public class CeolaDict {
    /**
     * The read in JSON object representing the entire dictionary.
     */
    public static JSONObject dictionaryJSON;
    /**
     * An arraylist which contains all the words in the dictionary.
     */
    public static ArrayList<Word> dictionary = new ArrayList<>();
    /**
     * A map of words to an arraylist of their related words.
     */
    public static HashMap<String, ArrayList<String>> relatedMap = new HashMap<>();

    /** The file path the dictionary is saved to upon close. */
    static String filePath = "./dictionary.json";

    /**
     * Opens the dictionary JSON file and reads it into dictionaryJSON and subsequently dictionary.
     * Populates relatedMap with every word and their referents.
     * Runs Window.main() to open the application.
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        openDictionary(filePath);
        Window.main();
    }

    /**
     * This opens the dictionary and gets the JSON Object representing it.
     */
    static void openDictionary(String pathname) {
        CeolaDict.dictionary = new ArrayList<>();
        CeolaDict.dictionaryJSON = new JSONObject();

        File file = new File(pathname);

        try {
            dictionaryJSON = new JSONObject(new String(Files.readAllBytes(Paths.get(file.toURI()))));

            Iterator<String> keys = dictionaryJSON.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject content = dictionaryJSON.getJSONObject(key);

                for (Object item : content.getJSONArray("entries")) {
                    JSONObject object = (JSONObject) item;
                    Word word = new Word(
                            key,
                            object.getString("pronunciation"),
                            makeArrayList(object.getJSONArray("part of speech")),
                            makeArrayList(content.getJSONArray("related")),
                            makeArrayList(object.getJSONArray("translations")),
                            makeArrayList(object.getJSONArray("meanings")),
                            object.getBoolean("weak")
                    );
                    dictionary.add(word);

                    for (String related : makeArrayList(content.getJSONArray("related"))) {
                        try {
                            if (!relatedMap.get((word.getWord())).contains(related)) {
                                relatedMap.get(word.getWord()).add(related);
                            }
                        } catch (RuntimeException re) {
                            ArrayList<String> newArr = new ArrayList<>();
                            newArr.add(related);
                            relatedMap.put(word.getWord(), newArr);
                        }
                    }

                    if (content.getJSONArray("related").length() > 0) {
                        Collections.sort(relatedMap.get(word.getWord()));
                    } else {
                        relatedMap.put(word.getWord(), new ArrayList<>());
                    }

                    word.setRelated(relatedMap.get(word.getWord()));
                }
            }

            Collections.sort(dictionary);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses the dictionary ArrayList back into a JSON object and writes it to the dictionary.json file.
     * Copies the previous content of the dictionary.json file into archive.json, just in case.
     */
    static void writeDictionary(String path) {
        JSONObject dictOut = new JSONObject();

        for (Word word : dictionary) {
            JSONObject content = new JSONObject();
            content.put("pronunciation", word.getPronunciation());
            content.put("part of speech", word.getPos());
            content.put("translations", word.getTranslations());
            content.put("meanings", word.getMeanings());
            content.put("weak", word.isWeak());

            try {
                dictOut.getJSONObject(word.getWord()).getJSONArray("entries").put(content);
            } catch (JSONException je) {
                dictOut.put(word.getWord(), new JSONObject().put("entries", new JSONArray()));
                dictOut.getJSONObject(word.getWord()).getJSONArray("entries").put(content);
                dictOut.getJSONObject(word.getWord()).put("related", word.getRelated());
            }
        }

        try {
            File old = new File(path);
            File archive = new File("./archive.json");
            old.renameTo(archive);

            FileWriter currentWriter = new FileWriter(path);
            currentWriter.write(dictOut.toString(4));
            currentWriter.close();
        } catch (IOException e) {
            System.out.println("Writing failed");
            e.printStackTrace();
        }
    }

    /**
     * Converts a JSONArray object to an ArrayList of strings.
     * @param arr The JSON array to be converted.
     * @return an ArrayList of strings from a JSON array.
     */
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
