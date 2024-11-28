import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    private static Socket clientEndpoint;
    private static boolean running = true;

    private static boolean handleCommands(String command) {
        DataOutputStream dosWriter;
        String[] parameters = command.split(" "); // split by spaces
        String sServerAddress;
        int nPort;

        // use try-catch to catch errors
        try {
            // if command is /join
            if (command.startsWith("/join")) {
                // check if client is already connected to a server
                if (clientEndpoint == null || clientEndpoint.isClosed()) {
                    if (parameters.length != 3) {
                        System.out.println("Error: Command parameters do not match or are not allowed.");
                        return true;
                    }

                    // try to connect to server
                    try {
                        // get the parameters of the command
                        sServerAddress = parameters[1];
                        nPort = Integer.parseInt(parameters[2]);

                        // instantiate the socket, reader, and writer
                        clientEndpoint = new Socket(sServerAddress, nPort);
                        final DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());
                        System.out.println("Server: " + disReader.readUTF());
                        System.out.println("Enter command:");

                        // listen to server responses in separate threads
                        // and allow program to continue if server does not have a response for readUTF.
                        new Thread(() -> getServerResponse(disReader)).start();
                    } catch (IOException e) {
                        System.out.println("Error: Connection to the Server failed! Please check IP Address and Port Number.");
                        return true;
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Command parameters do not match or are not allowed.");
                    }
                } else {
                    System.out.println("Error: Already connected to a server.");
                }
            }

            // Command: /send <file-path>
            else if (command.startsWith("/send")) {
                if (clientEndpoint != null && !clientEndpoint.isClosed()) {
                    if (parameters.length == 2) {
                        String filePath = parameters[1];
                        sendFileToServer(filePath);
                    } else {
                        System.out.println("Error: Command parameters do not match or are not allowed.");
                        return true;
                    }
                } else {
                    System.out.println("Error: Please join a server first.");
                    return true;
                }
            }

            // Command: /dir
            else if (command.startsWith("/dir")) {
                if (clientEndpoint != null && !clientEndpoint.isClosed()) {
                    dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
                    dosWriter.writeUTF("/dir");
                } else {
                    System.out.println("Error: Please join a server first.");
                }
            }

            // Command: /get <filename>
            else if (command.startsWith("/get")) {
                if (clientEndpoint != null && !clientEndpoint.isClosed()) {
                    if (parameters.length == 2) {
                        dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
                        dosWriter.writeUTF("/get " + parameters[1]);
                        receiveFile(parameters[1]); // Download the file
                    } else {
                        System.out.println("Error: Invalid syntax. Use /get <filename>.");
                    }
                } else {
                    System.out.println("Error: Please join a server first.");
                }
            }

            // Command: /leave
            else if (command.startsWith("/leave")) {
                if (clientEndpoint != null && !clientEndpoint.isClosed()) {
                    if (parameters.length == 1) {
                        dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
                        dosWriter.writeUTF("/leave");
                        running = false;
                        clientEndpoint.close();
                        return false;
                    } else {
                        System.out.println("Error: Command parameters do not match or are not allowed.");
                        return true;
                    }
                } else {
                    System.out.println("Error: Disconnection failed. Please connect to the server first.");
                    return true;
                }
            }

            else if (clientEndpoint != null && !clientEndpoint.isClosed()) {
                dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
                dosWriter.writeUTF(command);
            }

            // error if client is not connected
            else {
                System.out.println("Error: Please join a server first.");
                return true;
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return true;
        }

        return true;
    }

    private static void sendFileToServer(String filePath) {
        try {
            File file = new File(filePath);

            // check if the file exists
            if (!file.exists() || !file.isFile()) {
                System.out.println("Error: File does not exist or is not a valid file.");
                return;
            }

            // create a DataOutputStream to send the file
            DataOutputStream dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());

            // notify server about the /send command
            dosWriter.writeUTF("/send " + file.getName());
            dosWriter.flush();

            long fileSize = file.length();
            dosWriter.writeLong(fileSize);
            dosWriter.flush();

            // send file content
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) > 0) {
                dosWriter.write(buffer, 0, bytesRead);
            }
            dosWriter.flush();
            fileInputStream.close();

            System.out.println("File \"" + file.getName() + "\" sent to the server successfully.");
        } catch (IOException e) {
            System.out.println("Error while sending the file: " + e.getMessage());
        }
    }

    private static void receiveFile(String fileName) {
        try {
            DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());
            long fileSize = disReader.readLong(); // Read the file size

            if (fileSize > 0) {
                System.out.println("Receiving file: " + fileName + " (" + fileSize + " bytes)");
                FileOutputStream fos = new FileOutputStream(fileName);
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalRead = 0;

                while (totalRead < fileSize && (bytesRead = disReader.read(buffer)) > 0) {
                    fos.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }

                fos.close();
                System.out.println("File downloaded successfully: " + fileName);
            } else {
                System.out.println("Server response: File not found.");
            }
        } catch (IOException e) {
            System.out.println("Error receiving file: " + e.getMessage());
        }
    }

    private static void getServerResponse(DataInputStream disReader) {
        try {
            while (true) {
                String serverResponse = disReader.readUTF();
                synchronized (System.out) {
                    System.out.println("\nServer: " + serverResponse);
                    System.out.flush();
                    System.out.print("Enter command: ");
                }
            }
        } catch (IOException e) {
            if (running)
                System.out.println("Error: " + e.getMessage());
        }
    }

    private static boolean isConnected() {
        return clientEndpoint != null && !clientEndpoint.isClosed() && clientEndpoint.isConnected();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input;
        boolean keepLooping = true;

        while (keepLooping) {
            if (!isConnected()) {
                System.out.println("Enter command: ");
            }
            input = sc.nextLine().trim();
            keepLooping = handleCommands(input);
        }
    }
}
