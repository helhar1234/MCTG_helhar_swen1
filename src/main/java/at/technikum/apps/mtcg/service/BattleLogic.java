package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.battle.BattleRepository;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
// TODO: additional feature -> winner kriegt eine komplett neue karte
// FAQ: CAN GAME BE DRAW? -> unit test for all cases
public class BattleLogic {
    private final BattleRepository battleRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    private static final int MAX_ROUNDS = 100;
    private static final int ELO_WIN = 3;
    private static final int ELO_LOSS = -5;

    public BattleLogic(BattleRepository battleRepository, UserRepository userRepository, CardRepository cardRepository) {
        this.battleRepository = battleRepository;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
    }

    public BattleResult performBattle(String battleId, User playerA, User playerB) {
        boolean started = battleRepository.startBattle(battleId, playerA.getId(), playerB.getId());
        if (!started) {
            battleRepository.addToLog(battleId, "Failed to start the battle.\n");
            return null;
        }

        // Start log
        String introLog = "The grand battle commences in the mystical arena! " +
                "Champion " + playerA.getUsername() + " versus the valiant " + playerB.getUsername() +
                ". May the bravest warrior prevail!\n";

        battleRepository.startLog(battleId, introLog);
        // Get player decks and store them in objects
        List<Card> playerADeck = new ArrayList<>(Arrays.asList(cardRepository.getUserDeckCards(playerA.getId())));
        List<Card> playerBDeck = new ArrayList<>(Arrays.asList(cardRepository.getUserDeckCards(playerB.getId())));

        // Log initial deck states
        logDeckState("Initial", playerA, playerADeck, battleId);
        logDeckState("Initial", playerB, playerBDeck, battleId);

        // Initialize ELO points
        int eloA = playerA.getEloRating();
        int eloB = playerB.getEloRating();

        // Battle logic
        int round = 0;
        boolean isDraw = true;
        while (round < MAX_ROUNDS && !playerADeck.isEmpty() && !playerBDeck.isEmpty()) {
            round++;

            // Shuffle decks
            Collections.shuffle(playerADeck);
            Collections.shuffle(playerBDeck);

            Card cardA = playerADeck.get(0);
            Card cardB = playerBDeck.get(0);

            // Determine winner of the round
            Card winnerCard = determineRoundWinner(cardA, cardB);
            String battleNarrative = "Epic Round " + round + ": " +
                    "The mighty " + cardA.getName() + " of " + playerA.getUsername() +
                    " clashes against the fearsome " + cardB.getName() + " of " + playerB.getUsername() + ". ";

            if (winnerCard != null) {
                isDraw = false;
                if (winnerCard.equals(cardA)) {
                    playerADeck.add(cardB); // Player A captures Player B's card
                    battleNarrative += "In a stunning display of power, " + playerA.getUsername() + "'s " + cardA.getName() +
                            " triumphs over " + playerB.getUsername() + "'s " + cardB.getName() +
                            ", capturing the foe!";
                } else {
                    playerBDeck.add(cardA); // Player B captures Player A's card
                    battleNarrative += "With a cunning maneuver, " + playerB.getUsername() + "'s " + cardB.getName() +
                            " overpowers " + playerA.getUsername() + "'s " + cardA.getName() +
                            ", claiming victory!";
                }
            } else {
                battleNarrative += "The clash of titans ends in a deadlock, none could overpower the other!";
            }

            battleRepository.addToLog(battleId, battleNarrative + "\n");
        }

        // Save deck changes to the stack
        saveDeckToStack(playerA, playerADeck, battleId);
        saveDeckToStack(playerB, playerBDeck, battleId);

        // Determine the final winner and update ELO
        if (!isDraw) {
            User finalWinner = playerADeck.size() > playerBDeck.size() ? playerA : playerB;
            User finalLoser = finalWinner.equals(playerA) ? playerB : playerA;
            eloA += finalWinner.equals(playerA) ? ELO_WIN : ELO_LOSS;
            eloB += finalWinner.equals(playerB) ? ELO_WIN : ELO_LOSS;
            userRepository.updateELO(playerA.getId(), eloA);
            userRepository.updateELO(playerB.getId(), eloB);
            battleRepository.crownWinner(battleId, finalWinner.getId());
            battleRepository.addToLog(battleId, finalWinner.getUsername() + " wins the battle with final ELO: " + (finalWinner.equals(playerA) ? eloA : eloB) + "\n");
        } else {
            battleRepository.addToLog(battleId, "The battle ended in a draw.\n");
            battleRepository.crownWinner(battleId, null);
        }

        // Return the battle result
        return battleRepository.findBattleById(battleId).orElse(null);
    }

    private void logDeckState(String state, User player, List<Card> deck, String battleId) {
        StringBuilder log = new StringBuilder(state + " deck state for " + player.getUsername() + ": ");
        for (Card card : deck) {
            log.append(card.getName()).append(", ");
        }
        battleRepository.addToLog(battleId, log + "\n");
    }

    private void saveDeckToStack(User player, List<Card> deck, String battleId) {
        for (Card card : deck) {
            if (!cardRepository.isCardInStack(player.getId(), card.getId())) {
                cardRepository.addCardToStack(player.getId(), card.getId());
                battleRepository.addToLog(battleId, "Card " + card.getName() + " added to stack for " + player.getUsername() + ".\n");
            }
        }
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
        // Check for any special rules that apply
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