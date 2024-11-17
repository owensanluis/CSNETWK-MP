import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    private static Socket clientEndpoint;

    private static boolean handleCommands(String command) {
        DataOutputStream dosWriter;
        String[] parameters = command.split(" "); // split by spaces
        String sServerAddress;
        int nPort;

        // use try-catch to catch errors
        try {
            // if command is /join
            if(command.startsWith("/join")) {
                // check if client is already connected to a server
                if(clientEndpoint == null || clientEndpoint.isClosed()) {

                    //check if correct usage of the /join command
                    if (parameters.length != 3) {
                        System.out.println("Error: Command parameters do not match or is not allowed.");
                        return true;
                    }

                    // get the parameters of the command
                    sServerAddress = parameters[1];
                    nPort = Integer.parseInt(parameters[2]);

                    // try to connect to server
                    try {
                        // instantiate the socket, reader, and writer
                        clientEndpoint = new Socket(sServerAddress, nPort);
                        final DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());

                        System.out.println("Server: " + disReader.readUTF());

                        // listen to server responses in separate threads
                        // and allow program to continue if server does not have a response for readUTF.
                        new Thread(() -> getServerResponse(disReader)).start();

                    } catch(IOException e) { // print error message when connection failed
                        System.out.println("Error: Connection to the Server has failed! " +
                                            "Please check IP Address and Port Number.");
                    }
                } else {
                    // print an error message if client tries /join even if connected already
                    System.out.println("Error: Already connected to a server.");
                }
            }
            // if command is /leave
            else if (command.startsWith("/leave")) {
                // check if client is connected to a server
                if (clientEndpoint != null && !clientEndpoint.isClosed()) {
                    // check if the command usage is correct
                    // make sure dosWriter is not null so that it will not throw NullPointerException
                    if (parameters.length == 1) {
                        dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
                        dosWriter.writeUTF("/leave");
                        clientEndpoint.close();
                        return false;
                    } else {
                        // print error message if command usage is incorrect
                        System.out.println("Error: Command parameters do not match or is not allowed.");
                    }
                } else {
                    // print error message if client tries /leave without connecting to a server
                    System.out.println("Error: Disconnection failed. Please connect to the server first.");
                }
            }
            // if command is not /join and /leave AND client is connected to a server
            else if (clientEndpoint != null && !clientEndpoint.isClosed()) {
                dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
                dosWriter.writeUTF(command);
            }
            // if command is not /join and /leave AND client is NOT connected to a server
            else {
                System.out.println("Error: Please join a server first.");
            }
        } catch (IOException e) {
            // catch errors and print it
            System.out.println("Error: " + e.getMessage());
        }

        return true;
    }

    private static void getServerResponse(DataInputStream disReader) {
        // try-catch to catch incoming errors
        try {
            // loop to keep on reading server's response/message
            while (true) {
                // read server's message
                String serverResponse = disReader.readUTF();
                // print out the message
                System.out.println("Server: " + serverResponse);
            }
        } catch (IOException e) {
            // print error message
            System.out.println("Error: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input;
        boolean keepLooping = true;

        // loop to get all commands until /leave
        while(keepLooping) {
            System.out.println("Enter command: ");
            input = sc.nextLine().trim();

            keepLooping = handleCommands(input);
        }
    }
}
