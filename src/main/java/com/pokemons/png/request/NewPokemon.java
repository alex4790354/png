package com.pokemons.png.request;


import com.fasterxml.jackson.annotation.JsonProperty;

public class NewPokemon {

    private final String name;
    @JsonProperty("photo_id")
    private int photoId;
    @JsonProperty("photo_id")
    private String pokemonId;

    public NewPokemon(String name, int photoId, String pokemonId) {
        this.name = name;
        this.photoId = photoId;
        this.pokemonId = pokemonId;
    }

    @Override
    public String toString() {
        return String.format("{ \"name\": \"%s\", \"photo_id\": %s, \"pokemon_id\": \"%s\" }", name, photoId, pokemonId);
    }
}
