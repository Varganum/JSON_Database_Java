package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {

    private static boolean isExit = false;

    public static void main(String[] args) throws IOException {

        String address = "127.0.0.1";
        int port = 23456;

        //create ServerSocket
        ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address));

        //set the time-out time 10 ms so that server could check isExit parameter every 10 ms
        server.setSoTimeout(10);

        System.out.println("Server started!");
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        //client socket
        Socket socket;

        while (!isExit) {

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
            RequestHandler requestHandler = new RequestHandler(input, output);
            executorService.execute(requestHandler);

        }

        executorService.shutdown();
        server.close();

    }

    //change isExit parameter for exit from main server listening loop
    public static void stopServer() {
        isExit = true;
    }

}
