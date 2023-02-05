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
    private Color lowColor = new Color(127, 127, 127);
    private Color highColor = new Color(0, 0, 0);

    private String[] posOptions = {
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

    private HashMap<Object, String> fields = new HashMap<>();


    public Window() {
        Arrays.sort(posOptions);
        pos.setListData(posOptions);

        fields.put(word, "word");
        fields.put(pronunciation, "pronunciation");
        fields.put(meanings, "meanings");
        fields.put(translations, "translations");
        fields.put(relatedWords, "related words");
        fields.put(query, "query");

        for (Object m : fields.keySet()) {
            if (m instanceof JTextArea || m instanceof JTextField) {
                ((JTextComponent) m).setForeground(lowColor);
                ((JTextComponent) m).setText(fields.get(m));
                ((JTextComponent) m).addFocusListener(createFocusAdapter(m, fields.get(m)));
            }
        }
    }

    public static void main() {
        JFrame frame = new JFrame("Céola Dictionary");
        frame.setContentPane(new Window().contentPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
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

