package client;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Request {

    private String type;
    private Object cellKey;

    private Object cellValue;

    private Map<String, Object> requestData;

    private String requestJson;

    public Request(String type, Object cellKey, Object cellValue) {
        this.type = type;
        this.cellKey = cellKey;
        this.cellValue = cellValue;
        buildRequest();
    }

    public Request(String type, Object cellKey) {
        this.type = type;
        this.cellKey = cellKey;
        this.cellValue = null;
        buildRequest();
    }

    public Request(String type) {
        this.type = type;
        this.cellKey = null;
        this.cellValue = null;
        buildRequest();
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

    public String getRequestJson() {
        return requestJson;
    }

}
