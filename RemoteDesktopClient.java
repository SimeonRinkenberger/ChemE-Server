import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class RemoteDesktopClient extends JFrame {

    private JLabel screenLabel;

    public RemoteDesktopClient() {
        super("Remote Desktop Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        screenLabel = new JLabel();
        add(new JScrollPane(screenLabel));
    }

    public void connectToServer(String serverAddress) throws IOException {
        try (Socket socket = new Socket(serverAddress, 9999)) {
            InputStream inputStream = socket.getInputStream();

            while (true) {
                // Read image size
                byte[] sizeAr = new byte[4];
                inputStream.read(sizeAr);
                int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

                // Read image bytes
                byte[] imageAr = new byte[size];
                int bytesRead = 0;
                while (bytesRead < size) {
                    int result = inputStream.read(imageAr, bytesRead, size - bytesRead);
                    if (result == -1) break;
                    bytesRead += result;
                }

                // Convert bytes to ImageIcon and set it to the label
                ImageIcon imageIcon = new ImageIcon(imageAr);
                screenLabel.setIcon(imageIcon);
                pack(); // Resize the window to fit the image
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Disconnected from the server.", "Connection Lost", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                RemoteDesktopClient client = new RemoteDesktopClient();
                client.setVisible(true);
                String serverAddress = JOptionPane.showInputDialog("Enter server IP address:");
                if (serverAddress != null && !serverAddress.trim().isEmpty()) {
                    client.connectToServer(serverAddress.trim());
                } else {
                    JOptionPane.showMessageDialog(client, "Invalid IP Address.", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Could not connect to server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
