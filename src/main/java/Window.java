import org.json.JSONArray;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.*;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Window {
    private final Color lowColor = new Color(127, 127, 127);
    private final Color highColor = new Color(0, 0, 0);

    private final HashMap<Object, String> submitFields = new HashMap<>();
    private final HashMap<Object, String> requestFields = new HashMap<>();
    private final String[] posOptions = {"Noun", "Verb", "Adjective", "Adverb", "Preposition",
            "Auxiliary Verb", "Conjunction", "Particle", "Pronoun", "Number"};

    public Window() {


        Arrays.sort(posOptions);
        pos.setListData(posOptions);
        dictEntry.setContentType("text/html");

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

        //Add action listeners to the buttons
        submitButton.addActionListener(e -> {
            boolean fieldsFull = true;

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
        });
        recallButton.addActionListener(e -> {
            String key = (String) dictTable.getValueAt(dictTable.getSelectedRow(), 0);
            int index = Integer.parseInt((String) model.getValueAt(dictTable.getSelectedRow(), 0));
            displayEntry(key, CeolaDict.dictionary.getJSONObject("C2E").getJSONArray(key).getJSONObject(index));
        });
        deleteButton.addActionListener(e -> {
            String key = (String) dictTable.getValueAt(dictTable.getSelectedRow(), 0);
            int index = Integer.parseInt((String) model.getValueAt(dictTable.getSelectedRow(), 0));
            JOptionPane confirmDelete = new JOptionPane();
            int input = confirmDelete.showConfirmDialog(null,
                    "Are you sure you want to delete this item?");

            if (input == 0) {
                CeolaDict.dictionary.getJSONObject("C2E").getJSONArray(key).remove(index);
                if (CeolaDict.dictionary.getJSONObject("C2E").getJSONArray(key).length() < 1) {
                    CeolaDict.dictionary.getJSONObject("C2E").remove(key);
                }

                CeolaDict.writeDictionary(CeolaDict.dictionary);
                setDictTable(model);
            }
        });

        //Load dictionary items to dictionary list
        {model.setColumnCount(4);
        dictTable.removeColumn(dictTable.getColumnModel().getColumn(0));
        dictTable.setPreferredScrollableViewportSize(dictTable.getPreferredSize());
        dictTable.setFillsViewportHeight(true);
        dictTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dictTable.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Word");
        dictTable.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("POS");
        dictTable.getTableHeader().getColumnModel().getColumn(2).setHeaderValue("Meaning");}
        setDictTable(model);
    }

    public static void main() {
        JFrame frame = new JFrame("Céola Dictionary");

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setPreferredSize(new Dimension((int)(screen.width * 0.5), (int)(screen.height * 0.75)));

        frame.setContentPane(new Window().contentPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void newWord(JSONObject dictionary) {
        String word = this.word.getText();
        resetField(this.word, wordText);

        JSONObject content = new JSONObject();
        // Set content and reset fields
        {
            content.put("pronunciation", this.pronunciation.getText());
            resetField(this.pronunciation, pronunciationText);

            content.put("part of speech", new JSONArray(this.pos.getSelectedValuesList()));
            resetField(this.pos);

            content.put("weak", this.isWeak.isSelected());
            resetField(this.isWeak);

            content.put("meanings", new JSONArray(Arrays.asList(this.meanings.getText().split("\n"))));
            resetField(this.meanings, meaningsText);

            content.put("translations", new JSONArray(Arrays.asList(this.translations.getText().split("\n"))));
            resetField(this.translations, translationsText);


            if (relatedWords.getForeground().equals(highColor)) {
                content.put("related", new JSONArray(Arrays.asList(this.relatedWords.getText().split("\n"))));
                resetField(this.relatedWords, relatedWordsText);
            } else {
                content.put("related", new JSONArray());
            }
        }

        try {
            JSONArray existing = dictionary.getJSONObject("C2E").getJSONArray(word);

            for (Object item : existing) {
                if (item instanceof JSONObject) {
                    JSONAssert.assertNotEquals(content.toString(), item.toString(), false);
                }
            }

            existing.put(content);
        } catch (AssertionError ae) {
            return;
        } catch (Exception e) {
            dictionary.getJSONObject("C2E").put(word, new JSONArray());
            dictionary.getJSONObject("C2E").getJSONArray(word).put(content);
        }

        displayEntry(word, content);
        CeolaDict.writeDictionary(dictionary);
        setDictTable(model);
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

    private void displayEntry(String word, JSONObject content) {
        String contentString;

        String title = "<p style=\"font-size: 1.5em; margin: 0; color: #aa0055\">" + word + "</p>";
        String header = "<p style=\"font-size: 0.75em; margin: 0; color: #777777\">" +
                String.join(", ", makeList(content.getJSONArray("part of speech"))) +
                " (" + (content.getBoolean("weak") ? "weak" : "strong") + ")" +
                "<span style=\"color: #333333;\">&emsp/"
                + content.getString("pronunciation") + "/</span>" +
                "</p>";
        String content1 = "<p style=\"margin: 0.5em 0 0 0\">Meanings</p>" +
                "<p style=\"font-size: 0.9em; color: #333333; margin: 0;\">-" +
                String.join("<br>-", makeList(content.getJSONArray("meanings"))) +
                "</p>";
        String content2 = "<p style=\"margin: 0.5em 0 0 0\">Translations</p>" +
                "<p style=\"font-size: 0.9em; color: #333333; margin: 0;\">-" +
                String.join("<br>-", makeList(content.getJSONArray("translations"))) +
                "</p>";
        String content3 = "<p style=\"margin: 0.5em 0 0 0\">Related Words</p>" +
                "<p style=\"font-size: 0.9em; color: #333333; margin: 0;\">-" +
                String.join("<br>-", makeList(content.getJSONArray("related"))) +
                "</p>";

        contentString = "<div style=\"padding: 5\">" + title + header + content1 + content2 +
                (((makeList(content.getJSONArray("related"))).length > 0) ? content3 : "") +
                "</div>";
        dictEntry.setText(contentString);
    }

    public void setDictTable(DefaultTableModel model) {
        JSONObject currentDict = ((JSONObject) CeolaDict.dictionary.get("C2E"));
        ArrayList<String> keys = new ArrayList<>();
        currentDict.keys().forEachRemaining(keys::add);
        Collections.sort(keys);

        model.setRowCount(0);

        for (String key : keys) {
            int count = 0;
            for (Object item : currentDict.getJSONArray(key)) {
                String[] parts = makeList(((JSONObject) item).getJSONArray("part of speech"));
                String[] translations = makeList(((JSONObject) item).getJSONArray("translations"));
                model.addRow(new String[] {Integer.toString(count), key, String.join(", ", parts), String.join(", ", translations)});
                count++;
            }
        }
    }

    public String[] makeList(JSONArray arr){
        ArrayList<String> out = new ArrayList<>();

        for (Object val : arr) {
            if (val instanceof String) {
                out.add((String) val);
            }
        }

        return out.toArray(new String[0]);
    }

    private JPanel contentPanel;
    private JPanel Fields;
    private JPanel Data;

    private JTextField word;
    private final String wordText = "word";

    private JTextField pronunciation;
    private final String pronunciationText = "pronunciation";

    private JCheckBox isWeak;

    private JTextArea meanings;
    private final String meaningsText = "meanings";

    private JTextArea translations;
    private final String translationsText = "translations";

    private JTextArea relatedWords;
    private final String relatedWordsText = "related words";

    private JEditorPane dictEntry;
    private JButton submitButton;
    private JButton searchButton;

    private JTextField query;
    private final String queryText = "query";

    private JList pos;
    private JPanel List;
    private JButton editButton;
    private JScrollPane ListScroll;
    private JScrollPane meaningScroll;
    private JScrollPane translationScroll;
    private JScrollPane relatedScroll;
    private JButton recallButton;
    private JPanel listButtons;
    private JButton deleteButton;
    private JTable dictTable;
    private DefaultTableModel model = (DefaultTableModel) dictTable.getModel();

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

