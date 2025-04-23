package com.pokemons.png.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MyPokemon {

    @JsonProperty("pokemon_id")
    private String pokemonId;

    ///////////////
    public MyPokemon(String pokemonId) {
        this.pokemonId = pokemonId;
    }

    @Override
    public String toString() {
        return String.format("{ \"pokemon_id\": \"%s\" }", pokemonId);
    }

    public String getPokemonId() {
        return pokemonId;
    }

    public void setPokemonId(String pokemonId) {
        this.pokemonId = pokemonId;
    }
}
