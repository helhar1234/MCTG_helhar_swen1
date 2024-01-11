package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserStats;
import at.technikum.apps.mtcg.repository.scoreboard.ScoreboardRepository;
import at.technikum.server.http.Request;

public class ScoreboardService {

    private final ScoreboardRepository scoreboardRepository;
    private final SessionService sessionService;

    public ScoreboardService(ScoreboardRepository scoreboardRepository, SessionService sessionService) {
        this.scoreboardRepository = scoreboardRepository;
        this.sessionService = sessionService;
    }

    public UserStats[] getScoreboard(Request request) {
        User requester = sessionService.authenticateRequest(request);
        return scoreboardRepository.getScoreboard();
    }
}
