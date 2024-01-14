package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.battle.BattleRepository;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.elo.ELORepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BattleLogic {
    private final BattleRepository battleRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final ELORepository eloRepository;

    private static final int MAX_ROUNDS = 100;
    private static final int ELO_WIN = 3;
    private static final int ELO_LOSS = -5;

    public BattleLogic(BattleRepository battleRepository, UserRepository userRepository, CardRepository cardRepository, ELORepository eloRepository) {
        this.battleRepository = battleRepository;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.eloRepository = eloRepository;
    }

    /**
     * Conducts a battle between two players and records the outcome.
     *
     * @param battleId The unique identifier of the battle.
     * @param playerA  The first player in the battle.
     * @param playerB  The second player in the battle.
     * @return The result of the battle.
     */
    public BattleResult performBattle(String battleId, User playerA, User playerB) {
        // Attempt to start the battle and log if it fails
        boolean started = battleRepository.startBattle(battleId, playerA.getId(), playerB.getId());
        if (!started) {
            battleRepository.addToLog(battleId, "Failed to start the battle.\n");
            return null;
        }

        // Log the beginning of the battle
        String introLog = "The grand battle commences in the mystical arena! " +
                "Champion " + playerA.getUsername() + " versus the valiant " + playerB.getUsername() +
                ". May the bravest warrior prevail!\n";
        battleRepository.startLog(battleId, introLog);

        // Retrieve the decks of each player
        List<Card> playerADeck = new ArrayList<>(Arrays.asList(cardRepository.getUserDeckCards(playerA.getId())));
        List<Card> playerBDeck = new ArrayList<>(Arrays.asList(cardRepository.getUserDeckCards(playerB.getId())));

        // Log the initial state of each player's deck
        logDeckState("Initial", playerA, playerADeck, battleId);
        logDeckState("Initial", playerB, playerBDeck, battleId);

        // Initialize ELO points for both players
        int eloA = playerA.getEloRating();
        int eloB = playerB.getEloRating();

        // Conduct the battle rounds
        int round = 0;
        boolean isDraw = true;
        while (round < MAX_ROUNDS && !playerADeck.isEmpty() && !playerBDeck.isEmpty()) {
            round++;

            // Shuffle decks
            Collections.shuffle(playerADeck);
            Collections.shuffle(playerBDeck);

            // Determine the winner of each round
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
            eloRepository.updateELO(playerA.getId(), eloA);
            eloRepository.updateELO(playerB.getId(), eloB);
            battleRepository.crownWinner(battleId, finalWinner.getId());
            battleRepository.addToLog(battleId, finalWinner.getUsername() + " wins the battle with final ELO: " + (finalWinner.equals(playerA) ? eloA : eloB) + "\n");
        } else {
            // draw
            battleRepository.addToLog(battleId, "The battle ended in a draw.\n");
            battleRepository.crownWinner(battleId, null);
        }

        // Return the battle result
        return battleRepository.findBattleById(battleId).orElse(null);
    }

    /**
     * Logs the current state of a player's deck for a specific battle.
     *
     * @param state    A string describing the state of the deck (e.g., "Initial", "Final").
     * @param player   The player whose deck state is being logged.
     * @param deck     A list of cards in the player's deck.
     * @param battleId The unique identifier of the battle.
     */
    private void logDeckState(String state, User player, List<Card> deck, String battleId) {
        // Start building a log entry with the state and player's username
        StringBuilder log = new StringBuilder(state + " deck state for " + player.getUsername() + ": ");

        // Append the names of all cards in the deck to the log entry
        for (Card card : deck) {
            log.append(card.getName()).append(", ");
        }

        // Trim the final comma and space, then append a newline character
        if (log.length() > 0) {
            log.setLength(log.length() - 2); // Remove trailing comma and space
        }
        log.append("\n");

        // Add the constructed log entry to the battle log in the repository
        battleRepository.addToLog(battleId, log.toString());
    }


    /**
     * Saves the current state of a player's deck to their stack after a battle.
     *
     * @param player   The player whose deck is being saved.
     * @param deck     The list of cards in the player's deck after the battle.
     * @param battleId The unique identifier of the battle.
     */
    private void saveDeckToStack(User player, List<Card> deck, String battleId) {
        // Iterate over each card in the deck
        for (Card card : deck) {
            // Check if the card is already in the player's stack
            if (!cardRepository.isCardInStack(player.getId(), card.getId())) {
                // If the card is not in the stack, add it to the stack
                cardRepository.addCardToStack(player.getId(), card.getId());

                // Log the addition of the card to the player's stack
                battleRepository.addToLog(battleId, "Card " + card.getName() + " added to stack for " + player.getUsername() + ".\n");
            }
        }
    }


    /**
     * Determines the winner of a battle round between two cards.
     *
     * @param cardA The first card in the battle round of player A or B.
     * @param cardB The second card in the battle round of player A or B.
     * @return The winning card, or null if the round ends in a draw.
     */
    public Card determineRoundWinner(Card cardA, Card cardB) {
        // Check if any special rules apply to these cards
        if (isSpecialRuleApplicable(cardA, cardB)) {
            // Determine the winner based on special rules
            return getSpecialRuleWinner(cardA, cardB);
        }

        // If either card is a spell, use element type logic to determine the winner
        if (cardA.getCardType().equals("spell") || cardB.getCardType().equals("spell")) {
            // Calculate the effective damage for each card
            double damageA = getEffectiveDamage(cardA, cardB);
            double damageB = getEffectiveDamage(cardB, cardA);

            // Compare the damages to determine the winner
            if (damageA > damageB) return cardA;
            if (damageB > damageA) return cardB;
            // If damages are equal, the result is a draw
            return null;
        }

        // For non-spell cards, directly compare their damage values
        if (cardA.getDamage() > cardB.getDamage()) return cardA;
        if (cardB.getDamage() > cardA.getDamage()) return cardB;

        // If none of the above conditions apply, the round is a draw
        return null;
    }


    /**
     * Checks if any special rules apply to the given pair of cards.
     *
     * @param cardA The first card in the battle round.
     * @param cardB The second card in the battle round.
     * @return True if a special rule applies to the pair of cards, false otherwise.
     */
    private boolean isSpecialRuleApplicable(Card cardA, Card cardB) {
        // Check for specific matchups where special rules apply
        return (cardA.getName().equalsIgnoreCase("Goblin") && cardB.getName().equalsIgnoreCase("Dragon")) ||
                (cardA.getName().equalsIgnoreCase("Wizzard") && cardB.getName().equalsIgnoreCase("Ork")) ||
                (cardA.getName().equalsIgnoreCase("Knight") && cardB.getCardType().equalsIgnoreCase("spell") && cardB.getElementType().equalsIgnoreCase("Water")) ||
                (cardA.getName().equalsIgnoreCase("Kraken") && cardB.getCardType().equalsIgnoreCase("spell")) ||
                (cardA.getName().equalsIgnoreCase("FireElf") && cardB.getName().equalsIgnoreCase("Dragon"));
    }


    /**
     * Determines the winner of a battle round based on special rules.
     *
     * @param cardA The first card in the battle round.
     * @param cardB The second card in the battle round.
     * @return The card that wins according to the special rules, or null if the rules result in a draw.
     */
    private Card getSpecialRuleWinner(Card cardA, Card cardB) {
        // Determine the winner based on the matchup and special rules
        if (cardA.getName().equalsIgnoreCase("Goblin") && cardB.getName().equalsIgnoreCase("Dragon")) return cardB;
        if (cardA.getName().equalsIgnoreCase("Wizzard") && cardB.getName().equalsIgnoreCase("Ork")) return cardA;
        if (cardA.getName().equalsIgnoreCase("Knight") && cardB.getCardType().equalsIgnoreCase("spell") && cardB.getElementType().equalsIgnoreCase("Water"))
            return cardB;
        if (cardA.getName().equalsIgnoreCase("Kraken") && cardB.getCardType().equalsIgnoreCase("spell")) return cardA;
        if (cardA.getName().equalsIgnoreCase("FireElf") && cardB.getName().equalsIgnoreCase("Dragon")) return cardA;

        // If no special rule dictates a winner, the result is a draw
        return null;
    }


    /**
     * Calculates the effective damage of an attacking card against a defending card,
     * taking into account the elemental strengths and weaknesses.
     *
     * @param attacker The attacking card.
     * @param defender The defending card.
     * @return The effective damage value after considering elemental effects.
     */
    private double getEffectiveDamage(Card attacker, Card defender) {
        // Start with the base damage of the attacking card
        double damage = attacker.getDamage();

        // If the attacker is a spell card, adjust damage based on elemental types
        if (attacker.getCardType().equals("spell")) {
            switch (attacker.getElementType().toLowerCase()) {
                case "water":
                    // Water is effective against Fire but weak against Normal
                    if (defender.getElementType().equalsIgnoreCase("fire")) {
                        damage *= 2; // Double damage against Fire
                    } else if (defender.getElementType().equalsIgnoreCase("normal")) {
                        damage /= 2; // Half damage against Normal
                    }
                    break;
                case "fire":
                    // Fire is effective against Normal but weak against Water
                    if (defender.getElementType().equalsIgnoreCase("normal")) {
                        damage *= 2; // Double damage against Normal
                    } else if (defender.getElementType().equalsIgnoreCase("water")) {
                        damage /= 2; // Half damage against Water
                    }
                    break;
                case "normal":
                    // Normal is effective against Water but weak against Fire
                    if (defender.getElementType().equalsIgnoreCase("water")) {
                        damage *= 2; // Double damage against Water
                    } else if (defender.getElementType().equalsIgnoreCase("fire")) {
                        damage /= 2; // Half damage against Fire
                    }
                    break;
            }
        }
        // Return the adjusted damage value
        return damage;
    }


}