package pw2;

import java.io.*;
import java.util.Scanner;

public class User implements Runnable {
    public final static String FILE_PATH = "pw2/history.txt";
    private String username;
    private final Scanner message;
    private final int ID;
    BufferedReader in;
    BufferedWriter out;
    private boolean isReading;

    User(BufferedWriter out, int id) {
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
        System.out.println("\nWelcome on the chat room " + username + " !\n");
        System.out.println("To see all available command, type : /help\n");
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
                    changeUsername();
                    System.out.println("\nYour new username is : " + username + ".\n");
                    break;
                case "/online":
                    System.out.println("\nAll connected people:\n");
                    break;
                case "/help":
                    System.out.println("\nAll available commands:");
                    System.out.println("\n\t/username : Change your username");
                    System.out.println("\n\t/online : See all connected people");
                    System.out.println("\n\t/quit : Quit the chat room\n");
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
        } while(username.equals(""));
    }
}