package at.technikum.apps.mtcg;

import at.technikum.apps.mtcg.controller.*;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.repository.battle.BattleRepository;
import at.technikum.apps.mtcg.repository.battle.BattleRepository_db;
import at.technikum.apps.mtcg.repository.card.CardRepository;
import at.technikum.apps.mtcg.repository.card.CardRepository_db;
import at.technikum.apps.mtcg.repository.coins.CoinRepository;
import at.technikum.apps.mtcg.repository.coins.CoinRepository_db;
import at.technikum.apps.mtcg.repository.elo.ELORepository;
import at.technikum.apps.mtcg.repository.elo.ELORepository_db;
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
import at.technikum.apps.mtcg.repository.wheel.WheelOfFortuneRepository;
import at.technikum.apps.mtcg.repository.wheel.WheelOfFortuneRepository_db;
import at.technikum.apps.mtcg.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Injector {
    public List<Controller> createController() {
        //Database initialisieren
        Database database = new Database();
        Random random = new Random();
        // Repositories initialisieren
        UserRepository userRepository = new UserRepository_db(database);
        CardRepository cardRepository = new CardRepository_db(database);
        SessionRepository sessionRepository = new SessionRepository_db(database);
        BattleRepository battleRepository = new BattleRepository_db(database);
        PackageRepository packageRepository = new PackageRepository_db(database);
        ScoreboardRepository scoreboardRepository = new ScoreboardRepository_db(database);
        StatsRepository statsRepository = new StatsRepository_db(database);
        TradingRepository tradingRepository = new TradingRepository_db(database);
        WheelOfFortuneRepository wheelOfFortuneRepository = new WheelOfFortuneRepository_db(database);
        CoinRepository coinRepository = new CoinRepository_db(database);
        ELORepository eloRepository = new ELORepository_db(database);

        // Services initialisieren

        HashingService hashingService = new HashingService();
        SessionService sessionService = new SessionService(userRepository, sessionRepository, hashingService);
        UserService userService = new UserService(userRepository, hashingService);
        CardService cardService = new CardService(cardRepository, sessionService);
        DeckService deckService = new DeckService(cardRepository, sessionService);
        PackageService packageService = new PackageService(cardRepository, packageRepository, sessionService);
        ScoreboardService scoreboardService = new ScoreboardService(scoreboardRepository, sessionService);
        StatsService statsService = new StatsService(statsRepository, sessionService);
        TradingService tradingService = new TradingService(tradingRepository, cardRepository, userRepository, sessionService);
        TransactionsService transactionsService = new TransactionsService(cardRepository, userRepository, packageRepository, sessionService, coinRepository);
        WheelOfFortuneService wheelOfFortuneService = new WheelOfFortuneService(wheelOfFortuneRepository, userRepository, cardRepository, coinRepository, random);

        // Battle-Logik und Battle-Warteschlange initialisieren
        BattleLogic battleLogic = new BattleLogic(battleRepository, userRepository, cardRepository, eloRepository);
        ConcurrentHashMap<String, BattleResult> battlesWaiting = new ConcurrentHashMap<>();
        BattleService battleService = new BattleService(battleRepository, battleLogic, battlesWaiting, sessionService, deckService);

        // Controller mit Services initialisieren
        List<Controller> controllerList = new ArrayList<>();
        controllerList.add(new SessionController(sessionService));
        controllerList.add(new UserController(userService, sessionService));
        controllerList.add(new CardController(cardService, sessionService, userService));
        controllerList.add(new DeckController(deckService, sessionService, userService, cardService));
        controllerList.add(new PackageController(packageService, sessionService, userService, cardService));
        controllerList.add(new ScoreboardController(scoreboardService, sessionService, userService));
        controllerList.add(new StatsController(statsService, sessionService, userService));
        controllerList.add(new TradingController(tradingService, sessionService, cardService, userService, deckService));
        controllerList.add(new TransactionsController(transactionsService, sessionService));
        controllerList.add(new BattleController(battleService, sessionService, userService, deckService));
        controllerList.add(new WheelOfFortuneController(wheelOfFortuneService, sessionService));

        return controllerList;
    }
}

