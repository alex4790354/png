package com.pokemons.png.service;

import com.pokemons.png.request.Battle;
import com.pokemons.png.request.PokemonShort;
import com.pokemons.png.request.NewPokemon;
import com.pokemons.png.request.Pokemon;
import com.pokemons.png.response.ResponseMessage;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import java.util.Comparator;


@Component
public class MainLogic {

    private int CURRENT_COUNT = 41; //872
    private int MAX_TRAINER_LEVEL = 6; // min:2, max:6
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
    private int sleepTime = 5_000;
    private boolean isLeaveInPokeBall = true;
    String POKEMON_INTO_BALL_URL = "HTTPS://api.pokemonbattle.ru/v2/trainers/add_pokeball";
    String POKEMON_FROM_BALL_URL = "HTTPS://api.pokemonbattle.ru/v2/trainers/delete_pokeball";

    List<Pokemon> allPokemons = new ArrayList<>();
    List<Pokemon> myPokemons = new ArrayList<>();
    List<Pokemon> enemyPokemons = new ArrayList<>();
    Map<String, Integer> trainersMap = new HashMap<>();

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String currentTime;

        //for (int i = 0; i < 10; i++) {
        while (true) {

            enemyPokemonId = "0";
            myPokemonId = "0";
            getPokemonsList();
            if (myPokemons == null) {
                myPokemons = new ArrayList<>();
            }

            if (myPokemons.size() < 3) {
                for (int i = myPokemons.size(); i < 3; i++) {
                    myPokemonId = createPokemon(myPokemons);
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##68 " + currentTime + " New pokemon with ID = " + myPokemonId + " created");
                    getPokemonsList();
                    myPokemonId = "0";
                }
            } else {
                myPokemon = myPokemons.getLast();
                myPokemonShort = new PokemonShort(myPokemon.getId());
                myPokemonId = myPokemon.getId();
            }

            if (!enemyPokemons.isEmpty()) {
                setTrainerHashMap();
                enemyPokemonId = getEnemyId(); // enemyPokemons.getFirst().getId();
            }

            if (enemyPokemonId.equals("0")) {
                if (sleepCount <= 1) {
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##95 " + currentTime + " . No enemy found");
                } else if (sleepCount > 60) {
                    sleepCount = -1;
                    isLastBattle = false;
                    sleepTime = 60_000;
                }
                sleepCount++;
                sleepTime = 305_000;
            } else if (myPokemonId.equals("0")) {
                currentTime = LocalTime.now().format(formatter);
                System.out.println("##105 " + currentTime + " .My pokemonId = 0");
            } else if (isLastBattle) {
                if (sleepCount == 0) {
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##109 " + currentTime + " . The attack limit has been reached for today. Sleep tight");
                } else if (sleepCount > 60) {
                    sleepCount = -1;
                    isLastBattle = false;
                }
                sleepCount++;
                sleepTime = 305_000;
            }  else {
                sleepTime = 5_000;
                if (myPokemon.getInPokeball() != 1) {
                    responseMessage = sendRequest.makeRequest(myPokemonShort.toString(), POKEMON_INTO_BALL_URL, HttpMethod.POST);
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##121 " + currentTime + " . Gettging ready for fight. message.getStatus(): " + responseMessage.getStatus());
                }

                responseMessage = fight(new Battle(myPokemonId, enemyPokemonId));
                if (responseMessage.getMessage().contains("Твой лимит боёв исчерпан")) {
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##127 " + currentTime + " . The battle limit has been reached");
                    isLastBattle = true;
                    sleepTime = 300_000;
                } else {
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##132 " + currentTime + " . Selected pokemon for attack: " + enemyPokemonId);
                    System.out.println("##133 " + currentTime + " . The fight has passed. getMessage(): " + responseMessage.getMessage() + ", .getResult(): " +
                            responseMessage.getResult() + ", " + responseMessage.getBattleLimit());

                    if (responseMessage.getResult().contains("победил")) {
                        if (!isLeaveInPokeBall) {
                            responseMessage = sendRequest.makeRequest(myPokemonShort.toString(), POKEMON_FROM_BALL_URL,
                                HttpMethod.PUT);
                            System.out.println("        ##140 " + currentTime + " . Вытаскиваю покемона: " + responseMessage.getMessage());
                        } else {
                            System.out.println("        ##141 " + currentTime + " . Оставил в покеболе: " + responseMessage.getMessage());
                        }
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
                NewPokemon pokemon = new NewPokemon(pokemonTemplate.getName() + CURRENT_COUNT++, pokemonTemplate.getPhotoId());
                String CREATE_POKEMON_URL = "HTTPS://api.pokemonbattle.ru/v2/pokemons";
                ResponseMessage message = sendRequest.makeRequest(pokemon.toString(), CREATE_POKEMON_URL, HttpMethod.POST);

                if (isLeaveInPokeBall) {
                    PokemonShort pokemonShort = new PokemonShort(message.getId());
                    message = sendRequest.makeRequest(pokemonShort.toString(), POKEMON_INTO_BALL_URL, HttpMethod.POST);
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
            System.out.println("##194. Thread was interrupted, failed to complete sleep");
        }
        myPokemons = sendRequest.makeRequest("", MY_POKEMON_LIST_URL, HttpMethod.GET).getData();
    }

    private void setTrainerHashMap() {
        String TRAINER_INFO_URL = "https://api.pokemonbattle.ru/v2/trainers/";
        ResponseMessage trainer;
        for (Pokemon pokemon : enemyPokemons) {
            if (!trainersMap.containsKey(pokemon.getTrainerId())) {
                trainer = sendRequest.makeRequest("", TRAINER_INFO_URL + pokemon.getTrainerId(), HttpMethod.GET);
                trainersMap.put(trainer.getId(), trainer.getLevel());
            }
        }
    }

    private String getEnemyId() {

        return enemyPokemons.stream()
            .filter(pokemon -> pokemon.getAttack() == 1)
            .filter(pokemon -> trainersMap.get(pokemon.getTrainerId()) != null && trainersMap.get(pokemon.getTrainerId()) < MAX_TRAINER_LEVEL)
            .findFirst()
            .map(Pokemon::getId)
            .orElse("0");
    }

}
