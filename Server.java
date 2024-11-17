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

import java.util.concurrent.ConcurrentHashMap;

private static ConcurrentHashMap<String, ClientHandler> registeredHandles = new ConcurrentHashMap<>();

@Override
public void run() {
    try {
        while (true) {
            String command = inputStream.readUTF();
            
            if (command.startsWith("/register")) {
                String[] parts = command.split(" ");
                if (parts.length == 2) {
                    String handle = parts[1];
                    
                    if (registeredHandles.containsKey(handle)) {
                        outputStream.writeUTF("Error: Registration failed. Handle or alias already exists.");
                    } else {
                        registeredHandles.put(handle, this);
                        outputStream.writeUTF("Welcome " + handle + "!");
                    }
                } else {
                    outputStream.writeUTF("Error: Command parameters do not match or are not allowed.");
                }
                outputStream.flush();
            }
            
        }
    } catch (IOException e) {
        System.out.println("Client disconnected.");
    }
}

// List of valid commands with their descriptions
const commands = {
    "/?": "Displays all available commands.",
    "/help": "Alias for /?, provides help information.",
    "/upload": "Upload a file. Syntax: /upload <file-path>",
    "/download": "Download a file. Syntax: /download <file-name>",
    "/list": "Lists all available files on the server.",
    "/delete": "Delete a file. Syntax: /delete <file-name>",
};

// DOM Elements for interaction
const commandInput = document.getElementById("command-input"); // Input field for commands
const sendButton = document.getElementById("send-button"); // Send button
const outputArea = document.getElementById("output-area"); // Output display area

/**
 * Appends a message to the output area.
 * @param {string} message - The message to display.
 * @param {boolean} isError - Whether the message is an error.
 */
function printMessage(message, isError = false) {
    const messageElement = document.createElement("div"); // Create a new message div
    messageElement.textContent = message; // Set the message text
    messageElement.style.color = isError ? "red" : "black"; // Red for errors, black otherwise
    outputArea.appendChild(messageElement); // Add to output area
    outputArea.scrollTop = outputArea.scrollHeight; // Auto-scroll to latest message
}

/**
 * Displays the help message with all available commands.
 */
function showHelp() {
    printMessage("Available Commands:");
    for (const [cmd, description] of Object.entries(commands)) {
        printMessage(`${cmd}: ${description}`); // Show each command and its description
    }
}

/**
 * Processes and validates the input command.
 * @param {string} input - The command entered by the user.
 */
function processCommand(input) {
    const args = input.split(" "); // Split input into command and parameters
    const command = args[0]; // Extract the command
    const params = args.slice(1); // Extract the parameters

    // Check if the command is valid
    if (commands[command]) {
        if (command === "/?" || command === "/help") {
            showHelp(); // Display help for /? or /help
        } else {
            // General feedback for other commands
            printMessage(`Command recognized: ${command}`);
            if (params.length > 0) {
                printMessage(`Parameters provided: ${params.join(" ")}`);
            } else {
                printMessage("No parameters provided.");
            }
        }
    } else {
        // Invalid command error
        printMessage("Error: Command not found.", true);
    }
}

/**
 * Handles the click event of the Send button.
 */
function handleSendButton() {
    const input = commandInput.value.trim(); // Get the user input and trim spaces
    if (input) {
        processCommand(input); // Process the command
    }
    commandInput.value = ""; // Clear the input field after submission
}

/**
 * Handles the Enter key press event in the input field.
 * @param {KeyboardEvent} event - The keydown event.
 */
function handleEnterKey(event) {
    if (event.key === "Enter") {
        handleSendButton(); // Trigger send button behavior
    }
}

// Attach event listeners for interaction
sendButton.addEventListener("click", handleSendButton); // Click on the Send button
commandInput.addEventListener("keydown", handleEnterKey); // Press Enter in the input box



