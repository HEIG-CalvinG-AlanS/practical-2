package pw2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List; // peut etre maven dependency????

public class ServerTCP {
    private static final int PORT = 1234;
    private static final int SERVER_ID = (int) (Math.random() * 1000000);
    public static String FILE_PATH = "pw2/history.txt"; //la mettre en const
    public static int CHATROOM_SIZE = 10;
    private static List<ClientHandler> onlineUsers;

    private static synchronized void addClientHandler(ClientHandler clientHandler) {
        if (onlineUsers.size() < CHATROOM_SIZE) {
            onlineUsers.add(clientHandler);
        } else {
            System.out.println("La chatroom est pleine. Impossible d'ajouter de nouveaux utilisateurs.");
            // Vous pouvez ajouter une logique supplémentaire ici si nécessaire
        }
    }

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT);) {

            System.out.println(
                    "[Server " + SERVER_ID + "] starting with id " + SERVER_ID
            );
            System.out.println(
                    "[Server " + SERVER_ID + "] listening on port " + PORT
            );

            onlineUsers = new ArrayList<>();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                addClientHandler(clientHandler);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }

        } catch (IOException e) {
            System.out.println("[Server " + SERVER_ID + "] exception: " + e);
        }
    }


    static class ClientHandler implements Runnable {

        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    socket; // This allow to use try-with-resources with the socket
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                    );
                    BufferedWriter out = new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream(),
                                    StandardCharsets.UTF_8
                            )
                    )
            ) {
                System.out.println(
                        "[Server " + SERVER_ID + "] new client connected from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort()
                );
                System.out.println(
                        "[Server " + SERVER_ID + "] number of online clients :" + onlineUsers.size()
                );


                // Send the last written line from the history
                String lastLine = "";

                BufferedReader br = null;
                try {
                    String sCurrentLine;

                    br = new BufferedReader(new FileReader(FILE_PATH));

                    while ((sCurrentLine = br.readLine()) != null) {
                        lastLine = sCurrentLine;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (br != null) br.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                out.write(lastLine + "\n"); // sert a envoyer lastline au client
                out.flush();

                String userInput = "";

                while(userInput != null) {
                    // Write the user input into the history file
                    userInput = in.readLine();

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
                        if(userInput != null && userInput.charAt(5) != '/') writer.write(userInput + "\n"); // systems differents, pas \n
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                  
            } catch (IOException e) {
                System.out.println("[Server " + SERVER_ID + "] exception: " + e);
            }
            System.out.println("[Server " + SERVER_ID + "] closing connection");
        }
    }
}
