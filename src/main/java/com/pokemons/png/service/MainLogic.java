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
    private final int MAX_TRAINER_LEVEL = 6; // min:2, max:6
    private final SendRequest sendRequest;
    private final List<Pokemon> pokemonNames = Arrays.asList(
        /*new Pokemon("JSON", 311),
        new Pokemon("XML", 312),
        new Pokemon("WSDL", 426)
        new Pokemon("R2B2-", 338),
        new Pokemon("WALL-E ", 379),
        new Pokemon("T800 U", 970) */
        new Pokemon("Бим ", 338),
        new Pokemon("Бом ", 379),
        new Pokemon("Бум ", 970)
    );
    private final String MY_TRAINER_ID = "36046";
    private boolean isLastBattle = false;
    private int sleepCount = 0;
    private int sleepTime = 25_000;
    private final boolean isLeaveInPokeBall = true;
    private int battleWinCount = 0;
    private int battleLoseCount = 0;
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
            //System.out.println("##70");
            getPokemonsList();
            //System.out.println("##72");
            if (myPokemons == null) {
                myPokemons = new ArrayList<>();
            }

            if (myPokemons.size() < 3) {
                //System.out.println("##78");
                for (int i = myPokemons.size(); i < 3; i++) {
                    //System.out.println("##80");
                    myPokemonId = createPokemon(myPokemons);
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##83 " + currentTime + " New pokemon with ID = " + myPokemonId + " created");
                    getPokemonsList();
                    //System.out.println("##85");
                    myPokemonId = "0";
                }
            } else {
                //System.out.println("##89");
                myPokemon = myPokemons.getLast();
                myPokemonShort = new PokemonShort(myPokemon.getId());
                myPokemonId = myPokemon.getId();
            }
            //System.out.println("##94");

            if (!enemyPokemons.isEmpty()) {
                //System.out.println("##97");
                setTrainerHashMap();
                enemyPokemonId = getEnemyId(); // enemyPokemons.getFirst().getId();
                //System.out.println("##100");
            }

            if (enemyPokemonId.equals("0")) {
                //System.out.println("##104");
                if (sleepCount <= 1) {
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##100 " + currentTime + " . No enemy found");
                    sleepTime = 5_000;
                } else if (sleepCount > 15) {
                    sleepCount = -1;
                    isLastBattle = false;
                    sleepTime = 30_000;
                } else {
                    sleepTime = 300_000;
                }
                sleepCount++;
            } else if (myPokemonId.equals("0")) {
                currentTime = LocalTime.now().format(formatter);
                System.out.println("##123 " + currentTime + " .My pokemonId = 0");
            } else if (isLastBattle) {
                if (sleepCount == 0) {
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##116 " + currentTime + " . The attack limit has been reached for today. Sleep tight");
                } else if (sleepCount > 15) {
                    sleepCount = -1;
                    isLastBattle = false;
                }
                //System.out.println("##128");
                sleepCount++;
                sleepTime = 300_000;
            }  else {
                //System.out.println("##132");
                sleepTime = 5_000;
                if (myPokemon.getInPokeball() != 1) {
                    responseMessage = sendRequest.makeRequest(myPokemonShort.toString(), POKEMON_INTO_BALL_URL, HttpMethod.POST);
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##137 " + currentTime + " . Gettging ready for fight. message.getStatus(): " + responseMessage.getStatus());
                }

                responseMessage = fight(new Battle(myPokemonId, enemyPokemonId));
                //System.out.println("##141");
                if (responseMessage.getMessage().contains("Твой лимит боёв исчерпан")) {
                    currentTime = LocalTime.now().format(formatter);
                    System.out.println("##143 " + currentTime + " . The battle limit has been reached");
                    isLastBattle = true;
                    sleepTime = 300_000;
                } else {
                    currentTime = LocalTime.now().format(formatter);
                    if (responseMessage.getResult().contains("победил")) {
                        battleWinCount++;
                        if (!isLeaveInPokeBall) {
                            responseMessage = sendRequest.makeRequest(myPokemonShort.toString(), POKEMON_FROM_BALL_URL,
                                HttpMethod.PUT);
                            System.out.println("        ##153 " + currentTime + " . Вытаскиваю покемона: " + responseMessage.getMessage());
                        } else {
                            System.out.println("        ##155 " + currentTime + " . Оставил в покеболе: " + responseMessage.getMessage());
                        }
                        //System.out.println("##158");
                    } else if (responseMessage.getResult().contains("проиграл")) {
                        //System.out.println("##160");
                        getPokemonsList();
                        createPokemon(myPokemons);
                        //System.out.println("##163");
                    }
                    System.out.println("##165 " + currentTime +
                        ". Fight with: " + enemyPokemonId +
                        ". Response: " + responseMessage.getMessage() +
                        ", .getResult(): " + responseMessage.getResult() + ", " + responseMessage.getBattleLimit() +
                        ". Побед: " + battleWinCount +
                        ", Поражений: " + battleLoseCount + 1);
                }
            }
            //System.out.println("##173");

            try {
                Thread.sleep(sleepTime); // 60_000 milliseconds = 1 min
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted, failed to complete sleep");
            }
            //System.out.println("##181");
        }
        //System.exit(0);
    }


    private String createPokemon(List<Pokemon> myPokemons) {
        String RENAME_POKEMON_URL = "HTTPS://api.pokemonbattle.ru/v2/pokemons";

        for (Pokemon pokemonTemplate : pokemonNames) {
            //System.out.println("##185");
            boolean exists = myPokemons.stream()
                .anyMatch(pokemon -> pokemon.getPhotoId() == pokemonTemplate.getPhotoId());

            if (!exists) {
                battleLoseCount++;
                NewPokemon pokemon = new NewPokemon(Integer.toBinaryString(CURRENT_COUNT++), pokemonTemplate.getPhotoId(), "0");
                String CREATE_POKEMON_URL = "HTTPS://api.pokemonbattle.ru/v2/pokemons";
                ResponseMessage message = sendRequest.makeRequest(pokemon.toString(), CREATE_POKEMON_URL, HttpMethod.POST);
                pokemon = new NewPokemon(pokemonTemplate.getName() + Integer.toBinaryString(Integer.parseInt(message.getId()) % 100),
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
        //System.out.println("##213");
        String BATTLE_URL = "https://api.pokemonbattle.ru/v2/battle";
        return sendRequest.makeRequest(battle.toString(), BATTLE_URL, HttpMethod.POST);
    }

    private void getPokemonsList() {
        //System.out.println("##219");
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
            System.out.println("##226. Thread was interrupted, failed to complete sleep");
        }
        //System.out.println("##225");
        List<Pokemon> myPokemons1 = sendRequest.makeRequest("", MY_POKEMON_LIST_URL, HttpMethod.GET).getData();
        //System.out.println("##227");
        myPokemons = myPokemons1.stream()
            .filter(pokemon -> !pokemon.getId().equals("311111"))
            .filter(pokemon -> !pokemon.getId().equals("314618"))
            .toList();
        //System.out.println("##231");
    }

    private void setTrainerHashMap() {
        //System.out.println("##249");
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
        //System.out.println("##261");
        return enemyPokemons.stream()
            .filter(pokemon -> pokemon.getAttack() == 1)
            .filter(pokemon -> trainersMap.get(pokemon.getTrainerId()) != null && trainersMap.get(pokemon.getTrainerId()) < MAX_TRAINER_LEVEL)
            .findFirst()
            .map(Pokemon::getId)
            .orElse("0");
    }
}
