package com.pokemons.png.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Pokemon {

    private String id;
    private String name;
    private String stage;
    @JsonProperty("photo_id")
    private int photoId;
    private int attack;
    @JsonProperty("trainer_id")
    private String trainerId;
    private int status;
    @JsonProperty("in_pokeball")
    private int inPokeball;

    public Pokemon() {};

    public Pokemon(String name, int photo_id) {
        this.name = name;
        this.photoId = photo_id;
    }

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

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public String getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(String trainerId) {
        this.trainerId = trainerId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getInPokeball() {
        return inPokeball;
    }

    public void setInPokeball(int inPokeball) {
        this.inPokeball = inPokeball;
    }
}
