package pw2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;

public class ServerTCP {
    private static final int PORT = 1234;
    private static final int SERVER_ID = (int) (Math.random() * 1000000);// si port fixe, id sert a rien
    private static final String SERVER_MESSAGE = "[Server " + SERVER_ID + "] ";
    public static final String FILE_PATH = "pw2/history.txt";
    public static int CHATROOM_SIZE = 3;
    private static final Map<Integer, String> idUsername = new HashMap<>();

    public static int firstAvailableID(Map<Integer, String> idUsernameMap) {
        for (int i = 0; i < CHATROOM_SIZE; i++) {
            if (!idUsernameMap.containsKey(i)) {
                return i;
            }
        }
        return -1;
    }

    public static void emptyHistory() {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(FILE_PATH, false), StandardCharsets.UTF_8))) {
            System.out.println(SERVER_MESSAGE + "History reseted");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(SERVER_MESSAGE + "starting with id " + SERVER_ID);
            System.out.println(SERVER_MESSAGE + "listening on port " + PORT);
            emptyHistory();

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    PrintWriter outCheck = new PrintWriter(clientSocket.getOutputStream(), true);
                    if (idUsername.size() < CHATROOM_SIZE) {
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        clientHandler.setID(firstAvailableID(idUsername));
                        idUsername.put(clientHandler.getID(), "Anonymous");
                        outCheck.println(clientHandler.getID()); //Send the new client his ID
                        Thread clientThread = new Thread(clientHandler);
                        clientThread.start();
                    } else {
                        outCheck.println("Sorry, the chatroom is full!");
                        clientSocket.close();
                        System.out.println("CHATROOM FULL");
                    }
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.out.println(SERVER_MESSAGE + "exception: " + e);
        }
    }

    static class ClientHandler implements Runnable {

        private final Socket socket;
        private int clientID;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public int getID() {
            return clientID;
        }

        public void setID(int i) {
            clientID = i;
        }

        @Override
        public void run() {
            try (
                    socket; // This allows to use try-with-resources with the socket
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
                        SERVER_MESSAGE + "new client connected from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort()
                );

                System.out.println(
                        SERVER_MESSAGE + "number of online clients :" + idUsername.size()
                );

                String userInput = "";

                while (!userInput.equals("QUIT")) {
                    // Write the user input into the history file
                    userInput = in.readLine();
                    String[] splittedInput = userInput.split(" ");

                    if (userInput.equals("ONLINE")) {
                        out.write(idUsername.size() + "\n");
                        out.flush();

                        for (Map.Entry<Integer, String> entry : idUsername.entrySet()) {
                            String key = String.valueOf(entry.getKey());
                            String value = entry.getValue();
                            out.write("ID : " + key + ", username : " + value + "\n");
                            out.flush();
                        }

                        out.write("END\n");
                        out.flush();
                        break;
                    }
                    else if (splittedInput[0].equals("USERNAME")) {
                        idUsername.put(clientID, splittedInput[1]);
                        out.write("Your new username is " + splittedInput[1] + "\n");
                        out.flush();
                    }


                    try (BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(FILE_PATH, true), StandardCharsets.UTF_8))) {
                        if (userInput != null && userInput.charAt(0) == '[') {
                            fileWriter.write(userInput + "\n"); // systems differents, pas \n
                            fileWriter.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            } catch (IOException e) {
                if (e instanceof java.net.SocketException && e.getMessage().equals("Connection reset")) {

                } else {
                    e.printStackTrace();
                }
            }

            idUsername.remove(this.getID());

            System.out.println(SERVER_MESSAGE + "closing connection");
            System.out.println(SERVER_MESSAGE + "number of online clients :" + idUsername.size());
        }
    }
}
