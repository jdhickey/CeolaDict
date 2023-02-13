import org.json.JSONArray;
import org.json.JSONObject;

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
                newWord();
            }
        });
        recallButton.addActionListener(e -> {
            displayEntry(new ArrayList<>(CeolaDict.dictionary).get(dictTable.getSelectedRow()));
        });
        deleteButton.addActionListener(e -> {
            JOptionPane confirmDelete = new JOptionPane();
            int input = confirmDelete.showConfirmDialog(null,
                    "Are you sure you want to delete this item?");

            if (input == 0) {
                ArrayList<Word> words = new ArrayList<>(CeolaDict.dictionary);
                CeolaDict.dictionary.remove(words.get(dictTable.getSelectedRow()));
                updateTable();
            }
        });

        //Load dictionary items to dictionary list properly
        dictTable.setPreferredScrollableViewportSize(dictTable.getPreferredSize());
        dictTable.setFillsViewportHeight(true);
        dictTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dictTable.setVisible(true);

        model = new DefaultTableModel(new Object[]{"Word", "POS", "Meanings"}, 0);
        dictTable.setModel(model);
        updateTable();
    }

    public static void main() {
        JFrame frame = new JFrame("Céola Dictionary");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CeolaDict.writeDictionary();
                e.getWindow().dispose();
            }
        });

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setPreferredSize(new Dimension((int)(screen.width * 0.5), (int)(screen.height * 0.75)));

        frame.setContentPane(new Window().contentPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    private void newWord() {
        Word word = new Word(this.word.getText(), this.pronunciation.getText(),
                new ArrayList<String>(this.pos.getSelectedValuesList()),
                relatedWords.getText().equals(relatedWordsText) ? new ArrayList<>() : new ArrayList<>(Arrays.asList(this.relatedWords.getText().split("\n"))),
                new ArrayList<>(Arrays.asList(this.translations.getText().split("\n"))),
                new ArrayList<>(Arrays.asList(this.meanings.getText().split("\n"))),
                this.isWeak.isSelected());

        resetField(this.word, wordText);
        resetField(this.pronunciation, pronunciationText);
        resetField(this.pos);
        resetField(this.relatedWords, relatedWordsText);
        resetField(this.isWeak);
        resetField(this.meanings, meaningsText);
        resetField(this.translations, translationsText);

        CeolaDict.dictionary.add(word);
        updateTable();
        displayEntry(word);
    }
    private void displayEntry(Word word) {
        String contentString;

        String title = "<p style=\"font-size: 1.5em; margin: 0; color: #aa0055\">" + word.getWord() + "</p>";
        String header = "<p style=\"font-size: 0.75em; margin: 0; color: #777777\">" +
                String.join(", ", word.getPos()) +
                " (" + (word.isWeak() ? "weak" : "strong") + ")" +
                "<span style=\"color: #333333;\">&emsp/"
                + word.getPronunciation() + "/</span>" +
                "</p>";
        String content1 = "<p style=\"margin: 0.5em 0 0 0\">Meanings</p>" +
                "<p style=\"font-size: 0.9em; color: #333333; margin: 0;\">-" +
                String.join("<br>-", word.getMeanings()) +
                "</p>";
        String content2 = "<p style=\"margin: 0.5em 0 0 0\">Translations</p>" +
                "<p style=\"font-size: 0.9em; color: #333333; margin: 0;\">-" +
                String.join("<br>-", word.getTranslations()) +
                "</p>";
        String content3 = "<p style=\"margin: 0.5em 0 0 0\">Related Words</p>" +
                "<p style=\"font-size: 0.9em; color: #333333; margin: 0;\">-" +
                String.join("<br>-", word.getRelated()) +
                "</p>";

        contentString = "<div style=\"padding: 5\">" + title + header + content1 + content2 +
                ((word.getRelated().size() > 0) ? content3 : "") +
                "</div>";
        dictEntry.setText(contentString);
    }
    private void updateTable() {
        ArrayList<Word> words = new ArrayList<>(CeolaDict.dictionary);
        model.setNumRows(0);

        for (Word word : words) {
            model.addRow(new Object[]{word.getWord(), String.join(", ", word.getPos()),
                    String.join(", ",word.getTranslations())});
        }
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
    private DefaultTableModel model;

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

