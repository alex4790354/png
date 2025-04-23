package com.pokemons.png.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Pokemon {

    private String id;
    private String name;
    private String stage;
    @JsonProperty("photo_id")
    private int photo_id;
    private int attack;
    @JsonProperty("trainer_id")
    private String trainer_id;
    private int status;
    @JsonProperty("in_pokeball")
    private int in_pokeball;

    /// ////////////

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public int getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(int photo_id) {
        this.photo_id = photo_id;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public String getTrainer_id() {
        return trainer_id;
    }

    public void setTrainer_id(String trainer_id) {
        this.trainer_id = trainer_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getIn_pokeball() {
        return in_pokeball;
    }

    public void setIn_pokeball(int in_pokeball) {
        this.in_pokeball = in_pokeball;
    }
}
