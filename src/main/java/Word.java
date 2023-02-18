import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/** Represents information about a word for a dictionary entry.
 * @author Jack Hickey
 */
public class Word implements Comparable{
    private String word;
    private String pronunciation;
    private ArrayList<String> pos;
    private ArrayList<String> related;
    private ArrayList<String> translations;
    private ArrayList<String> meanings;
    private boolean weak;
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public ArrayList<String> getPos() {
        return pos;
    }

    public void setPos(ArrayList<String> pos) {
        this.pos = pos;
    }

    public ArrayList<String> getRelated() {
        return related;
    }

    public void setRelated(ArrayList<String> related) {
        this.related = related;
    }

    public ArrayList<String> getTranslations() {
        return translations;
    }

    public void setTranslations(ArrayList<String> translations) {
        this.translations = translations;
    }

    public ArrayList<String> getMeanings() {
        return meanings;
    }

    public void setMeanings(ArrayList<String> meanings) {
        this.meanings = meanings;
    }

    public boolean isWeak() {
        return weak;
    }

    public void setWeak(boolean weak) {
        this.weak = weak;
    }

    /**
     * Creates a word with the specified information.
     * @param word The textual representation of the word.
     * @param pronunciation The IPA pronunciation of the word.
     * @param pos An ArrayList of the valid parts of speech for the word.
     * @param related An ArrayList of related words.
     * @param translations An ArrayList of valid multi-word translations.
     * @param meanings An ArrayList of valid one-word English translations.
     * @param weak A boolean value representing the 'weak' aspect of the word, per rules of Ceola.
     */
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

    /**
     * An empty constructor for a word with no content
     */
    public Word() {}

    /** Compares this to an object passed in. If o is not of class Word it returns -1.
     * If o is of class Word, it compares the word value, pronunciation, and parts of speech of the word.
     * @param o the object to be compared.
     * @return 0 if equal, 1 if alphabetically before this, -1 if alphabetically after
     * (or if different determined by the parts of speech).
     */
    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof Word) {
            int wordComp = -((Word) o).word.compareTo(this.word);
            int posComp = ((Word) o).pos.equals(this.pos) ? 0 : -1;
            int pronunciationComp = -((Word) o).pronunciation.compareTo(this.pronunciation);

            return (wordComp != 0) ? wordComp : ((pronunciationComp != 0 ? pronunciationComp : posComp));
        } else {
            return -1;
        }
    }

    /** Evaluates the equality of obj and this using compareTo.
     * @param obj
     * @return either the result of .compareTo() or .equals() inherited from Object.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Word) {
            return ((Word) obj).compareTo(this) == 0;
        } else {
            return super.equals(obj);
        }
    }
}
