package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.TradeRequest;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.trading.TradingRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.server.http.HttpStatus;

import java.util.Optional;

public class TradingService {
    private final TradingRepository tradingRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public TradingService(TradingRepository tradingRepository, CardRepository cardRepository, UserRepository userRepository) {
        this.tradingRepository = tradingRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a trade request by its ID.
     *
     * @param id The ID of the trade request.
     * @return An Optional containing the TradeRequest if found, or an empty Optional if not found.
     */
    public Optional<TradeRequest> getTradeById(String id) {
        // Delegate to the tradingRepository to find the trade request by ID
        return tradingRepository.getTradeById(id);
    }


    /**
     * Creates a new trade request.
     *
     * @param user         The user who is creating the trade.
     * @param tradeRequest The details of the trade request.
     * @return True if the trade request is successfully created, false otherwise.
     * @throws HttpStatusException If the user doesn't own the card or a trade with the same ID already exists.
     */
    public boolean createTrade(User user, TradeRequest tradeRequest) {
        // Validate that the user owns the card and it's not in their deck
        if (!cardRepository.isCardInStack(user.getId(), tradeRequest.getCardToTrade()) ||
                cardRepository.isCardInDeck(tradeRequest.getCardToTrade(), user.getId())) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN,
                    "The deal contains a card that is not owned by the user or locked in the deck.");
        }

        // Check if a trade with the same ID already exists
        if (tradingRepository.getTradeById(tradeRequest.getId()).isPresent()) {
            throw new HttpStatusException(HttpStatus.CONFLICT,
                    "Conflict: A deal with this deal ID already exists.");
        }

        // Create the trade request in the repository
        return tradingRepository.createTrade(tradeRequest, user.getId());
    }


    /**
     * Retrieves all trading deals available.
     *
     * @param user The user requesting the trading deals.
     * @return An array of TradeRequest objects.
     * @throws HttpStatusException If no trading deals are available.
     */
    public TradeRequest[] getAllTrades(User user) {
        TradeRequest[] trades = tradingRepository.getAllTrades();

        // Check if there are any trading deals available
        if (trades == null || trades.length == 0) {
            throw new HttpStatusException(HttpStatus.OK, "No Trading Deals");
        }

        // Enrich each trade request with additional details like card and user information
        for (TradeRequest trade : trades) {
            // Enrich with card details
            cardRepository.findCardById(trade.getCardToTrade()).ifPresent(card -> {
                trade.setCardName(card.getName());
                trade.setCardDamage(card.getDamage());
            });

            // Enrich with user details
            userRepository.findUserById(trade.getUserId()).ifPresent(trader -> trade.setUsername(trader.getUsername()));
        }

        return trades;
    }


    /**
     * Checks whether a specific trade is associated with a given user.
     *
     * @param userId    The ID of the user.
     * @param tradingId The ID of the trading deal.
     * @return True if the user is associated with the trade, false otherwise.
     */
    public boolean isUserTrade(String userId, String tradingId) {
        // Check if the trade is associated with the user
        return tradingRepository.isUserTrade(userId, tradingId);
    }


    /**
     * Deletes a trading deal.
     *
     * @param user      The user requesting to delete the trade.
     * @param tradingId The ID of the trade to be deleted.
     * @return True if the trade is successfully deleted, false otherwise.
     * @throws HttpStatusException If the trade does not belong to the user or the trade ID is not found.
     */
    public boolean deleteTrade(User user, String tradingId) {
        // Check if the trade belongs to the user and if it exists
        if (!isUserTrade(user.getId(), tradingId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "The deal is not owned by the user.");
        } else if (getTradeById(tradingId).isEmpty()) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Deal ID Not Found.");
        }

        // Delete the trade from the repository
        return tradingRepository.deleteTrade(tradingId);
    }


    /**
     * Checks if an offered card meets the requirements of a trade request.
     *
     * @param tradeRequest  The trade request against which to check the card.
     * @param offeredCardId The ID of the offered card.
     * @return True if the offered card meets the trade requirements, false otherwise.
     */
    public boolean meetsRequirements(TradeRequest tradeRequest, String offeredCardId) {
        Optional<Card> cardOptional = cardRepository.findCardById(offeredCardId);

        // Check if the card exists and meets the trade requirements
        if (cardOptional.isPresent()) {
            Card card = cardOptional.get();
            boolean typeMatches = tradeRequest.getType().equals(card.getCardType());
            boolean damageMeets = tradeRequest.getMinimumDamage() <= card.getDamage();
            return typeMatches && damageMeets;
        } else {
            return false;
        }
    }


    /**
     * Executes a trading deal.
     *
     * @param requester     The User requesting the trade.
     * @param tradingId     The ID of the trading deal to be executed.
     * @param offeredCardId The ID of the card being offered in the trade.
     * @return True if the trade is successfully executed, false otherwise.
     * @throws HttpStatusException If the trading deal is not found, or the user cannot trade with themselves, or if the offered card is not valid.
     */
    public boolean trade(User requester, String tradingId, String offeredCardId) {

        // Retrieve the trade request
        Optional<TradeRequest> trade = getTradeById(tradingId);
        if (trade.isEmpty()) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Trading deal not found");
        } else if (requester.getId().equals(trade.get().getUserId())) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "User cannot trade with themselves");
        }

        // Validate the offered card
        if (!cardRepository.isCardInStack(requester.getId(), offeredCardId) ||
                cardRepository.isCardInDeck(requester.getId(), offeredCardId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "The offered card is not owned by the user or is locked in the deck");
        }

        // Check if the offered card meets the trade requirements
        if (!meetsRequirements(trade.get(), offeredCardId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "The offered card does not meet the trade requirements or is locked in the deck.");
        }

        // Execute the trade by swapping cards and deleting the trade
        return cardRepository.deleteCardFromStack(requester.getId(), offeredCardId) &&
                cardRepository.addCardToStack(trade.get().getUserId(), offeredCardId) &&
                cardRepository.deleteCardFromStack(trade.get().getUserId(), trade.get().getCardToTrade()) &&
                cardRepository.addCardToStack(requester.getId(), offeredCardId) &&
                tradingRepository.deleteTrade(tradingId);
    }

}
