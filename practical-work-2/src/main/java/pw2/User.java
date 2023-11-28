package pw2;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class User implements Runnable {
    private final static String FILE_PATH = "pw2/history.txt";
    private String username;
    private Scanner message = null;
    private final int ID;
    private final BufferedWriter out;
    private final BufferedReader in;
    private final BufferedReader file;
    private boolean isReading;
    private final String CLIENT_ERROR = "[ERROR] ";

    User(BufferedWriter out, int id, BufferedReader in) throws IOException {
        this.out = out;
        this.ID = id;
        this.in = in;
        try {
            this.file = new BufferedReader(new InputStreamReader(new FileInputStream(FILE_PATH), StandardCharsets. UTF_8));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(CLIENT_ERROR + "History file not found");
        }
        isReading = true;
        message = new Scanner(System.in, StandardCharsets.UTF_8);
        username = "";

        changeUsername();
        out.write("USERNAME " + username + "\n"); // Send the new username to the server
        out.flush();
        in.readLine();
        System.out.println("\nWelcome on the chat room " + username + " !\n");
        System.out.println("To see all available command, type: /help\n");
        run();
    }

    public void readFile() {
        String userInput = "";
        try {
            while ((file.readLine()) != null) { } // Skip all lines already present
            while (isReading) {
                userInput = file.readLine();
                if (userInput != null) showNewMsg(userInput);
            }
        }
        catch (IOException e) {
            System.out.println(CLIENT_ERROR + "Unable to read history file");
        }
        System.exit(0);
    }

    private void showNewMsg(String msg) {
        String color = "";
        // Displays the message with a colour according to the ID
        switch (msg.charAt(2)) {
            case '0': color = "\u001B[31m"; break; // Red
            case '1': color = "\u001B[32m"; break; // Green
            case '2': color = "\u001B[33m"; break; // Yellow
            case '3': color = "\u001B[34m"; break; // Blue
            case '4': color = "\u001B[35m"; break; // Magenta
            case '5': color = "\u001B[36m"; break; // Cyan
            case '6': color = "\u001B[37m"; break; // White
            case '7': color = "\u001B[30m"; break; // Black
            case '8': color = "\u001B[90m"; break; // Light grey
            case '9': color = "\u001B[94m"; break; // Light blue
        }

        // Move cursor up | The two lines below were created by ChatGPT
        System.out.print("\033[2K"); // Deletes the current line
        System.out.print("\r"); // Places the cursor at the beginning of the line

        System.out.println(color + msg + "\u001B[37m"); // Change colour, display message, reset to white
        System.out.print("> ");
    }

    private void getHelp() {
        System.out.println("\nAll available commands:");
        System.out.println("\n\t/username : Change your username");
        System.out.println("\n\t/online : See all connected users");
        System.out.println("\n\t/quit : Quit the chat room\n");
        System.out.print("> ");
    }

    private void getOnline() {
        try {
            System.out.print("\033[1A"); // Moves the cursor to the top of a line
            System.out.print("\033[2K"); // Delete the line
            out.write("ONLINE\n");
            out.flush();

            String serverResponse = in.readLine();
            if (Integer.parseInt(serverResponse) > 1) showNewMsg("There are currently " + serverResponse + " users online :\n");
            else showNewMsg("There is currently " + serverResponse + " user online :\n");

            // Read and display the online users
            serverResponse = in.readLine();
            while (!serverResponse.equals("END")) {
                showNewMsg(serverResponse);
                serverResponse = in.readLine();
            }
        } catch (IOException e) {
            try {
                out.close();
            } catch (IOException ex) {
                System.out.println(CLIENT_ERROR + "There has been an issue while the writer");
            }
            System.out.println(CLIENT_ERROR + "The server response could not be read");
        }
    }

    private void sendMessage(String msg) {
        try {
            System.out.print("\033[1A"); // Déplace le curseur vers le haut d'une ligne
            System.out.print("\033[2K"); // Efface la ligne

            // Send the message to the server
            out.write("MSG [#" + ID + "] " + username + ": " + msg + "\n");
            out.flush();

            String serverResponse = in.readLine();
            if(!serverResponse.equals("RCV")) { // Only displays the server response if there is an error
               System.out.println(serverResponse);
               System.out.print("> ");
            }


        } catch (IOException e) {
            try {
                out.close();
            } catch (IOException ex) {
                System.out.println(CLIENT_ERROR + "There has been an issue while closing the writer");
            }
            System.out.println(CLIENT_ERROR + "Unable to write to history file");
        }
    }

    public void run() {
        Thread readFile = new Thread(this::readFile);
        readFile.start();
        String msg = "";
        System.out.print("> ");
        while(!msg.equals("/quit")) {
            msg = message.nextLine();
            switch (msg) {
                case "/username":
                    changeUsername();
                    try {
                        out.write("USERNAME " + username + "\n"); // Send the new username to the server
                        out.flush();
                        System.out.println(in.readLine());
                        System.out.print("> ");
                    } catch (IOException e) {
                        try {
                            out.close();
                        } catch (IOException ex) {
                            System.out.println(CLIENT_ERROR + "There has been an issue while the writer");
                        }
                        System.out.println(CLIENT_ERROR + "The response to the server could not be sent");
                    }
                    break;
                case "/online":
                    getOnline();
                    break;
                case "/help":
                    getHelp();
                    break;
                case "":
                    System.out.print("\033[1A"); // Moves the cursor to the top of a line
                    System.out.print("\033[2K"); // Delete the line
                    System.out.print("> ");
                    break;
                case "/quit":
                    break;
                default:
                    sendMessage(msg);
            }
        }
        isReading = false;
        try {
            readFile.join();
            out.close();
            in.close();
            file.close();
            message.close();
        } catch (InterruptedException e) {
            System.out.println(CLIENT_ERROR + "The thread was interrupted while in the waiting state");
        } catch (IOException e) {
            System.out.println(CLIENT_ERROR + "There has been an issue while closing the writer or reader");
        }
        System.exit(0);
    }

    private void changeUsername() {
        do {
            System.out.println("\nPlease enter your username : ");
            this.username = message.nextLine();
            username = username.replace(" ", "");
            if(username.isEmpty() || username.length() > 15)
                System.out.println("\nYour username must contain between 1 and 15 characters.");
        } while(username.isEmpty() || username.length() > 15);
    }
}