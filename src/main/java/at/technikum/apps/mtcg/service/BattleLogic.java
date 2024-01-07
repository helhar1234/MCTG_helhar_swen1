package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.battle.BattleRepository;
import at.technikum.apps.mtcg.repository.battle.BattleRepository_db;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.card.CardRepository_db;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository_db;

import java.util.Arrays;
import java.util.Collections;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
public class BattleLogic {
    private final BattleRepository battleRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    private static final int MAX_ROUNDS = 100;
    private static final int ELO_WIN = 3;
    private static final int ELO_LOSS = -5;
    private static final int ELO_START = 100;

    public BattleLogic() {
        this.battleRepository = new BattleRepository_db();
        this.userRepository = new UserRepository_db();
        this.cardRepository = new CardRepository_db();
    }

    public BattleResult performBattle(String battleId, User playerA, User playerB) {
        boolean started = battleRepository.startBattle(battleId, playerA.getId(), playerB.getId());
        if (!started) {
            // Handle the case where the battle cannot be started.
            return null;
        }

        // Start log
        battleRepository.startLog(battleId, "Battle started between " + playerA.getUsername() + " and " + playerB.getUsername() + ".\n");

        // Get player decks
        Card[] playerADeck = cardRepository.getUserDeckCards(playerA.getId());
        Card[] playerBDeck = cardRepository.getUserDeckCards(playerB.getId());


        // Initialize ELO points
        int eloA = ELO_START;
        int eloB = ELO_START;

        // Battle logic
        int round = 0;
        while (round < MAX_ROUNDS && playerADeck.length > 0 && playerBDeck.length > 0 && eloA >= 0 && eloB >= 0) {
            round++;

            // Shuffle decks
            shuffleDeck(playerADeck);
            shuffleDeck(playerBDeck);

            Card cardA = playerADeck[0];
            Card cardB = playerBDeck[0];

            // Determine winner of the round
            Card winnerCard = determineRoundWinner(cardA, cardB);
            User roundWinner = null;
            if (winnerCard != null) {
                if (winnerCard.equals(cardA)) {
                    roundWinner = playerA;
                    if (!cardRepository.isCardInStack(roundWinner.getId(), cardB.getId())) {
                        cardRepository.deleteCardFromStack(playerB.getId(), cardB.getId());
                        cardRepository.deleteCardFromStack(roundWinner.getId(), cardA.getId());
                        cardRepository.addCardToStack(roundWinner.getId(), cardB.getId());
                        cardRepository.addCardToStack(playerB.getId(), cardA.getId());
                        cardRepository.addCardToDeck(roundWinner.getId(), cardB.getId());
                        cardRepository.addCardToDeck(playerB.getId(), cardA.getId());
                        String roundLog = "Card " + cardB.getName() + " was captured by " + roundWinner.getUsername() + "\n";
                        battleRepository.addToLog(battleId, roundLog);

                        System.out.println(roundLog);
                    }
                    eloA += ELO_WIN;
                    eloB -= ELO_LOSS;
                } else {
                    roundWinner = playerB;
                    if (!cardRepository.isCardInStack(roundWinner.getId(), cardA.getId())) {
                        cardRepository.deleteCardFromStack(playerA.getId(), cardA.getId());
                        cardRepository.deleteCardFromStack(roundWinner.getId(), cardB.getId());
                        cardRepository.addCardToStack(roundWinner.getId(), cardA.getId());
                        cardRepository.addCardToStack(playerA.getId(), cardB.getId());
                        cardRepository.addCardToDeck(roundWinner.getId(), cardA.getId());
                        cardRepository.addCardToDeck(playerA.getId(), cardB.getId());
                        String roundLog = "Card " + cardA.getName() + " was captured by " + roundWinner.getUsername() + "\n";
                        battleRepository.addToLog(battleId, roundLog);

                        System.out.println(roundLog);
                    }
                    eloB += ELO_WIN;
                    eloA -= ELO_LOSS;
                }

            }

            // Make battle log entry
            String roundLog = "Round " + round + ": " +
                    cardA.getName() + " vs " +
                    cardB.getName() + " -> " +
                    (roundWinner != null ? roundWinner.getUsername() + " wins" : "Draw")
                    + ".\n";
            battleRepository.addToLog(battleId, roundLog);
        }

        // Determine the final winner
        User finalWinner = eloA > eloB ? playerA : (eloB > eloA ? playerB : null);
        if (finalWinner != null) {
            battleRepository.crownWinner(battleId, finalWinner.getId());
            userRepository.updateELO(playerA.getId(), eloA);
            userRepository.updateELO(playerB.getId(), eloB);
            String roundLog = "GAME OVER!\n" + finalWinner.getUsername() + " won the Battle!\n";
            battleRepository.addToLog(battleId, roundLog);
        }

        // Return the battle result
        return battleRepository.findBattleById(battleId).orElse(null);
    }

