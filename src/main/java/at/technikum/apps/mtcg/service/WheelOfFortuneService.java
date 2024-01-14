package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.dto.WheelPrize;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.coins.CoinRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.apps.mtcg.repository.wheel.WheelOfFortuneRepository;
import at.technikum.server.http.HttpStatus;

import java.util.Optional;
import java.util.Random;

public class WheelOfFortuneService {
    private final WheelOfFortuneRepository wheelOfFortuneRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CoinRepository coinRepository;

    private final Random random;

    public WheelOfFortuneService(WheelOfFortuneRepository wheelOfFortuneRepository,
                                 UserRepository userRepository,
                                 CardRepository cardRepository,
                                 CoinRepository coinRepository, Random random) {
        this.wheelOfFortuneRepository = wheelOfFortuneRepository;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.coinRepository = coinRepository;
        this.random = random;
    }

    public WheelPrize spin(User user) {
        if (!wheelOfFortuneRepository.hasUserSpun(user.getId())) {
            WheelPrize prize = getRandPrize(user);
            wheelOfFortuneRepository.saveSpin(user.getId());
            return prize;
        } else {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, user.getUsername() + " has already spun today - Try again tomorrow;)");
        }
    }

    private WheelPrize getRandPrize(User spinner) {
        int randomNumber = random.nextInt(16) + 1;
        WheelPrize prize = new WheelPrize();

        prize.setSpinner(spinner);
        if (randomNumber == 1) {
            updateUserCoins(spinner, -2);
            prize.setPrizeType("COINS");
            prize.setCoinAmount(-2);
        } else if (randomNumber == 2) {
            updateUserCoins(spinner, -5);
            prize.setPrizeType("COINS");
            prize.setCoinAmount(-5);
        } else if (randomNumber <= 10) {
            int coinsToAdd = randomNumber - 2; // Zufälliger Betrag zwischen 1 und 10 Münzen
            updateUserCoins(spinner, coinsToAdd);
            prize.setPrizeType("COINS");
            prize.setCoinAmount(coinsToAdd);
        } else {
            Optional<Card> card = cardRepository.getCardNotPossesed();
            if (!card.isPresent()) {
                updateUserCoins(spinner, 5);
                prize.setPrizeType("COINS");
                prize.setCoinAmount(5);
            } else {
                userRepository.addCardToStack(spinner.getId(), card.get());
                prize.setPrizeType("CARD");
                prize.setWonCard(card.get());
            }
        }
        return prize;
    }

    private void updateUserCoins(User spinner, int coinChange) {
        int newCoinAmount = spinner.getCoins() + coinChange;

        // Überprüfen, ob die neue Münzmenge negativ wäre
        if (newCoinAmount < 0) {
            spinner.setCoins(0);
        } else {
            // Aktualisieren Sie die Münzen in der Datenbank
            coinRepository.updateCoins(spinner.getId(), coinChange);
            // Aktualisieren Sie den Münzstand des Benutzers im Speicher
            spinner.setCoins(newCoinAmount);
        }
    }


}
