import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static final ConcurrentHashMap<String, ClientHandler> registeredHandles = new ConcurrentHashMap<>();
    private static final List<String> storedFiles = Collections.synchronizedList(new ArrayList<>());
    private static final String SERVER_STORAGE_DIR = "server_files"; // Directory for stored files

    // Ensure the server storage directory exists
    static {
        File storageDir = new File(SERVER_STORAGE_DIR);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            DataInputStream disReader = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dosWriter = new DataOutputStream(clientSocket.getOutputStream());

            String input, cmd;
            String[] parameters;
            String clientHandle = null;

            // Notify the client of a successful connection
            dosWriter.writeUTF("Connection to the File Exchange Server is successful!");

            while (true) {
                input = disReader.readUTF(); // Read client input
                System.out.println("Server: Received \"" + input + "\" from the client.");
                parameters = input.split(" ");
                cmd = parameters[0];

                switch (cmd) {
                    case "/leave":
                        dosWriter.writeUTF("Connection closed. Thank you!");
                        clientSocket.close();
                        return;

                    case "/register":
                        if (parameters.length == 2) {
                            clientHandle = parameters[1];
                            if (registeredHandles.containsKey(clientHandle)) {
                                dosWriter.writeUTF("Error: Registration failed. Handle or alias already exists.");
                            } else {
                                registeredHandles.put(clientHandle, new ClientHandler(clientSocket));
                                dosWriter.writeUTF("Welcome " + clientHandle + "!");
                            }
                        } else {
                            dosWriter.writeUTF("Error: Command parameters do not match or are not allowed.");
                        }
                        break;

                    case "/send":
                        if (clientHandle == null) {
                            dosWriter.writeUTF("Error: Please register first using /register <handle>.");
                            break;
                        }
                        if (parameters.length == 2) {
                            String filename = parameters[1];
                            receiveFile(disReader, filename);
                            storedFiles.add(filename);
                            dosWriter.writeUTF("File \"" + filename + "\" uploaded successfully.");
                        } else {
                            dosWriter.writeUTF("Error: Invalid syntax. Use /send <filename>.");
                        }
                        break;

                    case "/dir":
                        if (storedFiles.isEmpty()) {
                            dosWriter.writeUTF("Server Directory is empty.");
                        } else {
                            dosWriter.writeUTF("Server Directory:\n" + String.join("\n", storedFiles));
                        }
                        break;

                    case "/get":
                        if (parameters.length == 2) {
                            String filename = parameters[1];
                            if (storedFiles.contains(filename)) {
                                sendFile(dosWriter, filename);
                            } else {
                                dosWriter.writeUTF("Error: File \"" + filename + "\" not found on the server.");
                            }
                        } else {
                            dosWriter.writeUTF("Error: Invalid syntax. Use /get <filename>.");
                        }
                        break;

                    case "/?":
                        dosWriter.writeUTF("""
                                List of commands:
                                /join <server_ip_add> <port> - Connect to the server application
                                /leave                      - Disconnect from the server application
                                /register <handle>          - Register a unique handle or alias
                                /send <file-path>           - Send file to the server
                                /dir                        - Request directory file list from the server
                                /get <filename>             - Fetch a file from the server
                                """);
                        break;

                    default:
                        dosWriter.writeUTF("Error: Command not found.");
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            System.out.println("Server: Connection is terminated.");
        }
    }

    private static void receiveFile(DataInputStream disReader, String filename) throws IOException {
        File file = new File(SERVER_STORAGE_DIR, filename);

        long fileSize = disReader.readLong(); // Read the file size
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalRead = 0;

        System.out.println("Server: Receiving file \"" + filename + "\" (" + fileSize + " bytes)");

        while (totalRead < fileSize && (bytesRead = disReader.read(buffer)) > 0) {
            fos.write(buffer, 0, bytesRead);
            totalRead += bytesRead;
        }
        fos.close();

        System.out.println("Server: File \"" + filename + "\" received successfully.");
    }

    private static void sendFile(DataOutputStream dosWriter, String filename) throws IOException {
        File file = new File(SERVER_STORAGE_DIR, filename);

        if (!file.exists()) {
            dosWriter.writeUTF("Error: File \"" + filename + "\" not found.");
            return;
        }

        long fileSize = file.length();
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;

        dosWriter.writeUTF("Receiving file \"" + filename + "\" (" + fileSize + " bytes)");
        dosWriter.writeLong(fileSize);

        System.out.println("Server: Sending file \"" + filename + "\" (" + fileSize + " bytes)");

        while ((bytesRead = fis.read(buffer)) > 0) {
            dosWriter.write(buffer, 0, bytesRead);
        }
        fis.close();

        System.out.println("Server: File \"" + filename + "\" sent successfully.");
    }

    public static void main(String[] args) {
        ServerSocket serverSocket;
        Scanner sc = new Scanner(System.in);
        int nPort;
        boolean validInput = false;

        while (!validInput) {
            System.out.println("Enter port number: ");
            try {
                nPort = sc.nextInt();
                if (nPort >= 1024 && nPort <= 65535) {
                    try {
                        serverSocket = new ServerSocket(nPort);
                        validInput = true;
                        System.out.println("Server: Listening on port " + nPort + "...");

                        while (true) {
                            final Socket serverEndpoint = serverSocket.accept();
                            System.out.println("Server: New client connected: " +
                                    serverEndpoint.getRemoteSocketAddress());
                            new Thread(() -> handleClient(serverEndpoint)).start();
                        }
                    } catch (IOException e) {
                        System.out.println("Server: ERROR! Port " + nPort +
                                " is unavailable. Please try another port.\n");
                    }
                } else {
                    System.out.println("Invalid port number. Please enter a number between 1024 and 65535.\n");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter an integer.");
                sc.next(); // Clear invalid input
            }
        }
    }
}

class ClientHandler {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }
}
