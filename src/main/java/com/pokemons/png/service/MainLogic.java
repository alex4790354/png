package com.pokemons.png.service;

import com.pokemons.png.request.Battle;
import com.pokemons.png.request.PokemonShort;
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

    private int count = 870; //675
    private final SendRequest sendRequest;
    private final List<Pokemon> pokemonNames = Arrays.asList(
        /*new Pokemon("JSON", 311),
        new Pokemon("XML", 312),
        new Pokemon("WSDL", 426)*/
        new Pokemon("R2B2-", 338),
        new Pokemon("WALL-E ", 379),
        new Pokemon("T800 U", 970)
    );
    private final String MY_TRAINER_ID = "36046";
    private boolean isLastBattle = false;
    private int sleepCount = 0;
    private int sleepTime = 30_000;
    String POKEMON_INTO_BALL_URL = "HTTPS://api.pokemonbattle.ru/v2/trainers/add_pokeball";
    String POKEMON_FROM_BALL_URL = "HTTPS://api.pokemonbattle.ru/v2/trainers/delete_pokeball";

    List<Pokemon> allPokemons = new ArrayList<>();
    List<Pokemon> myPokemons = new ArrayList<>();
    List<Pokemon> enemyPokemons = new ArrayList<>();

    @Autowired
    public MainLogic(SendRequest sendRequest) {
        this.sendRequest = sendRequest;
    }

    public void justRun() {

        String myPokemonId;
        Pokemon myPokemon = new Pokemon();
        PokemonShort myPokemonShort = new PokemonShort("0");
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
                    System.out.println("##64 New pokemon with ID = " + myPokemonId + " created");
                    getPokemonsList();
                    myPokemonId = "0";
                }
            } else {
                myPokemon = myPokemons.getLast();
                myPokemonShort = new PokemonShort(myPokemon.getId());
                myPokemonId = myPokemon.getId();
            }

            if (!enemyPokemons.isEmpty()) {
                enemyPokemonId = enemyPokemons.getFirst().getId();
            }

            if (enemyPokemonId.equals("0")) {
                if (sleepCount <= 1) {
                    System.out.println("##80. No enemy found");
                } else if (sleepCount > 60) {
                    sleepCount = -1;
                    isLastBattle = false;
                    sleepTime = 60_000;
                }
                sleepCount++;
            } else if (myPokemonId.equals("0")) {
                System.out.println("##88. My pokemonId = 0");
            } else if (isLastBattle) {
                if (sleepCount == 0) {
                    System.out.println("##91. The attack limit has been reached for today. Sleep tight");
                } else if (sleepCount > 60) {
                    sleepCount = -1;
                    isLastBattle = false;
                    sleepTime = 60_000;
                }
                sleepCount++;
            }  else {

                if (myPokemon.getInPokeball() != 1) {
                    responseMessage = sendRequest.makeRequest(myPokemonShort.toString(), POKEMON_INTO_BALL_URL, HttpMethod.POST);
                    System.out.println("##101. Gettging ready for fight. message.getStatus(): " + responseMessage.getStatus());
                }

                responseMessage = fight(new Battle(myPokemonId, enemyPokemonId));
                if (responseMessage.getMessage().contains("Твой лимит боёв исчерпан")) {
                    System.out.println("##107. The battle limit has been reached");
                    isLastBattle = true;
                    sleepTime = 300_000;
                } else {
                    System.out.println("##111. Selected pokemon for attack: " + enemyPokemonId);
                    System.out.println(
                        "##113. The fight has passed. getMessage(): " + responseMessage.getMessage() + ", \n .getResult(): " +
                            responseMessage.getResult() + ", " + responseMessage.getBattleLimit());

                    if (responseMessage.getResult().contains("победил")) {
                        responseMessage = sendRequest.makeRequest(myPokemonShort.toString(), POKEMON_FROM_BALL_URL, HttpMethod.PUT);
                        System.out.println("##118. Вытаскиваю покемона: " + responseMessage.getMessage());
                    } else if (responseMessage.getResult().contains("проиграл")) {
                        myPokemons.remove(myPokemon);
                        createPokemon(myPokemons);
                    }
                }
            }

            try {
                Thread.sleep(sleepTime); // 60_000 milliseconds = 1 min
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
                ResponseMessage message = sendRequest.makeRequest(pokemon.toString(), CREATE_POKEMON_URL, HttpMethod.POST);
                PokemonShort myPokemon = new PokemonShort("0");
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

        String ENEMY_POKEMON_LIST_URL = "https://api.pokemonbattle.ru/v2/pokemons?in_pokeball=1&status=1";
        String MY_POKEMON_LIST_URL = "https://api.pokemonbattle.ru/v2/pokemons?trainer_id=" + MY_TRAINER_ID + "&status=1";

        allPokemons = sendRequest.makeRequest("", ENEMY_POKEMON_LIST_URL, HttpMethod.GET).getData();
        enemyPokemons = allPokemons.stream()
            .filter(pokemon -> !MY_TRAINER_ID.equals(pokemon.getTrainerId()))
            .filter(pokemon -> pokemon.getAttack() == 1)
            .filter(pokemon -> pokemon.getInPokeball() == 1)
            .filter(pokemon -> pokemon.getStatus() == 1)
            .sorted(Comparator.comparingInt(Pokemon::getAttack)
                .thenComparing(Pokemon::getId))
            .toList();

        try {
            Thread.sleep(100); // 60_000 milliseconds = 1 min
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread was interrupted, failed to complete sleep");
        }
        myPokemons = sendRequest.makeRequest("", MY_POKEMON_LIST_URL, HttpMethod.GET).getData();
    }

}
