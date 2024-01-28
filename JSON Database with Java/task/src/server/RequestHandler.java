package server;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RequestHandler extends Thread implements Runnable {

    private final Map<String, String> dataBase;

    private final DataInputStream input;

    private final DataOutputStream output;

    private String serverAnswer;
    private final Map<String, String> answerToClient = new LinkedHashMap<>();


    public RequestHandler(DataInputStream input, DataOutputStream output,  Map<String, String> db) {
        this.input = input;
        this.output = output;
        this.dataBase = db;
    }


    @Override
    public void run() {

        HashMap<String, String> clientRequest;
        boolean stopDB = false;
        String msg; // read a message from the client

        try {
            msg = input.readUTF();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Received: " + msg);

        clientRequest = new Gson().fromJson(msg, HashMap.class);
        String commandType = clientRequest.get("type");

        switch (commandType) {
            case "exit"   : {stopDB = true; updateAnswerToClient("OK"); break;}
            case "delete" : {deleteOperation(clientRequest); break;}
            case "get"    : {getOperation(clientRequest); break;}
            case "set"    : {setOperation(clientRequest); break;}
            default       : System.out.println("No such command");
        }

        serverAnswer = new Gson().toJson(answerToClient);
        try {
            output.writeUTF(serverAnswer); // send the answer to the client
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Sent: " + serverAnswer);
        resetServerAnswer();

        if (stopDB) {
            Main.stopServer();
        }

    }

    private void resetServerAnswer() {
        serverAnswer = "";
    }

    private void setOperation(HashMap<String, String> request) {

        dataBase.put(request.get("key"), request.get("value"));
        updateAnswerToClient("OK");

    }

    private void updateAnswerToClient(String response) {
        answerToClient.clear();
        answerToClient.put("response", response);
    }

    private void updateAnswerToClientWithReason(String response, String reason) {
        updateAnswerToClient(response);
        answerToClient.put("reason", reason);
    }

    private void updateAnswerToClientWithValue(String response, String value) {
        updateAnswerToClient(response);
        answerToClient.put("value", value);
    }


    private void getOperation(HashMap<String, String> request) {

        if (dataBase.containsKey(request.get("key"))) {
            if ("".equals(dataBase.get(request.get("key")))) {
                updateAnswerToClientWithReason("ERROR", "No such key");
                //System.out.println("ERROR");
            } else {
                //System.out.println(DB_ARRAY[index - 1]);
                updateAnswerToClientWithValue("OK", dataBase.get(request.get("key")));
            }
        } else {
            updateAnswerToClientWithReason("ERROR", "No such key");
        }
    }

    private void deleteOperation(HashMap<String, String> request) {

        if (dataBase.containsKey(request.get("key"))) {
            dataBase.remove(request.get("key"));
            updateAnswerToClient("OK");
        } else {
            updateAnswerToClientWithReason("ERROR", "No such key");
        }
    }

}
