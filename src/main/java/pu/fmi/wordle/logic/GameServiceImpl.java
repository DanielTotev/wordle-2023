package pu.fmi.wordle.logic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.stereotype.Component;
import pu.fmi.wordle.model.Game;
import pu.fmi.wordle.model.GameRepo;
import pu.fmi.wordle.model.Guess;
import pu.fmi.wordle.model.WordRepo;

@Component
public class GameServiceImpl implements GameService {

  final GameRepo gameRepo;

  final WordRepo wordRepo;

  public GameServiceImpl(GameRepo gameRepo, WordRepo wordRepo) {
    this.gameRepo = gameRepo;
    this.wordRepo = wordRepo;
  }

  @Override
  public Game startNewGame() {
    var game = new Game();
    game.setId(UUID.randomUUID().toString());
    game.setStartedOn(LocalDateTime.now());
    game.setWord(wordRepo.getRandom());
    game.setGuesses(new ArrayList<>(game.getMaxGuesses()));
    return game;
  }

  @Override
  public Game getGame(String id) {
    var game = gameRepo.get(id);
    if (game == null) throw new GameNotFoundException(id);
    return game;
  }

  @Override
  public Game makeGuess(String gameId, String word) {
    if(!wordRepo.exists(word)) {
      throw new UnknownWordException(word);
    }
    Game game = getGame(gameId);
    Guess guess = createGuess(word);
    String matches = createMatchesString(game.getWord(), guess.getWord());
    guess.setMatches(matches);
    game.getGuesses().add(guess);
    gameRepo.update(game);
    return game;
  }

  private String createMatchesString(String gameWord, String guessWord) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < guessWord.length(); i++) {
      char currentCharFromGuessWord = guessWord.charAt(i);
      if(currentCharFromGuessWord == gameWord.charAt(i)) {
        result.append(Guess.PLACE_MATCH);
        continue;
      }
      char charToAppend = gameWord.contains("" + currentCharFromGuessWord) ? Guess.LETTER_MATCH : Guess.NO_MATCH;
      result.append(charToAppend);
    }
    return result.toString();
  }
  private Guess createGuess(String word) {
    Guess guess = new Guess();
    guess.setWord(word);
    guess.setMadeAt(LocalDateTime.now());
    return guess;
  }
}
