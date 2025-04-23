package com.pokemons.png.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Battle {

    @JsonProperty("attacking_pokemon")
    private String attackingPokemon;

    @JsonProperty("defending_pokemon")
    private String defendingPokemon;

    ////////
    public Battle(String attackingPokemon, String defendingPokemon) {
        this.attackingPokemon = attackingPokemon;
        this.defendingPokemon = defendingPokemon;
    }

    @Override
    public String toString() {
        return String.format(
            """
                { \s
                 "attacking_pokemon": "%s",\s
                 "defending_pokemon": "%s"\s
                }""", this.attackingPokemon, this.defendingPokemon);
    }
    ////////

    public String getAttackingPokemon() {
        return attackingPokemon;
    }

    public void setAttackingPokemon(String attackingPokemon) {
        this.attackingPokemon = attackingPokemon;
    }

    public String getDefendingPokemon() {
        return defendingPokemon;
    }

    public void setDefendingPokemon(String defendingPokemon) {
        this.defendingPokemon = defendingPokemon;
    }
}
