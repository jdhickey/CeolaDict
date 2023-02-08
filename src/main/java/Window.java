import org.json.JSONArray;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.*;


import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;

public class Window {
    private final Color lowColor = new Color(127, 127, 127);
    private final Color highColor = new Color(0, 0, 0);

    private final HashMap<Object, String> submitFields = new HashMap<>();
    private final HashMap<Object, String> requestFields = new HashMap<>();

    public Window() {
        String[] posOptions = {"Noun", "Verb", "Adjective", "Adverb", "Preposition",
                "Auxiliary Verb", "Conjunction", "Particle", "Pronoun", "Number",};

        Arrays.sort(posOptions);
        pos.setListData(posOptions);

        submitFields.put(word, wordText);
        submitFields.put(pronunciation, pronunciationText);
        submitFields.put(meanings, meaningsText);
        submitFields.put(translations, translationsText);
        submitFields.put(relatedWords, relatedWordsText);

        requestFields.put(query, queryText);

        HashMap<Object, String> allFields = new HashMap<>(submitFields);
        allFields.putAll(requestFields);

        //Initialize all text fields to their default state
        for (Object m : allFields.keySet()) {
            if (m instanceof JTextArea || m instanceof JTextField) {
                ((JTextComponent) m).setForeground(lowColor);
                ((JTextComponent) m).setText(allFields.get(m));
                ((JTextComponent) m).addFocusListener(createFocusAdapter(m, allFields.get(m)));
            }
        }

        //Add action listeners to all buttons
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Boolean fieldsFull = true;

                for (Object m : submitFields.keySet()) {
                    if (m.equals(relatedWords)) {
                        continue;
                    }

                    if (m instanceof JTextArea || m instanceof JTextField) {
                        fieldsFull = fieldsFull && !((JTextComponent) m).getText().equals(submitFields.get(m));
                    }
                }

                fieldsFull = fieldsFull && !(pos.getSelectedValuesList().isEmpty());

                if (fieldsFull) {
                    newWord(CeolaDict.dictionary);
                }
            }
        });

        //Load dictionary items to dictionary list

    }

    public static void main() {
        JFrame frame = new JFrame("Céola Dictionary");
        frame.setContentPane(new Window().contentPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void newWord(JSONObject dictionary) {
        String word = this.word.getText();
        resetField(this.word, wordText);

        JSONObject content = new JSONObject();
        content.put("pronunciation", this.pronunciation.getText());
        resetField(this.pronunciation, pronunciationText);

        content.put("part of speech", this.pos.getSelectedValuesList());
        resetField(this.pos);

        content.put("weak", this.isWeak.isSelected());
        resetField(this.isWeak);

        content.put("meanings", this.meanings.getText().split("\n"));
        resetField(this.meanings, meaningsText);

        content.put("translations", this.translations.getText().split("\n"));
        resetField(this.translations, translationsText);

        if (relatedWords.getForeground().equals(highColor)) {
            content.put("related", this.relatedWords.getText().split("\n"));
            resetField(this.relatedWords, relatedWordsText);
        } else {
            content.put("related", new JSONArray());
        }

        try {
            JSONArray existing = dictionary.getJSONObject("C2E").getJSONArray(word);

            for (Object item : existing) {
                if (item instanceof JSONObject) {
                    JSONAssert.assertNotEquals(content.toString(), ((JSONObject) item).toString(), false);
                }
            }

            existing.put(content);
        } catch (AssertionError ae) {
            return;
        } catch (Exception e) {
            dictionary.getJSONObject("C2E").put(word, new JSONArray());
            dictionary.getJSONObject("C2E").getJSONArray(word).put(content);
        }

        CeolaDict.writeDictionary(dictionary);
    }
    private void resetField(Object field, String... args) {
        if (field instanceof JTextArea || field instanceof JTextField) {
            ((JTextComponent) field).setText(args[0]);
            ((JTextComponent) field).setForeground(lowColor);
        } else if (field instanceof JList<?>) {
            ((JList<?>) field).clearSelection();
        } else if (field instanceof JCheckBox) {
            ((JCheckBox) field).setSelected(false);
        }
    }

    private JPanel contentPanel;
    private JPanel Fields;
    private JPanel Data;

    private JTextField word;
    private String wordText = "word";

    private JTextField pronunciation;
    private String pronunciationText = "pronunciation";

    private JCheckBox isWeak;

    private JTextArea meanings;
    private String meaningsText = "meanings";

    private JTextArea translations;
    private String translationsText = "translations";

    private JTextArea relatedWords;
    private String relatedWordsText = "related words";

    private JTextPane entry;
    private JButton submit;
    private JButton search;

    private JTextField query;
    private String queryText = "query";

    private JList pos;
    private JList dictList;
    private JPanel List;
    private JButton recall;

    private FocusAdapter createFocusAdapter(Object field, String name) {

        if (field instanceof JTextField || field instanceof JTextArea) {
            return new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    super.focusGained(e);
                    if (((JTextComponent) field).getText().equals(name)) {
                        ((JTextComponent) field).setText("");
                        ((JTextComponent) field).setForeground(highColor);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    super.focusLost(e);
                    if (((JTextComponent) field).getText().equals("")) {
                        ((JTextComponent) field).setText(name);
                        ((JTextComponent) field).setForeground(lowColor);
                    }
                }
            };
        }

        return null;
    }
}

