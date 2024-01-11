package at.technikum.apps.mtcg;

import at.technikum.apps.mtcg.controller.*;
import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.repository.battle.BattleRepository;
import at.technikum.apps.mtcg.repository.battle.BattleRepository_db;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.card.CardRepository_db;
import at.technikum.apps.mtcg.repository.packages.PackageRepository;
import at.technikum.apps.mtcg.repository.packages.PackageRepository_db;
import at.technikum.apps.mtcg.repository.scoreboard.ScoreboardRepository;
import at.technikum.apps.mtcg.repository.scoreboard.ScoreboardRepository_db;
import at.technikum.apps.mtcg.repository.session.SessionRepository;
import at.technikum.apps.mtcg.repository.session.SessionRepository_db;
import at.technikum.apps.mtcg.repository.stats.StatsRepository;
import at.technikum.apps.mtcg.repository.stats.StatsRepository_db;
import at.technikum.apps.mtcg.repository.trading.TradingRepository;
import at.technikum.apps.mtcg.repository.trading.TradingRepository_db;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository_db;
import at.technikum.apps.mtcg.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// TODO: ADD INJECTOR TO MtcgApp
public class Injector {
    public List<Controller> createController() {
        // Repositories initialisieren
        UserRepository userRepository = new UserRepository_db();
        CardRepository cardRepository = new CardRepository_db();
        SessionRepository sessionRepository = new SessionRepository_db();
        BattleRepository battleRepository = new BattleRepository_db();
        PackageRepository packageRepository = new PackageRepository_db();
        ScoreboardRepository scoreboardRepository = new ScoreboardRepository_db();
        StatsRepository statsRepository = new StatsRepository_db();
        TradingRepository tradingRepository = new TradingRepository_db();

        // Services initialisieren
        SessionService sessionService = new SessionService(userRepository, sessionRepository);
        UserService userService = new UserService(userRepository, sessionService);
        CardService cardService = new CardService(cardRepository, sessionService);
        DeckService deckService = new DeckService(cardRepository, sessionService);
        PackageService packageService = new PackageService(cardRepository, packageRepository, sessionService);
        ScoreboardService scoreboardService = new ScoreboardService(scoreboardRepository, sessionService);
        StatsService statsService = new StatsService(statsRepository, sessionService);
        TradingService tradingService = new TradingService(tradingRepository, cardRepository, userRepository, sessionService);
        TransactionsService transactionsService = new TransactionsService(cardRepository, userRepository, packageRepository, sessionService);

        // Battle-Logik und Battle-Warteschlange initialisieren
        BattleLogic battleLogic = new BattleLogic(battleRepository, userRepository, cardRepository);
        ConcurrentHashMap<String, BattleResult> battlesWaiting = new ConcurrentHashMap<>();
        BattleService battleService = new BattleService(battleRepository, battleLogic, battlesWaiting, sessionService, deckService);

        // Controller mit Services initialisieren
        List<Controller> controllerList = new ArrayList<>();
        controllerList.add(new SessionController(sessionService));
        controllerList.add(new UserController(userService));
        controllerList.add(new CardController(cardService, sessionService, userService));
        controllerList.add(new DeckController(deckService, sessionService, userService, cardService));
        controllerList.add(new PackageController(packageService, sessionService, userService, cardService));
        controllerList.add(new ScoreboardController(scoreboardService, sessionService, userService));
        controllerList.add(new StatsController(statsService, sessionService, userService));
        controllerList.add(new TradingController(tradingService, sessionService, cardService, userService, deckService));
        controllerList.add(new TransactionsController(transactionsService));
        controllerList.add(new BattleController(battleService, sessionService, userService, deckService));

        return controllerList;
    }
}

