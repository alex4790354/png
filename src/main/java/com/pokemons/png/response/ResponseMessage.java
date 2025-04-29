package com.pokemons.png.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.pokemons.png.request.Pokemon;
import java.util.List;

public class ResponseMessage {

    private String message;
    private String id;
    private String result;
    @JsonProperty("battle_limit")
    private String battleLimit;
    private String status;
    private String statusCode;
    private String trainer_name;
    private int level;

    private List<Pokemon> data;

    //////////////////
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getBattleLimit() {
        return battleLimit;
    }

    public void setBattleLimit(String battleLimit) {
        this.battleLimit = battleLimit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public List<Pokemon> getData() {
        return data;
    }

    public void setData(List<Pokemon> data) {
        this.data = data;
    }

    public String getTrainer_name() {
        return trainer_name;
    }

    public void setTrainer_name(String trainer_name) {
        this.trainer_name = trainer_name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
