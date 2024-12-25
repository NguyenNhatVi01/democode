import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Scanner;

public class remoteClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            boolean exit = false;
            while (!exit) {
               
                System.out.println("\nMENU:");
                System.out.println("1. Shutdown");
                System.out.println("2. Restart");
                System.out.println("3. Cancel Shutdown/Restart");
                System.out.println("4. Screenshot");
                System.out.println("5. Enable/Disable Keylogger");
                System.out.println("6. Delete File");
                System.out.println("7. Copy File");
                System.out.println("8. Exit");
                System.out.print("Choose a command (1-8): ");
                int choice = sc.nextInt();
                sc.nextLine(); 

                switch (choice) {
                    case 1:
                        writer.println("shutdown");
                        System.out.println(reader.readLine());
                        break;
                    case 2:
                        writer.println("restart");
                        System.out.println(reader.readLine());
                        break;
                    case 3:
                        writer.println("cancel");
                        System.out.println(reader.readLine());
                        break;
                    case 4:
                        writer.println("screenshot");
                        System.out.println(reader.readLine());
                        break;
                    case 5:
                        writer.println("keylogger");
                        System.out.println(reader.readLine());
                        break;
                    case 6:
                        System.out.print("Enter the path of the file to delete: ");
                        String deletePath = sc.nextLine();
                        writer.println("deleteFile");
                        writer.println(deletePath);
                        System.out.println(reader.readLine());
                        break;
                    case 7:
                        System.out.print("Enter the source file path: ");
                        String sourcePath = sc.nextLine();
                        System.out.print("Enter the destination file path: ");
                        String destPath = sc.nextLine();
                        writer.println("copyFile");
                        writer.println(sourcePath);
                        writer.println(destPath);
                        System.out.println(reader.readLine());
                        break;
                    case 8:
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice!");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