    private void shuffleDeck(Card[] deck) {
        Collections.shuffle(Arrays.asList(deck));
    }

    private Card determineRoundWinner(Card cardA, Card cardB) {
        // Special rules check
        if (isSpecialRuleApplicable(cardA, cardB)) {
            return getSpecialRuleWinner(cardA, cardB);
        }

        // Element type logic for spells
        if (cardA.getCardType().equals("spell") || cardB.getCardType().equals("spell")) {
            double damageA = getEffectiveDamage(cardA, cardB);
            double damageB = getEffectiveDamage(cardB, cardA);

            if (damageA > damageB) return cardA;
            if (damageB > damageA) return cardB;
            // If it's a draw
            return null;
        }

        // Direct damage comparison for monsters
        if (cardA.getDamage() > cardB.getDamage()) return cardA;
        if (cardB.getDamage() > cardA.getDamage()) return cardB;

        // If none of the above rules determine a winner, it's a draw
        return null;
    }

    private boolean isSpecialRuleApplicable(Card cardA, Card cardB) {
        // Check for any special rules that might apply
        // Add more special rules as required
        return (cardA.getName().equalsIgnoreCase("Goblin") && cardB.getName().equalsIgnoreCase("Dragon")) ||
                (cardA.getName().equalsIgnoreCase("Wizzard") && cardB.getName().equalsIgnoreCase("Ork")) ||
                (cardA.getName().equalsIgnoreCase("Knight") && cardB.getCardType().equalsIgnoreCase("spell") && cardB.getElementType().equalsIgnoreCase("Water")) ||
                (cardA.getName().equalsIgnoreCase("Kraken") && cardB.getCardType().equalsIgnoreCase("spell")) ||
                (cardA.getName().equalsIgnoreCase("FireElf") && cardB.getName().equalsIgnoreCase("Dragon"));
    }

    private Card getSpecialRuleWinner(Card cardA, Card cardB) {
        // Define the winner based on special rules
        if (cardA.getName().equalsIgnoreCase("Goblin") && cardB.getName().equalsIgnoreCase("Dragon")) return cardB;
        if (cardA.getName().equalsIgnoreCase("Wizzard") && cardB.getName().equalsIgnoreCase("Ork")) return cardA;
        if (cardA.getName().equalsIgnoreCase("Knight") && cardB.getCardType().equalsIgnoreCase("spell") && cardB.getElementType().equalsIgnoreCase("Water"))
            return cardB;
        if (cardA.getName().equalsIgnoreCase("Kraken") && cardB.getCardType().equalsIgnoreCase("spell")) return cardA;
        if (cardA.getName().equalsIgnoreCase("FireElf") && cardB.getName().equalsIgnoreCase("Dragon")) return cardA;
        // If no special rule gives a definite winner, it defaults to a draw
        return null;
    }

    private double getEffectiveDamage(Card attacker, Card defender) {
        double damage = attacker.getDamage();
        if (attacker.getCardType().equals("spell")) {
            switch (attacker.getElementType().toLowerCase()) {
                case "water":
                    if (defender.getElementType().equalsIgnoreCase("fire")) {
                        damage *= 2; // Water is effective against Fire
                    }
                    if (defender.getElementType().equalsIgnoreCase("normal")) {
                        damage /= 2; // Water is not effective against Normal
                    }
                    break;
                case "fire":
                    if (defender.getElementType().equalsIgnoreCase("normal")) {
                        damage *= 2; // Fire is effective against Normal
                    }
                    if (defender.getElementType().equalsIgnoreCase("water")) {
                        damage /= 2; // Fire is not effective against Water
                    }
                    break;
                case "normal":
                    if (defender.getElementType().equalsIgnoreCase("water")) {
                        damage *= 2; // Normal is effective against Water
                    }
                    if (defender.getElementType().equalsIgnoreCase("fire")) {
                        damage /= 2; // Normal is not effective against Fire
                    }
                    break;
            }
        }
        return damage;
    }

}
