import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines a windowed application for interacting with the Ceola dictionary.
 */
public class Window {
    /** The standard text color for inactive fields. */
    private final Color lowColor = new Color(127, 127, 127);
    /** The standard text color for active fields. */
    private final Color highColor = new Color(0, 0, 0);

    /** A map of all the submission fields and their default states. */
    private final HashMap<Object, String> submitFields = new HashMap<>();
    /** An array of all possible selection values for the part of speech. */
    private final String[] posOptions = {"Noun", "Verb", "Adjective", "Adverb", "Preposition",
            "Auxiliary Verb", "Conjunction", "Particle", "Pronoun", "Number"};
    /** A pattern of all valid characters for the dictionary. */
    private final Pattern VALID_CHARACTERS = Pattern.compile("^[mngptcfshlyaeuioáéúíór\\s]+", Pattern.CASE_INSENSITIVE);

    /**
     * Instantiates a new Window with the required fields and logic in place. Add appropriate elements as well as
     * action listeners to buttons, required data to selection fields, and the dictionary to the display table.
     */
    public Window() {
        // Set the options for the parts of speech selection field
        Arrays.sort(posOptions);
        pos.setListData(posOptions);

        // Ensure the display window can show html properly
        dictEntry.setContentType("text/html");

        // Sets default content for text-based fields
        submitFields.put(word, wordText);
        submitFields.put(pronunciation, pronunciationText);
        submitFields.put(meanings, meaningsText);
        submitFields.put(translations, translationsText);
        submitFields.put(relatedWords, relatedWordsText);

        HashMap<Object, String> allFields = new HashMap<>(submitFields);
        allFields.put(query, queryText);

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

            // Ensures all submission fields (except for related words) have meaningful content
            for (Object m : submitFields.keySet()) {
                if (m.equals(relatedWords)) {
                    continue;
                }

                if (m instanceof JTextArea || m instanceof JTextField) {
                    fieldsFull = fieldsFull && !((JTextComponent) m).getText().equals(submitFields.get(m));
                }
            }

            fieldsFull = fieldsFull && !(pos.getSelectedValuesList().isEmpty());

            // If the fields are all appropriately filled, proceed with creating a new word.
            if (fieldsFull) {
                newWord();
            }
        });
        deleteButton.addActionListener(e -> {
            if (dictTable.getSelectedRow() >= 0) {
                int input = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to delete this item?");

                int index = dictTable.getSelectedRow();
                if (input == 0) {
                    Word word = CeolaDict.dictionary.remove(index);

                    // If there are no words with that share the same related field as word, remove word from
                    // all word's related fields, and delete word's relatedMap entry
                    if (CeolaDict.dictionary.stream().map(Word::getWord).noneMatch(word.getWord()::equals)) {
                        for (String s : CeolaDict.relatedMap.get(word.getWord())) {
                            CeolaDict.relatedMap.get(s).remove(word.getWord());
                        }

                        CeolaDict.relatedMap.remove(word.getWord());
                    }

                    updateTable(CeolaDict.dictionary);
                }

                try {
                    dictTable.setRowSelectionInterval(index-1, index-1);
                } catch (IllegalArgumentException iae) {
                    if (dictTable.getRowCount() > 0) {
                        dictTable.setRowSelectionInterval(index, index);
                    } else {
                        displayEntry(new Word());
                    }
                }
            }
        });
        editButton.addActionListener(e -> {
            //Refills the fields with content from the selected word
            Word word = CeolaDict.dictionary.get(dictTable.getSelectedRow());
            setFields(word);
        });
        searchButton.addActionListener(e -> {
            String search = query.getText();
            if (search.equals(queryText)) {
                updateTable(CeolaDict.dictionary);
            } else {

                resetField(query, queryText);
                Matcher matcher = VALID_CHARACTERS.matcher(search);

                // Proceed only if the search contains only valid characters.
                if (matcher.find()) {
                    String[] matchChars = search.split("");

                    // Build a regex from the search term.
                    for (int i = 0; i < matchChars.length; i++) {

                        // Ensure accented vowels are interchangeable with non-accented vowels
                        switch (matchChars[i]) {
                            case ("a"), ("á") -> matchChars[i] = "[aá]";
                            case ("e"), ("é") -> matchChars[i] = "[eé]";
                            case ("i"), ("í") -> matchChars[i] = "[ií]";
                            case ("o"), ("ó") -> matchChars[i] = "[oó]";
                            case ("u"), ("ú") -> matchChars[i] = "[uú]";
                        }
                    }

                    Pattern pattern = Pattern.compile(String.join(".*", matchChars), Pattern.CASE_INSENSITIVE);
                    ArrayList<Word> out = new ArrayList<>();

                    // Adds all matches to the ArrayList out
                    for (Word word : CeolaDict.dictionary) {
                        if (pattern.matcher(word.getWord()).find()) {
                            out.add(word);
                        }
                    }

                    // Updates the display table with all the matches to the regex
                    updateTable(out);
                }
            }
        });

        //Makes it so that the selected item from the table is displayed
        dictTable.getSelectionModel().addListSelectionListener(event -> {
            if (dictTable.getSelectedRow() >= 0) {
                displayEntry(CeolaDict.dictionary.get(dictTable.getSelectedRow()));
            }
        });

        //Load dictionary items to dictionary list properly
        dictTable.setPreferredScrollableViewportSize(dictTable.getPreferredSize());
        dictTable.setFillsViewportHeight(true);
        dictTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dictTable.setVisible(true);

        //Sets the column headers of the table
        model = new DefaultTableModel(new Object[]{"Word", "POS", "Meanings"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        dictTable.setModel(model);
        updateTable(CeolaDict.dictionary);
    }

    /**
     * Instantiates a new Window object.
     */
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

    /**
     * Adds a new word to the dictionary. This updates dictionary and relatedMap.
     * @see CeolaDict
     */
    private void newWord() {
        // Creates a new word object with information from the Window fields.
        Word word = new Word(this.word.getText(), this.pronunciation.getText(),
                new ArrayList<>(this.pos.getSelectedValuesList()),
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList(this.translations.getText().split("\n"))),
                new ArrayList<>(Arrays.asList(this.meanings.getText().split("\n"))),
                this.isWeak.isSelected());

        // Handles the case of no related words for the new word
        String[] related = this.relatedWords.getText().split("\n");
        if (related.length == 1 && related[0].equals(relatedWordsText)) {
            related = new String[]{};
            CeolaDict.relatedMap.put(word.getWord(), new ArrayList<>());
        }

        // Adds word to the relatedMap entry for every related
        for (String s : related) {
            if (!CeolaDict.relatedMap.get(s).contains(word.getWord())) {
                CeolaDict.relatedMap.get(s).add(word.getWord());
            }
        }

        // Adds every related to the relatedMap entry for word
        try {
            for (String s : related) {
                if (!CeolaDict.relatedMap.get(word.getWord()).contains(s)) {
                    CeolaDict.relatedMap.get(word.getWord()).add(s);
                }
            }
        } catch (RuntimeException re) {
            CeolaDict.relatedMap.put(word.getWord(), new ArrayList<>(Arrays.asList(related)));
        }

        Collections.sort(CeolaDict.relatedMap.get(word.getWord()));
        word.setRelated(CeolaDict.relatedMap.get(word.getWord()));

        /*
        If the dictionary contains a word that matches the new entry (based on spelling, pronunciation and
        parts of speech) this confirms that the user wants to replace the previous entry.
         */
        int input = 0;
        if (CeolaDict.dictionary.contains(word)) {
            input = JOptionPane.showConfirmDialog(null,
                    "Do you want to update the dictionary entry for " + word.getWord() + "?");
            if (input == 0) {
                for (String s : CeolaDict.relatedMap.get(word.getWord())) {
                    if (!Arrays.asList(related).contains(s) && CeolaDict.relatedMap.get(word.getWord()).contains(s)) {
                        CeolaDict.relatedMap.get(s).remove(word.getWord());
                    }
                }
                CeolaDict.relatedMap.get(word.getWord()).clear();
                CeolaDict.relatedMap.get(word.getWord()).addAll(Arrays.asList(related));
            }
        }

        if (input == 0) {
            // Resets all the Window submission fields
            resetField(this.word, wordText);
            resetField(this.pronunciation, pronunciationText);
            resetField(this.pos);
            resetField(this.relatedWords, relatedWordsText);
            resetField(this.isWeak);
            resetField(this.meanings, meaningsText);
            resetField(this.translations, translationsText);

            // Removes the word (to prevent against duplicates)
            CeolaDict.dictionary.remove(word);

            // Adds the word, resorts the dictionary
            CeolaDict.dictionary.add(word);
            Collections.sort(CeolaDict.dictionary);

            // Updates the table of words
            updateTable(CeolaDict.dictionary);

            // Displays the word and it's full entry
            displayEntry(word);
        }
    }

    /**
     * Displays a given Word object in the dictEntry field of the window.
     * @param word The desired Word to be displayed.
     */
    private void displayEntry(Word word) {
        if (word.getWord() != null) {
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
        } else {
            dictEntry.setText("");
        }
    }

    /**
     * Updates the contents of the dictTable field of the window.
     * @param words An ArrayList of the words desired to be displayed.
     */
    private void updateTable(ArrayList<Word> words) {
        model.setNumRows(0);

        for (Word word : words) {
            model.addRow(new Object[]{word.getWord(), String.join(", ", word.getPos()),
                    String.join(", ",word.getTranslations())});
        }
    }

    /**
     * Resets the field of the window to its "default" state.
     * @param field The field from the window to be reset.
     * @param args If a text field is passed, this is expected to contain the text to reset it to.
     */
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

    /**
     * Sets the input fields of the Window to match the content of the word passed in.
     * Allows the user to edit the word.
     * @param word The word to be displayed.
     */
    private void setFields(Word word) {
        this.word.setText(word.getWord());
        this.word.setForeground(highColor);

        this.pronunciation.setText(word.getPronunciation());
        this.pronunciation.setForeground(highColor);

        this.pos.clearSelection();
        for (String part : word.getPos()) {
            int index = Arrays.asList(posOptions).indexOf(part);
            this.pos.getSelectionModel().addSelectionInterval(index, index);
        }

        if (word.getRelated().size() > 0) {
            this.relatedWords.setText(String.join("\n", word.getRelated()));
            this.relatedWords.setForeground(highColor);
        }

        this.isWeak.setSelected(word.isWeak());

        this.meanings.setText(String.join("\n", word.getMeanings()));
        this.meanings.setForeground(highColor);

        this.translations.setText(String.join("\n", word.getTranslations()));
        this.translations.setForeground(highColor);
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

    private JList<String> pos;
    private JPanel List;
    private JButton editButton;
    private JScrollPane ListScroll;
    private JScrollPane meaningScroll;
    private JScrollPane translationScroll;
    private JScrollPane relatedScroll;
    private JPanel listButtons;
    private JButton deleteButton;
    private JTable dictTable;
    private DefaultTableModel model;

    /**
     * Used to create a FocusAdapter for a textual Window element.
     * @param field The field the adapter should be added to.
     * @param defaultText The text for the element to hold when inactive/unfilled.
     * @return A new FocusAdapter if field is a textual element, otherwise null.
     */
    private FocusAdapter createFocusAdapter(Object field, String defaultText) {

        if (field instanceof JTextField || field instanceof JTextArea) {
            return new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    super.focusGained(e);
                    if (((JTextComponent) field).getText().equals(defaultText)) {
                        ((JTextComponent) field).setText("");
                        ((JTextComponent) field).setForeground(highColor);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    super.focusLost(e);
                    if (((JTextComponent) field).getText().equals("")) {
                        ((JTextComponent) field).setText(defaultText);
                        ((JTextComponent) field).setForeground(lowColor);
                    }
                }
            };
        }

        return null;
    }
}
