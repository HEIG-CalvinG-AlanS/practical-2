package pw2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;

public class ServerTCP {
    private static final int PORT = 1234;
    private static final int SERVER_ID = (int) (Math.random() * 1000000);
    private static final String SERVER_MESSAGE = "[Server " + SERVER_ID + "] ";
    private static final String FILE_PATH = "pw2/history.txt";
    private static final int CHATROOM_SIZE = 10;
    private static final String SERVER_ERROR = "[ERROR] ";
    private static final Map<Integer, String> idUsername = new HashMap<>();

    public static int firstAvailableID(Map<Integer, String> idUsernameMap) {
        for (int i = 0; i < CHATROOM_SIZE; i++) {
            if (!idUsernameMap.containsKey(i)) {
                return i;
            }
        }
        return -1;
    }

    public static void emptyHistory() throws IOException {
        OutputStreamWriter writer = null;
        try {
            // The false argument of OutputStreamWriter empties the file
            writer = new OutputStreamWriter(new FileOutputStream(FILE_PATH, false), StandardCharsets.UTF_8);
            System.out.println(SERVER_MESSAGE + "History reset");
        } catch (IOException e) {
            System.out.println(SERVER_ERROR + "History file not found");
        } finally {
            assert writer != null;
            writer.close();
        }
    }

    // Creates the loop that accepts/rejects upcoming connections
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
                        clientHandler.setID(firstAvailableID(idUsername)); // Choose an id for the new client
                        idUsername.put(clientHandler.getID(), "Anonymous");// Give the new client a temp name
                        outCheck.println(clientHandler.getID()); // Send the new client his ID
                        Thread clientThread = new Thread(clientHandler);
                        clientThread.start();
                    } else {
                        outCheck.println("Sorry, the chatroom is full !");
                        clientSocket.close();
                        System.out.println(SERVER_MESSAGE + "CHATROOM FULL");
                    }
                } catch (IOException e) {
                    System.out.println(SERVER_ERROR + "there has been an issue with the client connection " + e);
                }
            }
        } catch (IOException e) {
            System.out.println(SERVER_ERROR + "exception: " + e);
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
                    socket;
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    BufferedWriter out = new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
                    )
            ) {
                System.out.println(
                        SERVER_MESSAGE + "new client connected from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort()
                );

                System.out.println(
                        SERVER_MESSAGE + "number of online clients :" + idUsername.size()
                );

                String userInput;

                while (true) {
                    userInput = in.readLine();
                    String[] splitInput = userInput.split(" ");

                    if (userInput.equals("ONLINE")) {
                        // Send the number of connected clients
                        out.write(idUsername.size() + "\n");
                        out.flush();

                        // Send the list of every connected clients
                        for (Map.Entry<Integer, String> entry : idUsername.entrySet()) {
                            String key = String.valueOf(entry.getKey());
                            String value = entry.getValue();
                            out.write("ID : " + key + ", username : " + value + "\n");
                            out.flush();
                        }

                        out.write("END\n");
                        out.flush();
                    } else if (splitInput[0].equals("MSG")) {
                        BufferedWriter writer = null;
                        try {
                            writer = new BufferedWriter(new FileWriter(FILE_PATH, true));

                            // So that "MSG" is not present on the message
                            StringBuilder resultStringBuilder = new StringBuilder();
                            for (int i = 1; i < splitInput.length; i++) {
                                resultStringBuilder.append(splitInput[i]).append(" ");
                            }
                            String resultString = resultStringBuilder.toString();

                            writer.write(resultString + "\n");
                            writer.flush();
                        } catch (IOException e) {
                            System.out.println(SERVER_ERROR + " there has been an issue while writing the message");
                        } finally {
                            if (writer != null) writer.close();
                        }
                    } else if (splitInput[0].equals("USERNAME")) {
                        idUsername.put(clientID, splitInput[1]);
                        out.write("Your new username is " + splitInput[1] + "\n");
                        out.flush();
                    } else if (splitInput[0].equals("QUIT")) {
                        break;
                    }
                }
            } catch (IOException e) {
                if (!(e instanceof java.net.SocketException) && !(e.getMessage().equals("Connection reset"))) {
                    System.out.println(SERVER_ERROR + " The connection has been lost");
                }
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    System.out.println(SERVER_ERROR + " there has been an issue while closing the socket");
                }
            }

            idUsername.remove(this.getID());
            System.out.println(SERVER_MESSAGE + "closing connection");
            System.out.println(SERVER_MESSAGE + "number of online clients :" + idUsername.size());
        }
    }
}
