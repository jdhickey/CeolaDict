import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Word implements Comparable{
    private String word;
    private String pronunciation;

    public String getWord() {
        return word;
    }
    public String getPronunciation() {
        return pronunciation;
    }
    public ArrayList<String> getPos() {
        return pos;
    }
    public ArrayList<String> getRelated() {
        return related;
    }
    public ArrayList<String> getTranslations() {
        return translations;
    }
    public ArrayList<String> getMeanings() {
        return meanings;
    }
    public boolean isWeak() {
        return weak;
    }

    private ArrayList<String> pos;
    private ArrayList<String> related;
    private ArrayList<String> translations;
    private ArrayList<String> meanings;
    private boolean weak;

    public Word(String word, String pronunciation, ArrayList<String> pos, ArrayList<String> related,
                ArrayList<String> translations, ArrayList<String> meanings, boolean weak) {
        this.word = word;
        this.pronunciation = pronunciation;
        this.pos = pos;
        this.related = related;
        this.translations = translations;
        this.meanings = meanings;
        this.weak = weak;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof Word) {
            return -((Word) o).word.compareTo(this.word);
        } else {
            return 0;
        }
    }
}
