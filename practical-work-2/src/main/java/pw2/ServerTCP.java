package pw2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerTCP {
    private static final int PORT = 1234;
    private static final int SERVER_ID = (int) (Math.random() * 1000000);
    public static String FILE_PATH = "pw2/history.txt"; //la mettre en const

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT);) {
            System.out.println(
                    "[Server " + SERVER_ID + "] starting with id " + SERVER_ID
            );
            System.out.println(
                    "[Server " + SERVER_ID + "] listening on port " + PORT
            );

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
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



                // Write the user input into the history file
                String userInput = in.readLine();

                try (BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\S3\\DAI\\practical-2\\practical-work-2\\src\\main\\java\\pw2\\history.txt", true))) {
                    writer.write(userInput + "\n"); // systems differents, pas \n
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println(
                        "[Server " + SERVER_ID + "] received textual data from client: " + userInput
                );


                System.out.println("[Server " + SERVER_ID + "] closing connection");
            } catch (IOException e) {
                System.out.println("[Server " + SERVER_ID + "] exception: " + e);
            }
        }
    }
}
