package pw2;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

class ClientTCP {

    private static final String HOST = "localhost";
    private static final int PORT = 1234;
    private static final String CLIENT_ERROR = "[ERROR] ";

    public static void main(String[] args) {
        System.out.println("Connecting to the chat room via " + HOST + ":" + PORT);
        try {
            Socket socket = new Socket(HOST, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            String serverResponse = in.readLine(); // Read the response from the server

            int CLIENT_ID;
            if (serverResponse.equals("Sorry, the chatroom is full!")) {
                System.out.println(serverResponse);
                socket.close();
                return;
            }
            else CLIENT_ID = Integer.parseInt(serverResponse);

            System.out.println("Successful connection");
            System.out.println("Your personal ID is : " + CLIENT_ID);

            User user = new User(out, CLIENT_ID, in);

            Thread threadUser = new Thread(user);
            threadUser.join();

            try {
                out.write("QUIT\n");  // Inform the server that the user is leaving
                out.flush();
            } catch (IOException e) {
                System.out.println(CLIENT_ERROR + "The response to the server could not be sent");
            }

            out.close();
            in.close();
            socket.close();

            System.out.println("\nYou have left the chatroom.");
        } catch (ConnectException e) {
            System.out.println(CLIENT_ERROR + "Unable to connect to the server. Please make sure the server is running.");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (InterruptedException e) {
            throw new RuntimeException(CLIENT_ERROR + "The thread was interrupted while in the waiting state");
        }
    }
}