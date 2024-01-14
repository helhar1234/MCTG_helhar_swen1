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

    public WheelOfFortuneService(WheelOfFortuneRepository wheelOfFortuneRepository, UserRepository userRepository, CardRepository cardRepository, CoinRepository coinRepository, Random random) {
        this.wheelOfFortuneRepository = wheelOfFortuneRepository;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.coinRepository = coinRepository;
        this.random = random;
    }

    /**
     * Spins the wheel of fortune for the given user and returns the prize won.
     * Users are allowed to spin only once per day.
     *
     * @param user The user who is spinning the wheel.
     * @return The prize won by the user.
     * @throws HttpStatusException If the user has already spun the wheel today.
     */
    public WheelPrize spin(User user) {
        // Check if the user has already spun the wheel today
        if (!wheelOfFortuneRepository.hasUserSpun(user.getId())) {
            // Get a random prize for the user
            WheelPrize prize = getRandPrize(user);

            // Record the user's spin in the repository
            wheelOfFortuneRepository.saveSpin(user.getId());

            return prize;
        } else {
            // If the user has already spun today, throw an exception
            throw new HttpStatusException(HttpStatus.FORBIDDEN, user.getUsername() + " has already spun today - Try again tomorrow;)");
        }
    }


    /**
     * Generates a random prize for the user spinning the wheel.
     *
     * @param spinner The user spinning the wheel.
     * @return The randomly generated WheelPrize.
     */
    private WheelPrize getRandPrize(User spinner) {
        // Generate a random number to determine the prize
        int randomNumber = random.nextInt(16) + 1;
        WheelPrize prize = new WheelPrize();

        // Set the spinner of the prize
        prize.setSpinner(spinner);

        // Determine the prize based on the random number
        if (randomNumber == 1) {
            // If the number is 1, deduct coins from the user
            updateUserCoins(spinner, -2);
            prize.setPrizeType("COINS");
            prize.setCoinAmount(-2);
        } else if (randomNumber == 2) {
            // If the number is 2, deduct more coins from the user
            updateUserCoins(spinner, -5);
            prize.setPrizeType("COINS");
            prize.setCoinAmount(-5);
        } else if (randomNumber <= 10) {
            // If the number is between 3 and 10, add coins to the user
            int coinsToAdd = randomNumber - 2;
            updateUserCoins(spinner, coinsToAdd);
            prize.setPrizeType("COINS");
            prize.setCoinAmount(coinsToAdd);
        } else {
            // If the number is above 10, award a card or coins if no card is available
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


    /**
     * Updates the coin balance of the user based on a given change in coins.
     * Ensures that the coin balance does not fall below zero.
     *
     * @param spinner    The user whose coin balance is to be updated.
     * @param coinChange The change in the coin amount (can be negative or positive).
     */
    private void updateUserCoins(User spinner, int coinChange) {
        // Calculate the new coin amount
        int newCoinAmount = spinner.getCoins() + coinChange;

        // Ensure the new coin amount does not fall below zero
        if (newCoinAmount < 0) {
            spinner.setCoins(0);
        } else {
            // Update the user's coin balance in the database and memory
            coinRepository.updateCoins(spinner.getId(), coinChange);
            spinner.setCoins(newCoinAmount);
        }
    }


}
