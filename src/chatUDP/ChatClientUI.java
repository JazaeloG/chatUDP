package chatUDP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatClientUI {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private UDPClient client;

    public ChatClientUI(UDPClient client) {
        this.client = client;
        createUI();
    }

    private void createUI() {
        frame = new JFrame("Chat Client");
        chatArea = new JTextArea(20, 30);
        chatArea.setEditable(false);
        messageField = new JTextField(25);
        sendButton = new JButton("Send");

        JPanel panel = new JPanel();
        panel.add(new JScrollPane(chatArea));
        panel.add(messageField);
        panel.add(sendButton);

        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                client.sendMessage(message);
                messageField.setText("");
            }
        });
    }

    public void displayMessage(String message) {
        chatArea.append(message + "\n");
    }

    public static void main(String[] args) {
    }
}
