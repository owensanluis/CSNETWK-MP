import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Server {

    private static void handleClient(Socket clientSocket) {
        // try-catch to catch any errors
        try {
            // instantiate variables
            DataInputStream disReader = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dosWriter = new DataOutputStream(clientSocket.getOutputStream());
            String input, cmd;
            String[] parameters;

            // write the message stating connection is successful
            dosWriter.writeUTF("Connection to the File Exchange Server is successful!");

            // get the inputs by client
            input = disReader.readUTF();

            // print inputs by client
            System.out.println("Server: Received \"" + input + "\" from the client.");

            // split the input by spaces
            parameters = input.split(" ");
            cmd = parameters[0];

            switch(cmd) {
                case "/leave":
                    dosWriter.writeUTF("Connection closed. Thank you!");
                    break;
                case "/register":
                    break;
                case "/store":
                    break;
                case "/dir":
                    break;
                case "/get":
                    break;
                case "/?":
                    System.out.println("\nList of commands:");
                    System.out.printf("%-30s - %s\n", "/join <server_ip_add> <port>", "Connect to the server application");
                    System.out.printf("%-30s - %s\n", "/leave", "Disconnect from the server application");
                    System.out.printf("%-30s - %s\n", "/register <handle>", "Register a unique handle or alias");
                    System.out.printf("%-30s - %s\n", "/store <filename>", "Send file to server");
                    System.out.printf("%-30s - %s\n", "/dir", "Request directory file list from a server");
                    System.out.printf("%-30s - %s\n\n", "/get <filename>", "Fetch a file from a server");
                    break;
                default:
                    dosWriter.writeUTF("Error: Command not found.\n");
                    break;

            }
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            System.out.println("Server: Connection is terminated");
        }
    }

    public static void main(String[] args) {
        ServerSocket serverSocket;
        Scanner sc = new Scanner(System.in);
        int nPort;
        boolean validInput = false;

        while(!validInput) {
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
