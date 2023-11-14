package pw2;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

class ClientTCP {

  private static final String HOST = "localhost";
  private static final int PORT = 1234;
  private static final int CLIENT_ID = (int) (Math.random() * 1000000);
  private static final String TEXTUAL_DATA = "[#" + CLIENT_ID + "] Hello";

  public static void main(String args[]) {
    System.out.println("Connecting to the chat room via " + HOST + ":" + PORT);

    try (
      Socket socket = new Socket(HOST, PORT);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); //Inutilisé pour le moment
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    )
    {
      System.out.println("Successful connection");
      System.out.println("Your personnal ID is : " + CLIENT_ID);

      out.write(TEXTUAL_DATA + "\n");
      out.flush();

      User user = new User();
      Thread threadUser = new Thread(user);

      System.out.println("\nYou have left the chatroom.");
    }
    catch (IOException e) {
      System.out.println("[Client " + CLIENT_ID + "] exception: " + e);
    }
  }
}
