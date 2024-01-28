package server;

import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final String PATH_TO_DB_FILE = "C:\\Java\\JSON Database (Java)\\JSON Database (Java)\\task\\out\\production\\classes\\server\\data\\db.json";

    private static final File DB_FILE = new File(PATH_TO_DB_FILE);

    private static final String INITIAL_DB_CONTENT;

    static {
        try {
            INITIAL_DB_CONTENT = new String(Files.readAllBytes(Paths.get(PATH_TO_DB_FILE)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<String, String> DB_MAP = new Gson().fromJson(INITIAL_DB_CONTENT, HashMap.class);

    static ExecutorService executorService;

    private static boolean isExit = false;

    public static void main(String[] args) throws IOException {

        String address = "127.0.0.1";
        int port = 23456;

        //create ServerSocket
        ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address));

        //set the time-out time 10 ms so that server could check isExit parameter every 10 ms
        server.setSoTimeout(10);

        System.out.println("Server started!");
        executorService = Executors.newFixedThreadPool(4);

        while (!isExit) {

            //client socket
            Socket socket;

            //accept connection from client within the time-out time
            try {
                socket = server.accept(); // accept a new client
            } catch (SocketTimeoutException e) {
                continue;
            }

            //create input/output streams for connection accepted
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            //create add execute requestHandler for client
            RequestHandler requestHandler = new RequestHandler(input, output, DB_MAP);
            executorService.execute(requestHandler);

        }

        //System.out.println("\nData base content:");
        //System.out.println(DB_MAP);
        executorService.shutdown();
        server.close();

    }

    //change isExit parameter for exit from main server listening loop
    public static void stopServer() {
        isExit = true;
        saveDatabaseToFile();
    }

    private static void saveDatabaseToFile() {
        String final_db_content = new Gson().toJson(DB_MAP, HashMap.class);
        try (FileWriter writer = new FileWriter(DB_FILE)) {
            writer.write(final_db_content);
        } catch (IOException e) {
            System.out.printf("An exception occurred %s", e.getMessage());
        }
    }


}
