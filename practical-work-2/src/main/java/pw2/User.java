package pw2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Scanner;

public class User implements Runnable {
    private String username;
    private final Scanner message;
    private final int ID;
    BufferedWriter out;

    User(BufferedWriter out, int id) {
        this.out = out;
        this.ID = id;

        message = new Scanner(System.in);
        username = "";
        changeUsername();
        System.out.println("\nWelcome on the chat room " + username + "!\n");
        System.out.println("To see all available command, type: /help\n");
        run();
    }

    public void run() {
        String msg = "";
        while(!msg.equals("/quit")) {
            System.out.print("Your message: ");
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
                default:
                    try {
                       // System.out.println("Message : " + msg);
                        out.write("[#" + ID + "] " + msg + "\n");
                        out.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
            }
        }
    }

    private void changeUsername() {
        do {
            System.out.println("\nPlease enter your username: ");
            this.username = message.nextLine();
        } while(username.equals(""));
    }

}
