package com.pokemons.png.service;

import com.pokemons.png.request.Battle;
import com.pokemons.png.request.MyPokemon;
import com.pokemons.png.request.NewPokemon;
import com.pokemons.png.request.Pokemon;
import com.pokemons.png.response.ResponseMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import java.util.Comparator;


@Component
public class MainLogic {

    private final SendRequest sendRequest;
    private final List<Pokemon> pokemonNames = Arrays.asList(
        new Pokemon("+ ", 311),
        new Pokemon("- ", 312),
        new Pokemon("x ", 426)
    );
    private final String MY_TRAINER_ID = "36046";
    private boolean isLastBattle = false;
    private int sleepCount = 0;
    private int count = 1; //675

    List<Pokemon> allPokemons = new ArrayList<>();
    List<Pokemon> myPokemons = new ArrayList<>();
    List<Pokemon> enemyPokemons = new ArrayList<>();

    @Autowired
    public MainLogic(SendRequest sendRequest) {
        this.sendRequest = sendRequest;
    }

    public void justRun() {

        String myPokemonId;
        String enemyPokemonId;
        ResponseMessage responseMessage;

        //for (int i = 0; i < 10; i++) {
        while (true) {

            enemyPokemonId = "0";
            myPokemonId = "0";
            getPokemonsList();

            if (myPokemons.size() < 3) {
                for (int i = myPokemons.size(); i < 3; i++) {
                    myPokemonId = createPokemon(myPokemons);
                    System.out.println("##60 New pokemon with ID = " + myPokemonId + " created");
                    getPokemonsList();
                    myPokemonId = "0";
                }
            } else {
                myPokemonId = myPokemons.getLast().getId();
            }

            if (!enemyPokemons.isEmpty()) {
                enemyPokemonId = enemyPokemons.getFirst().getId();
            }

            if (enemyPokemonId.equals("0")) {
                if (sleepCount <= 1) {
                    System.out.println("##78. No enemy found");
                } else if (sleepCount > 10) {
                    sleepCount = -1;
                }
                sleepCount++;
            } else if (myPokemonId.equals("0")) {
                System.out.println("##84. My pokemonId = 0");
            } else if (isLastBattle) {
                if (sleepCount == 0) {
                    System.out.println("##87. The attack limit has been reached for today. Sleep tight");
                } else if (sleepCount > 10) {
                    sleepCount = -1;
                }
                sleepCount++;
            }  else {
                responseMessage = fight(new Battle(myPokemonId, enemyPokemonId));
                if (responseMessage.getMessage().contains("Твой лимит боёв исчерпан")) {
                    System.out.println("##95. The battle limit has been reached");
                    isLastBattle = true;
                } else {
                    System.out.println("##98. Selected pokemon for attack: " + enemyPokemonId);
                    System.out.println(
                        "##100. The fight has passed. getMessage(): " + responseMessage.getMessage() + ", \n .getResult(): " +
                            responseMessage.getResult() + ", " + responseMessage.getBattleLimit());
                }
            }


            try {
                Thread.sleep(60_000); // 60_000 milliseconds = 1 min
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted, failed to complete sleep");
            }

        }
        //System.exit(0);
    }


    private String createPokemon(List<Pokemon> myPokemons) {

        for (Pokemon pokemonTemplate : pokemonNames) {
            boolean exists = myPokemons.stream()
                .anyMatch(pokemon -> pokemon.getPhotoId() == pokemonTemplate.getPhotoId());

            if (!exists) {
                NewPokemon pokemon = new NewPokemon(pokemonTemplate.getName() + count++, pokemonTemplate.getPhotoId());
                String CREATE_POKEMON_URL = "HTTPS://api.pokemonbattle.ru/v2/pokemons";
                String POKEMON_IN_BALL_URL = "HTTPS://api.pokemonbattle.ru/v2/trainers/add_pokeball";

                ResponseMessage message = sendRequest.makeRequest(pokemon.toString(), CREATE_POKEMON_URL, HttpMethod.POST);
                MyPokemon myPokemon = new MyPokemon("0");

                if (!message.getId().equals("0")) {
                    myPokemon.setPokemonId(message.getId());
                    message = sendRequest.makeRequest(myPokemon.toString(), POKEMON_IN_BALL_URL, HttpMethod.POST);
                }
                return message.getId();
            }
        }
        return "0";
    }


    private ResponseMessage fight(Battle battle) {
        String BATTLE_URL = "https://api.pokemonbattle.ru/v2/battle";
        return sendRequest.makeRequest(battle.toString(), BATTLE_URL, HttpMethod.POST);
    }

    private void getPokemonsList() {
        String POKEMON_LIST_URL = "https://api.pokemonbattle.ru/v2/pokemons?in_pokeball=1&status=1";

        allPokemons = sendRequest.makeRequest("", POKEMON_LIST_URL, HttpMethod.GET).getData();

        try {
            Thread.sleep(1_000); // 60_000 milliseconds = 1 min
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread was interrupted, failed to complete sleep");
        }

        myPokemons = allPokemons.stream()
            .filter(pokemon -> MY_TRAINER_ID.equals(pokemon.getTrainerId()))
            .filter(pokemon -> pokemon.getStatus() == 1)
            .sorted(Comparator.comparingInt(Pokemon::getAttack))
            .toList();

        enemyPokemons = allPokemons.stream()
            .filter(pokemon -> !MY_TRAINER_ID.equals(pokemon.getTrainerId()))
            .filter(pokemon -> pokemon.getAttack() == 1)
            .filter(pokemon -> pokemon.getStatus() == 1)
            .sorted(Comparator.comparingInt(Pokemon::getAttack)
                .thenComparing(Pokemon::getId))
            .toList();
    }

}
