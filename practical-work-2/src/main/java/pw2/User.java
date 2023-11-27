package pw2;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class User implements Runnable {
    public final static String FILE_PATH = "pw2/history.txt";
    private String username;
    private Scanner message = null;
    private final int ID;
    private final BufferedWriter out;
    private final BufferedReader in;
    private final BufferedReader file;
    private boolean isReading;

    User(BufferedWriter out, int id, BufferedReader in) throws IOException {
        this.out = out;
        this.ID = id;
        this.in = in;
        try {
            this.file = new BufferedReader(new InputStreamReader(new FileInputStream(FILE_PATH), StandardCharsets.
                                                                 
                                                                 _8));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException("[#" + ID + "] Error opening history file : " + e);
        }

        isReading = true;
        message = new Scanner(System.in, StandardCharsets.UTF_8);
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
            while ((file.readLine()) != null) { } // Passez toutes les lignes déjà présente
            while (isReading) {
                userInput = file.readLine();
                if (userInput != null) showNewMsg(userInput);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("[#" + ID + "] Error reading history file : " + e);
        }
    }

    private void showNewMsg(String msg) {
        String color = "";
        switch (msg.charAt(2)) {
            case '0': color = "\u001B[31m"; break; // Rouge
            case '1': color = "\u001B[32m"; break; // Vert
            case '2': color = "\u001B[33m"; break; // Jaune
            case '3': color = "\u001B[34m"; break; // Bleu
            case '4': color = "\u001B[35m"; break; // Magenta
            case '5': color = "\u001B[36m"; break; // Cyan
            case '6': color = "\u001B[37m"; break; // Blanc
            case '7': color = "\u001B[30m"; break; // Noir
            case '8': color = "\u001B[90m"; break; // Gris clair
            case '9': color = "\u001B[94m"; break; // Bleu clair
        }

        // Déplacer le curseur vers le haut | Les deux lignes ci-dessous ont été réalisées par ChatGPT
        System.out.print("\033[2K"); // Efface la ligne actuelle
        System.out.print("\r"); // Place le curseur au début de la ligne

        System.out.println(color + msg + "\u001B[37m"); // Modifie la couleur, affiche le message, remet en blanc
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
                        System.out.print("\033[1A"); // Déplace le curseur vers le haut d'une ligne
                        System.out.print("\033[2K"); // Efface la ligne

                        out.write("ONLINE\n");
                        out.flush();

                        String serverResponse = in.readLine();

                        if (Integer.parseInt(serverResponse) > 1) showNewMsg("There are currently " + serverResponse + " users online:\n");
                        else showNewMsg("There is currently " + serverResponse + " user online:\n");

                        // Read and display the online users
                        serverResponse = in.readLine();
                        while (!serverResponse.equals("END")) {
                            showNewMsg(serverResponse);
                            serverResponse = in.readLine();
                        }
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
                case "/quit":
                    break;
                default:
                    try {
                        System.out.print("\033[1A"); // Déplace le curseur vers le haut d'une ligne
                        System.out.print("\033[2K"); // Efface la ligne

                        out.write("MSG [#" + ID + "] " + msg + "\n");
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