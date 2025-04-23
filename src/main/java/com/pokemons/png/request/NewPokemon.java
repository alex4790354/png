package com.pokemons.png.request;


import com.fasterxml.jackson.annotation.JsonProperty;

public class NewPokemon {

    private final String name;
    @JsonProperty("photo_id")
    private final int photoId;

    public NewPokemon(String name, int photoId) {
        this.name = name;
        this.photoId = photoId;
    }

    @Override
    public String toString() {
        return String.format("{ \"name\": \"%s\", \"photo_id\": %s}", name, photoId);
    }
}
