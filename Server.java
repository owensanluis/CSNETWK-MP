import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static final ConcurrentHashMap<String, ClientHandler> registeredHandles = new ConcurrentHashMap<>();
    private static final List<String> storedFiles = Collections.synchronizedList(new ArrayList<>());

    private static void handleClient(Socket clientSocket) {
        try {
            DataInputStream disReader = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dosWriter = new DataOutputStream(clientSocket.getOutputStream());

            String input, cmd;
            String[] parameters;
            String clientHandle = null;

            // notify the client of successful connection
            dosWriter.writeUTF("Connection to the File Exchange Server is successful!");

            while (true) {
                input = disReader.readUTF(); // read client input
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

                    case "/store":
                        if (clientHandle == null) {
                            dosWriter.writeUTF("Error: Please register first using /register <handle>.");
                            break;
                        }
                        if (parameters.length == 2) {
                            String filename = parameters[1];
                            storedFiles.add(filename);
                            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                            dosWriter.writeUTF(clientHandle + "<" + timestamp + ">: Uploaded " + filename);
                        } else {
                            dosWriter.writeUTF("Error: Invalid syntax. Use /store <filename>.");
                        }
                        break;

                    case "/dir":
                        if (storedFiles.isEmpty()) {
                            dosWriter.writeUTF("Server Directory is empty.");
                        } else {
                            dosWriter.writeUTF("Server Directory\n" + String.join("\n", storedFiles));
                        }
                        break;

                    case "/get":
                        if (parameters.length == 2) {
                            String filename = parameters[1];
                            if (storedFiles.contains(filename)) {
                                dosWriter.writeUTF("File received from Server: " + filename);
                            } else {
                                dosWriter.writeUTF("Error: File " + filename + " not found on the server.");
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
                                /store <filename>           - Send file to server
                                /dir                        - Request directory file list from server
                                /get <filename>             - Fetch a file from server
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
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer.");
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
