package pw2;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

class ClientTCP {

    private static final String HOST = "localhost";
    private static final int PORT = 1234;
    private static final int CLIENT_ID = 0;
    private static final String TEXTUAL_DATA = "[#" + CLIENT_ID + "] Hello";

    public static void main(String args[]) {
        System.out.println("Connecting to the chat room via " + HOST + ":" + PORT);
        try {
            Socket socket = new Socket(HOST, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            String serverResponse = in.readLine(); // Read the response from the server

            if (serverResponse != null && serverResponse.equals("Sorry, the chatroom is full!")) {
                System.out.println(serverResponse);
                socket.close();
                return;
            }

            System.out.println("Successful connection");
            System.out.println("Your personal ID is : " + CLIENT_ID);

            out.write(TEXTUAL_DATA + "\n");
            out.flush();

            User user = new User(out, CLIENT_ID);

            Thread threadUser = new Thread(user);

            //p-e un join??

            System.out.println("\nYou have left the chatroom.");
        } catch (ConnectException e) {
            System.out.println("Unable to connect to the server. Please make sure the server is running.");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
