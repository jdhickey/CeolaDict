import javax.swing.*;

public class Window {

    private String[] posOptions = {
            "Noun",
            "Verb",
            "Adjective",
            "Adverb",

    };

    public Window() {
        pos.setListData(posOptions);
    }

    public static void main() {
        JFrame frame = new JFrame("Window");
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
}
