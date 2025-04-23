package com.pokemons.png.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pokemons.png.request.Battle;
import com.pokemons.png.request.MyPokemon;
import com.pokemons.png.request.NewPokemon;
import com.pokemons.png.request.Pokemon;
import com.pokemons.png.response.ResponseMessage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import java.util.Comparator;


@Component
public class MainLogic {

    private final SendRequest sendRequest;

    @Autowired
    public MainLogic(SendRequest sendRequest) {
        this.sendRequest = sendRequest;
    }

    public void justRun() throws JsonProcessingException {

        //String myPokemonId = createPokemon();
        //System.out.println("My Pokemon ID: " + myPokemonId);
        final String MY_TRAINER_ID = "36046";
        String myPokemonId;
        String enemyPokemonId = "0";
        ResponseMessage responseMessage;

        List<Pokemon> myPokemons;
        List<Pokemon> enemyPokemons;

        for (int i = 0; i < 6; i++) {

            List<Pokemon> allPokemons = getPokemonList();
            System.out.println("##38 All pokemons.size(): " + allPokemons.size());

            myPokemons = allPokemons.stream()
                .filter(pokemon -> MY_TRAINER_ID.equals(pokemon.getTrainer_id()))
                .sorted(Comparator.comparingInt(Pokemon::getAttack))
                .toList();

            if (myPokemons.size() < 3) {
                myPokemonId = createPokemon();
                System.out.println("##47 New pokemon with ID = " + myPokemonId + " created");
            } else {
                myPokemonId = myPokemons.getFirst().getId();
                System.out.println("##43 Don't need to create new pokemon. Have pokemon with ID = " + myPokemonId);
            }

            enemyPokemons = allPokemons.stream()
                .filter(pokemon -> !MY_TRAINER_ID.equals(pokemon.getTrainer_id()))
                .filter(pokemon -> pokemon.getAttack() == 1)
                .sorted(Comparator.comparingInt(Pokemon::getAttack)
                    .thenComparing(Pokemon::getId))
                .toList();

            if (!enemyPokemons.isEmpty()) {
                enemyPokemonId = enemyPokemons.getFirst().getId();
                System.out.println("##63. Выбран покемон для атаки: " + enemyPokemonId);
            }

            if (!myPokemonId.equals("0") && !enemyPokemonId.equals("0")) {
                responseMessage = fight(new Battle(myPokemonId, enemyPokemonId));
                System.out.println(
                    "##68. Проведен. Результат: " + responseMessage.getMessage() + ", " + responseMessage.getResult() +
                    "/n " + responseMessage.getBattleLimit());
            }

            try {
                Thread.sleep(10000); // 10000 milliseconds = 10 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted, failed to complete sleep");
            }

        }

        System.exit(0);
    }


    private String createPokemon() throws JsonProcessingException {
        NewPokemon pokemon = new NewPokemon("generate", -1);
        String CREATE_POKEMON_URL = "HTTPS://api.pokemonbattle.ru/v2/pokemons";
        String POKEMON_IN_BALL_URL = "HTTPS://api.pokemonbattle.ru/v2/trainers/add_pokeball";

        ResponseMessage message = sendRequest.makeRequest(pokemon.toString(), CREATE_POKEMON_URL, HttpMethod.POST);
        System.out.println("##79. Создан новый покемон с ID = " + message.getId());
        MyPokemon myPokemon = new MyPokemon("0");

        if (!message.getId().equals("0")) {
            myPokemon.setPokemonId(message.getId());
            message = sendRequest.makeRequest(myPokemon.toString(), POKEMON_IN_BALL_URL, HttpMethod.POST);
            System.out.println("##85. Покемон с ID = " + message.getId() + " пойман в pokeball");
        }
        return message.getId();
    }


    private ResponseMessage fight(Battle battle) throws JsonProcessingException {
        String BATTLE_URL = "https://api.pokemonbattle.ru/v2/battle";
        return sendRequest.makeRequest(battle.toString(), BATTLE_URL, HttpMethod.POST);
    }

    private List<Pokemon> getPokemonList() throws JsonProcessingException {
        String POKEMON_LIST_URL = "https://api.pokemonbattle.ru/v2/pokemons?in_pokeball=1";
        return sendRequest.makeRequest("", POKEMON_LIST_URL, HttpMethod.GET).getData();
    }

}
