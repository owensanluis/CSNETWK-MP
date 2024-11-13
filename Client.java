import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    private static void handleCommands(String command) {
        String[] parameters = command.split(" "); //split by spaces
        String cmd = parameters[0];

        switch(cmd) {
            case "/join":
                break;
            case "/leave":
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
                System.out.println("Error: Command not found.\n");
                break;

        }
    }


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input;

        while(true) {
            System.out.println("Enter command: ");
            input = sc.nextLine().trim();

            handleCommands(input);
        }
    }
}
