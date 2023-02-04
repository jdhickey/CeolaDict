
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class Window extends JFrame{
    int screen_width;
    int screen_height;
    public Window(int percent) {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        this.screen_width = gd.getDisplayMode().getWidth();
        this.screen_height = gd.getDisplayMode().getHeight();

        setSize(this.screen_width * percent/100, this.screen_height * percent/100);
        setLocationRelativeTo(null);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.LINE_AXIS));
        contentPanel.setSize(getWidth(), getHeight());
            contentPanel.add(new JButton("Hello"));

        contentPanel.setBackground(new Color(127, 127, 255));
        contentPanel = loadInputFields(contentPanel);
        contentPanel.setVisible(true);

        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel loadInputFields(JPanel parent) {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.PAGE_AXIS));
        inputPanel.setSize(parent.getWidth(), parent.getHeight());
        inputPanel.setBackground(new Color(255, 127, 127));
        inputPanel.setVisible(true);
            inputPanel.add(new JButton("Hello"));

        parent.add(inputPanel, BorderLayout.LINE_START);
        return parent;
    }
}
