import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.HashMap;

public class Window {
    private final Color lowColor = new Color(127, 127, 127);
    private final Color highColor = new Color(0, 0, 0);

    private final HashMap<Object, String> submitFields = new HashMap<>();
    private final HashMap<Object, String> requestFields = new HashMap<>();

    public Window() {
        String[] posOptions = {
                "Noun",
                "Verb",
                "Adjective",
                "Adverb",
                "Preposition",

                "Auxiliary Verb",
                "Conjunction",
                "Particle",
                "Pronoun",
                "Number",
        };

        Arrays.sort(posOptions);
        pos.setListData(posOptions);

        submitFields.put(word, "word");
        submitFields.put(pronunciation, "pronunciation");
        submitFields.put(meanings, "meanings");
        submitFields.put(translations, "translations");
        submitFields.put(relatedWords, "related words");

        requestFields.put(query, "query");

        HashMap<Object, String> allFields = new HashMap<>(submitFields);
        allFields.putAll(requestFields);

        for (Object m : allFields.keySet()) {
            if (m instanceof JTextArea || m instanceof JTextField) {
                ((JTextComponent) m).setForeground(lowColor);
                ((JTextComponent) m).setText(allFields.get(m));
                ((JTextComponent) m).addFocusListener(createFocusAdapter(m, allFields.get(m)));
            }
        }
        Window window = this;
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
                    newWord(Dictionary.dictionary);
                }
            }
        });
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

        JSONObject content = new JSONObject();
        content.put("pronunciation", this.pronunciation.getText());
        content.put("pos", this.pos.getSelectedValuesList());
        content.put("strong", this.isStrong.isSelected());
        content.put("meanings", this.meanings.getText().split("\n"));
        content.put("translations", this.translations.getText().split("\n"));
        if (relatedWords.getForeground().equals(highColor)) {
            content.put("related", this.relatedWords.getText().split("\n"));
        } else {
            content.put("related", new JSONArray());
        }

        try {
            dictionary.getJSONObject("C2E").getJSONArray(word).put(content);
        } catch (Exception e) {
            dictionary.getJSONObject("C2E").put(word, new JSONArray());
            dictionary.getJSONObject("C2E").getJSONArray(word).put(content);
        }

        Dictionary.writeDictionary(dictionary);
    }

    private JPanel contentPanel;
    private JPanel Fields;
    private JPanel Data;
    private JTextField word;
    private JTextField pronunciation;
    private JCheckBox isStrong;
    private JTextArea meanings;
    private JTextArea translations;
    private JTextArea relatedWords;
    private JTextPane entry;
    private JButton submit;
    private JButton recall;
    private JTextField query;
    private JList pos;

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

