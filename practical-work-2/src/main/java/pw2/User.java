package pw2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Scanner;

public class User implements Runnable {
    private String username;
    private final Scanner message;
    private final int ID;
    BufferedWriter out;
    private final BufferedReader in;

    User(BufferedWriter out, int id, BufferedReader in) throws IOException {
        this.out = out;
        this.ID = id;
        this.in = in;

        message = new Scanner(System.in);
        username = "";

        changeUsername();
        out.write("USERNAME " + username + "\n"); //send the new username to the server
        out.flush();
        System.out.println(in.readLine());


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
                    try {
                        changeUsername();
                        out.write("USERNAME " + username + "\n"); //send the new username to the server
                        out.flush();

                        System.out.println(in.readLine());
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
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "/help":
                    System.out.println("\nAll available commands:");
                    System.out.println("\n\t/username : Change your username");
                    System.out.println("\n\t/online : See all connected users");
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
        } while(username.isEmpty());
    }

}
