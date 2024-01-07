package client;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Request {

    private String type;
    private String cellKey;

    private String cellValue;

    private Map<String, String> requestData;

    private String requestJson;

    public Request(String type, String cellKey, String cellValue) {
        this.type = type;
        this.cellKey = cellKey;
        this.cellValue = cellValue;
        buildRequest();
    }

    public Request(String type, String cellKey) {
        this.type = type;
        this.cellKey = cellKey;
        this.cellValue = null;
    }

    public Request(String type) {
        this.type = type;
        this.cellKey = null;
        this.cellValue = null;
    }

    private void buildRequest() {
        requestData = new HashMap<>();
        requestData.put("type", this.type);
        if (Objects.nonNull(this.cellKey)) {
            requestData.put("key", this.cellKey);
        }
        if (Objects.nonNull(this.cellValue)) {
            requestData.put("value", this.cellValue);
        }
        this.requestJson = new Gson().toJson(requestData);
    }

    public void printRequestJson() {
        System.out.println(this.requestJson);
    }

    public String getRequestJson() {
        return requestJson;
    }

}
