package pw2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class User implements Runnable {
    public final static String FILE_PATH = "pw2/history.txt";
    private String username;
    private final Scanner message;
    private final int ID;
    private final BufferedWriter out;
    private final BufferedReader in;
    private boolean isReading;

    User(BufferedWriter out, int id, BufferedReader in) throws IOException {
        this.out = out;
        this.ID = id;
        try {
            this.in = new BufferedReader(new FileReader(FILE_PATH));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException("[#" + ID + "] Error opening history file : " + e);
        }

        isReading = true;
        message = new Scanner(System.in);
        username = "";

        changeUsername();
        out.write("USERNAME " + username + "\n"); //send the new username to the server
        out.flush();
        System.out.println(in.readLine());
        System.out.println("\nWelcome on the chat room " + username + " !\n");
        System.out.println("To see all available command, type: /help\n");
        run();
    }

    public void readFile() {
        String userInput = "";
        try {
            while ((in.readLine()) != null) { } // Passez toutes les lignes déjà présente
                while (isReading) {
                    userInput = in.readLine();
                    if (userInput != null) afficherNouveauMessage(userInput);
                }
        }
        catch (IOException e) {
            throw new RuntimeException("[#" + ID + "] Error reading history file : " + e);
        }
    }

    private void afficherNouveauMessage(String message) {
        // Déplacer le curseur vers le haut | Les deux lignes ci-dessous ont été réalisées par ChatGPT
        System.out.print("\033[2K"); // Efface la ligne actuelle
        System.out.print("\r"); // Place le curseur au début de la ligne

        System.out.println(message);
        System.out.print("> ");
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
                    try {
                        changeUsername();
                        out.write("USERNAME " + username + "\n"); //send the new username to the server
                        out.flush();

                        System.out.println(in.readLine());
                        System.out.print("> ");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "/online":
                    try {
                        out.write("ONLINE\n");
                        out.flush();

                        String serverResponse = in.readLine();

                        if (Integer.parseInt(serverResponse) > 1) {
                            System.out.println("There are currently " + serverResponse + " users online:\n");
                        } else {
                            System.out.println("There is currently " + serverResponse + " user online:\n");
                        }

                        // Read and display the online users
                        serverResponse = in.readLine();
                        while (!serverResponse.equals("END")) {
                            System.out.println(serverResponse);
                            serverResponse = in.readLine();
                        }
                        System.out.print("> ");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "/help":
                    System.out.println("\nAll available commands:");
                    System.out.println("\n\t/username : Change your username");
                    System.out.println("\n\t/online : See all connected users");
                    System.out.println("\n\t/quit : Quit the chat room\n");
                    System.out.print("> ");
                    break;
                case "":
                    System.out.print("\033[1A"); // Déplace le curseur vers le haut d'une ligne
                    System.out.print("\033[2K"); // Efface la ligne
                    System.out.print("> ");
                    break;
                default:
                    try {
                        System.out.print("\033[1A"); // Déplace le curseur vers le haut d'une ligne
                        System.out.print("\033[2K"); // Efface la ligne

                        out.write("[#" + ID + "] " + msg + "\n");
                        out.flush();
                    } catch (IOException e) {
                        throw new RuntimeException("[#" + ID + "] Error writing to history file" + e);
                    }
            }
        }
        isReading = false;
    }

    private void changeUsername() {
        do {
            System.out.println("\nPlease enter your username: ");
            this.username = message.nextLine();
        } while(username.isEmpty());
    }
}