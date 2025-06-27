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

    private int CURRENT_COUNT = 101; // 697
    private final int MAX_TRAINER_LEVEL = 7; // min:2, max:6
    private final int MAX_ATTACK_LEVEL = 1;
    private final boolean isLeaveInPokeBall = true;
    private final int leaveAtackLevel = 2;

    private final String MY_TRAINER_ID = "36046";
    private boolean isLastBattle = false;
    private int sleepCount = 0;
    private final SendRequest sendRequest;
    private int sleepTime = 25_000;
    private int battleWinCount = 0;
    private int battleLoseCount = 0;
    String POKEMON_INTO_BALL_URL = "HTTPS://api.pokemonbattle.ru/v2/trainers/add_pokeball";
    String POKEMON_FROM_BALL_URL = "HTTPS://api.pokemonbattle.ru/v2/trainers/delete_pokeball";
    List<Pokemon> allPokemons = new ArrayList<>();
    List<Pokemon> myPokemons = new ArrayList<>();
    List<Pokemon> enemyPokemons = new ArrayList<>();
    Map<String, Integer> trainersMap = new HashMap<>();
    private final List<Pokemon> pokemonNames = Arrays.asList(
        /*new Pokemon("JSON", 311),
        new Pokemon("XML", 312),
        new Pokemon("WSDL", 426)
        new Pokemon("R2B2-", 338),
        new Pokemon("WALL-E ", 379),
        new Pokemon("T800 U", 970) */
        new Pokemon("Кевин", 338),
        new Pokemon("Стюарт", 379),
        new Pokemon("Боб", 970)
    );

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
            //System.out.println("##70");
            getPokemonsList();
            //System.out.println("##72");
            if (myPokemons == null) {
                myPokemons = new ArrayList<>();
            }

            if (myPokemons.size() < 3) {
                //System.out.println("##84");
                for (int i = myPokemons.size(); i < 3; i++) {
                    //System.out.println("##80");
                    myPokemonId = createPokemon();
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##89 " + currentTime + " New pokemon with ID = " + myPokemonId + " created");
                    getPokemonsList();
                }
            }

            if (!enemyPokemons.isEmpty()) {
                setTrainerHashMap();
                enemyPokemonId = getEnemyId(); // enemyPokemons.getFirst().getId();
            }

            if (enemyPokemonId.equals("0")) {
                if (sleepCount <= 1) {
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##100 " + currentTime + " . No enemy found" +
                        "battleWinCount: " + battleWinCount + ", battleLoseCount: " + battleLoseCount);
                    sleepTime = 5_000;
                } else if (sleepCount > 15) {
                    sleepCount = -1;
                    isLastBattle = false;
                    sleepTime = 30_000;
                } else {
                    sleepTime = 300_000;
                }
                sleepCount++;
            } else if (isLastBattle) {
                if (sleepCount == 0) {
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##116 " + currentTime + " . The attack limit has been reached for today. Sleep tight. " +
                        "battleWinCount: " + battleWinCount + ", battleLoseCount: " + battleLoseCount);
                } else if (sleepCount > 15) {
                    sleepCount = -1;
                    isLastBattle = false;
                }
                sleepCount++;
                sleepTime = 300_000;
            }  else {
                sleepTime = 5_000;

                myPokemon = myPokemons.getLast();
                myPokemonId = myPokemon.getId();
                myPokemonShort = new PokemonShort(myPokemon.getId());

                if (myPokemon.getInPokeball() != 1) {
                    responseMessage = sendRequest.makeRequest(myPokemonShort.toString(), POKEMON_INTO_BALL_URL, HttpMethod.POST);
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##134 " + currentTime + " . Gettging ready for fight. message.getStatus(): " + responseMessage.getStatus());
                }

                responseMessage = fight(new Battle(myPokemonId, enemyPokemonId));
                if (responseMessage.getMessage().contains("Твой лимит боёв исчерпан")) {
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##140 " + currentTime + " . The battle limit has been reached");
                    isLastBattle = true;
                    sleepTime = 300_000;
                } else {
                    currentTime = LocalTime.now().format(formatter);
                    if (responseMessage.getResult().contains("победил")) {
                        battleWinCount++;
                        if (!isLeaveInPokeBall && myPokemon.getAttack() + 1 < leaveAtackLevel) {
                            responseMessage = sendRequest.makeRequest(myPokemonShort.toString(), POKEMON_FROM_BALL_URL, HttpMethod.PUT);
                            System.out.println("        ##149 " + currentTime + " . Вытаскиваю покемона: " + responseMessage.getMessage());
                        } else {
                            System.out.println("        ##151 " + currentTime + " . Оставил в покеболе: " + responseMessage.getMessage());
                        }
                    } else if (responseMessage.getResult().contains("проиграл")) {
                        getPokemonsList();
                        createPokemon();
                    }
                    System.out.println("##157 " + currentTime +
                        ". Fight with: " + enemyPokemonId +
                        ". Response: " + responseMessage.getMessage() +
                        ", .getResult(): " + responseMessage.getResult() + ", " + responseMessage.getBattleLimit() +
                        ". Побед: " + battleWinCount +
                        ", Поражений: " + battleLoseCount);
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


    private String createPokemon() {
        String RENAME_POKEMON_URL = "HTTPS://api.pokemonbattle.ru/v2/pokemons";
        String CREATE_POKEMON_URL = "HTTPS://api.pokemonbattle.ru/v2/pokemons";

        for (Pokemon pokemonTemplate : pokemonNames) {
            boolean exists = myPokemons.stream()
                .anyMatch(pokemon -> pokemon.getPhotoId() == pokemonTemplate.getPhotoId());

            if (!exists) {
                NewPokemon pokemon = new NewPokemon(Integer.toBinaryString(CURRENT_COUNT++), pokemonTemplate.getPhotoId(), "0");
                ResponseMessage message = sendRequest.makeRequest(pokemon.toString(), CREATE_POKEMON_URL, HttpMethod.POST);
                battleLoseCount++;
                System.out.println("##189: New Pokemon created with ID = " + message.getId() +
                    ", getMessage(): " + message.getMessage() + ", battleLoseCount: " + battleLoseCount);
                //pokemon = new NewPokemon(pokemonTemplate.getName() + Integer.toBinaryString(Integer.parseInt(message.getId()) % 100),
                pokemon = new NewPokemon(pokemonTemplate.getName(),
                    pokemonTemplate.getPhotoId(),
                    message.getId());
                sendRequest.makeRequest(pokemon.toString(), RENAME_POKEMON_URL, HttpMethod.PUT);

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
            .filter(pokemon -> pokemon.getAttack() <= MAX_ATTACK_LEVEL)
            .filter(pokemon -> pokemon.getInPokeball() == 1)
            .filter(pokemon -> pokemon.getStatus() == 1)
            .sorted(Comparator.comparingInt(Pokemon::getAttack)
                .thenComparing(Pokemon::getId))
            .toList();

        try {
            Thread.sleep(100); // 60_000 milliseconds = 1 min
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("##232. Thread was interrupted, failed to complete sleep");
        }
        List<Pokemon> myPokemons1 = sendRequest.makeRequest("", MY_POKEMON_LIST_URL, HttpMethod.GET).getData();
        myPokemons = myPokemons1.stream()
            .filter(pokemon -> !pokemon.getId().equals("311111"))
            .filter(pokemon -> !pokemon.getId().equals("336864"))
            .sorted(Comparator.comparingInt(Pokemon::getAttack)
                .thenComparing(Pokemon::getId))
            .toList();
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
            .filter(pokemon -> pokemon.getAttack() <= MAX_ATTACK_LEVEL)
            .filter(pokemon -> trainersMap.get(pokemon.getTrainerId()) != null && trainersMap.get(pokemon.getTrainerId()) < MAX_TRAINER_LEVEL)
            .findFirst()
            .map(Pokemon::getId)
            .orElse("0");
    }
}

/**

 HTTP error occurred: 400 BAD_REQUEST
 Error response: {"status":"error","message":"Данный покемон в нокауте (id=314218)"}
 Exception in thread "restartedMain" java.lang.reflect.InvocationTargetException
 at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:115)
 at java.base/java.lang.reflect.Method.invoke(Method.java:580)
 at org.springframework.boot.devtools.restart.RestartLauncher.run(RestartLauncher.java:50)
 Caused by: java.lang.NullPointerException: Cannot invoke "String.contains(java.lang.CharSequence)" because the return value of "com.pokemons.png.response.ResponseMessage.getResult()" is null
 at com.pokemons.png.service.MainLogic.justRun(MainLogic.java:152)
 at com.pokemons.png.PngApplication.main(PngApplication.java:18)
 at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
 ... 2 more

 * */