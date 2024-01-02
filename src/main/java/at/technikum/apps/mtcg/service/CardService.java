package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.repository.CardRepository;
import at.technikum.apps.mtcg.repository.CardRepository_db;

import java.util.Optional;

public class CardService {
    private final CardRepository cardRepository;

    public CardService() {
        this.cardRepository = new CardRepository_db();
    }

    public Optional<Card> findCardById(String id) {
        return cardRepository.findCardById(id);
    }
}
