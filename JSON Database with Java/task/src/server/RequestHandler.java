package server;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RequestHandler extends Thread implements Runnable {

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
    private static final LinkedTreeMap<String, Object> DB_MAP = new Gson().fromJson(INITIAL_DB_CONTENT, LinkedTreeMap.class);


    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();
    private static final Lock READ_LOCK = LOCK.readLock();
    private static final Lock WRITE_LOCK = LOCK.writeLock();

    private final DataInputStream input;

    private final DataOutputStream output;

    private String serverAnswer;
    private final Map<String, Object> answerToClient = new LinkedHashMap<>();


    public RequestHandler(DataInputStream input, DataOutputStream output) {
        this.input = input;
        this.output = output;
    }


    @Override
    public void run() {

        HashMap<String, Object> clientRequest;
        boolean stopDB = false; //stopping server flag
        String msg; // for client message

        //read a message from client
        try {
            msg = input.readUTF();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Received: " + msg);

        //convert client message to HashMap
        clientRequest = new Gson().fromJson(msg, HashMap.class);

        //get Request type
        String commandType = (String) clientRequest.get("type");

        //handle the client Request depending on its type
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


    private void deleteOperation(HashMap<String, Object> request) {

        WRITE_LOCK.lock();

        //differ two cases: key is ArrayList or String
        if (request.get("key") instanceof ArrayList<?>) {
            //handle the case if key is an ArrayList

            //get the sequence of keys
            ArrayList<String> keys = (ArrayList<String>) request.get("key");

            //the number of existing keys from the beginning of key array
            int existingKeysNumber = checkExistingKeysNumber(keys);
            int keySize = keys.size();

            if (existingKeysNumber == keySize) {
                //delete existing value by key

                //create and fill the Map with key-values pairs
                Map<String, Object> keyValues = unpackDatabaseObject(keys);

                //remove the deepest level accessed by key array
                keyValues.remove(keys.get(keySize - 1));

                //remove the deleted key-pair from the new deepest LinkedTreeMap
                Map<String, Object> updatedValue = (LinkedTreeMap<String, Object>) keyValues.get(keys.get(keySize - 2));
                updatedValue.remove(keys.get(keySize - 1));

                keys.remove(keySize - 1);

                DB_MAP.put(keys.get(0), getUpdatedValue(keyValues.get(keys.get(0)), keys, 0, keySize - 2, updatedValue));
                saveDatabaseToFile();

                updateAnswerToClient("OK");


            } else {
                updateAnswerToClientWithReason("ERROR", "No such key");
            }

        } else {
            //handle the case if key is a String
            if (DB_MAP.containsKey(request.get("key"))) {
                DB_MAP.remove(request.get("key"));
                saveDatabaseToFile();
                updateAnswerToClient("OK");
            } else {
                updateAnswerToClientWithReason("ERROR", "No such key");
            }
        }

        WRITE_LOCK.unlock();

    }

    private void setOperation(HashMap<String, Object> request) {

        WRITE_LOCK.lock();

        //differ two cases: key is ArrayList or String
        if (request.get("key") instanceof ArrayList<?>) {
            //handle the case if key is an ArrayList

            //get the sequence of keys
            ArrayList<String> keys = (ArrayList<String>) request.get("key");

            //create the Map of key-values pairs
            Map<String, Object> keyValues;

            //the number of key levels
            int keySize = keys.size();

            //the number of existing keys from the beginning of key array
            int existingKeysNumber = checkExistingKeysNumber(keys);

            if (existingKeysNumber == 0) {
                //case of totally new key

                keyValues = new LinkedTreeMap<>();
                addNewKeys(keyValues, keys, request, existingKeysNumber);

                DB_MAP.put(keys.get(0), keyValues.get(keys.get(0)));

                //check the result of SET operation
                if (Objects.nonNull(DB_MAP.get(keys.get(0)))) {
                    saveDatabaseToFile();
                    updateAnswerToClient("OK");
                } else {
                    updateAnswerToClient("SET operation failed");
                }

            } else if (existingKeysNumber == keySize) {
                //case of totally existing key
                keyValues = unpackDatabaseObject(keys);

                DB_MAP.put(keys.get(0), getUpdatedValue(keyValues.get(keys.get(0)), keys, 0, keySize - 1, request.get("value")));
                saveDatabaseToFile();
                updateAnswerToClient("OK");

            } else {
                //case of partially existing key

                keyValues = unpackDatabaseObject(keys, existingKeysNumber);
                addNewKeys(keyValues, keys, request, existingKeysNumber);
                updateKeyValuesMap(keyValues, keys, existingKeysNumber);

                DB_MAP.put(keys.get(0), keyValues.get(keys.get(0)));

                //check the result of SET operation
                if (Objects.nonNull(DB_MAP.get(keys.get(0)))) {
                    saveDatabaseToFile();
                    updateAnswerToClient("OK");
                } else {
                    updateAnswerToClient("SET operation failed");
                }

            }


        } else {
            //handle the case if key is a String
            String cellKey = (String) request.get("key");

            DB_MAP.put(cellKey, request.get("value"));

            //check the result of SET operation
            if (Objects.nonNull(DB_MAP.get(cellKey))) {
                saveDatabaseToFile();
                updateAnswerToClient("OK");
            } else {
                updateAnswerToClient("SET operation failed");
            }
        }

        WRITE_LOCK.unlock();

    }

    private void updateKeyValuesMap(Map<String, Object> keyValues, ArrayList<String> keys, int num) {

        Map<String, Object> mapEntry;

        for (int index = num - 1; index >= 0; index --) {
            if (keyValues.get(keys.get(index)) instanceof LinkedTreeMap<?,?>) {
                mapEntry = (LinkedTreeMap<String, Object>) keyValues.get(keys.get(index));
                mapEntry.put(keys.get(index + 1), keyValues.get(keys.get(index + 1)));
            } else {
                Map<String, Object> mapObject = new LinkedTreeMap<>();
                mapObject.put(keys.get(index + 1), keyValues.get(keys.get(index + 1)));
                keyValues.put(keys.get(index), mapObject);
            }
        }

    }

    private void addNewKeys(Map<String, Object> keyValues, ArrayList<String> keys, HashMap<String, Object> request, int existingKeysNumber) {
        int index = keys.size() - 1;
        Map<String, Object> mapEntry = new LinkedTreeMap<>();

        //put the deepest object to keyValues map
        keyValues.put(keys.get(index), request.get("value"));

        //construct the deepest object with value from request
        mapEntry.put(keys.get(index), request.get("value"));

        Object moreDeepObject = mapEntry;
        Map<String, Object> lessDeepObject;

        while (index > existingKeysNumber) {

            index--;
            lessDeepObject = new LinkedTreeMap<>();
            keyValues.put(keys.get(index), moreDeepObject);
            lessDeepObject.put(keys.get(index), moreDeepObject);
            moreDeepObject = lessDeepObject;

        }
    }

    //Method unpacks database object for totally existing key
    private Map<String, Object> unpackDatabaseObject(ArrayList<String> keys) {

        Map<String, Object> resultMap = new LinkedTreeMap<>();
        Map<String, Object> mapEntry;
        int index = 0;

        //get the first key from keys array
        String key = keys.get(index);

        //get existing Object from database
        Object lessDeepObject = DB_MAP.get(key);

        //convert database object to LinkedTreeMap object
        mapEntry = (LinkedTreeMap<String, Object>) lessDeepObject;

        //put converted object to resultMap
        resultMap.put(key, mapEntry);

        //unpack nested objects in the loop
        Object moreDeepObject;

        for (index = 1; index < keys.size() - 1; index++) {
            key = keys.get(index);
            moreDeepObject = mapEntry.get(key);
            mapEntry = (LinkedTreeMap<String, Object>) moreDeepObject;
            resultMap.put(key, mapEntry);
        }

        return resultMap;

    }

    //Method unpacks database object for partially existing key
    private Map<String, Object> unpackDatabaseObject(ArrayList<String> keys, int num) {

        Map<String, Object> resultMap = new LinkedTreeMap<>();
        Map<String, Object> mapEntry;
        int index = 0;

        //get the first key from keys array
        String key = keys.get(index);

        //get existing Object from database
        Object lessDeepObject = DB_MAP.get(key);

        //convert database object to LinkedTreeMap object
        mapEntry = (LinkedTreeMap<String, Object>) lessDeepObject;

        //put converted object to resultMap
        resultMap.put(key, mapEntry);

        //unpack nested objects in the loop
        Object moreDeepObject;

        for (index = 1; index < num - 1; index++) {
            key = keys.get(index);
            moreDeepObject = mapEntry.get(key);
            mapEntry = (LinkedTreeMap<String, Object>) moreDeepObject;
            resultMap.put(key, mapEntry);
        }

        //get the last nested object which value can be either String or LinkedTreeMap
        key = keys.get(num - 1);
        moreDeepObject = mapEntry.get(key);
        resultMap.put(key, moreDeepObject);

        return resultMap;

    }

    //Method checks how many first keys really exist in database entries
    private int checkExistingKeysNumber(ArrayList<String> keys) {

        //index of the key of keys array checked
        int index = 0;

        //get the first object from the database using first key of keys array
        Object value = DB_MAP.get(keys.get(index));
        //The Map for nested maps
        Map<String, Object> nestedObject;

        while (Objects.nonNull(value)) {
            index++;
            if (value instanceof LinkedTreeMap<?,?> && index < keys.size()) {
                nestedObject = (LinkedTreeMap<String, Object>) value;
                value = nestedObject.get(keys.get(index));
            } else {break;}
        }

        return index;
    }

    private Object getUpdatedValue (Object nestedMap, ArrayList<String> keys, int index, int maxIndex, Object value) {

        int mindex = index;

        Map<String, Object> methodNestedMap = (LinkedTreeMap<String, Object>) nestedMap;

        Map<String, Object> nextMethodNestedMap;

        if (mindex < maxIndex - 1) {
            mindex++;
            nextMethodNestedMap = (LinkedTreeMap<String, Object>) methodNestedMap.get(keys.get(mindex));
            methodNestedMap.put(keys.get(mindex), getUpdatedValue(nextMethodNestedMap, keys, mindex, maxIndex, value));
        } else {
            mindex++;
            methodNestedMap.put(keys.get(mindex), value);
        }

        return methodNestedMap;
    }

    private void getOperation(HashMap<String, Object> request) {

        READ_LOCK.lock();

        //determine if key is ArrayList and differ two cases: ArrayList and String
        if (request.get("key") instanceof ArrayList<?>) {
            //handle the case if key is an ArrayList
            ArrayList<String> keys = (ArrayList<String>) request.get("key");

            Object value = DB_MAP.get(keys.get(0));
            Object nextValue;
            int keyIndex = 1;

            while (keyIndex < keys.size()) {

                if (value instanceof LinkedTreeMap<?,?>) {
                    Map<String, Object> mapValue = (LinkedTreeMap<String, Object>) value;
                    nextValue = mapValue.get(keys.get(keyIndex));
                } else {
                    updateAnswerToClientWithReason("ERROR", "No such key");
                    break;
                }

                value = nextValue;
                keyIndex++;
            }

            if (keyIndex == keys.size() && Objects.nonNull(value)) {
                updateAnswerToClientWithValue("OK", value);
            } else {
                updateAnswerToClientWithReason("ERROR", "No such key");
            }

        } else {

            //handle the case if key is a String
            if (DB_MAP.containsKey(request.get("key"))) {
                if ("".equals(DB_MAP.get(request.get("key")))) {
                    updateAnswerToClientWithReason("ERROR", "No such key");
                    //System.out.println("ERROR");
                } else {
                    //System.out.println(DB_ARRAY[index - 1]);
                    updateAnswerToClientWithValue("OK", DB_MAP.get(request.get("key")));
                }
            } else {
                updateAnswerToClientWithReason("ERROR", "No such key");
            }
        }

        READ_LOCK.unlock();

    }

    private void resetServerAnswer() {
        serverAnswer = "";
    }

    private void updateAnswerToClient(String response) {
        answerToClient.clear();
        answerToClient.put("response", response);
    }

    private void updateAnswerToClientWithReason(String response, String reason) {
        updateAnswerToClient(response);
        answerToClient.put("reason", reason);
    }

    private void updateAnswerToClientWithValue(String response, Object value) {
        updateAnswerToClient(response);
        answerToClient.put("value", value);
    }

    private static void saveDatabaseToFile() {
        String db_content = new Gson().toJson(DB_MAP, LinkedTreeMap.class);
        try (FileWriter writer = new FileWriter(DB_FILE)) {
            writer.write(db_content);
        } catch (IOException e) {
            System.out.printf("An exception occurred %s", e.getMessage());
        }
    }

}
