package at.technikum.apps.mtcg.entity;

import java.sql.Time;
import java.sql.Timestamp;

public class BattleResult {
    private String id;
    private User playerA;
    private User playerB;
    private String status;
    private User winner;
    private Timestamp start_time;
    private String battleLog;

    BattleResult(){}

    public BattleResult(String id, User playerA, String status){
        this.id = id;
        this.playerA = playerA;
        this.status = status;

    }

    public BattleResult(String id, User playerA, User playerB, String status, User winner, Timestamp start_time, String battleLog){
        this.id = id;
        this.playerA = playerA;
        this.playerB = playerB;
        this.status = status;
        this.winner = winner;
        this.start_time = start_time;
        this.battleLog = battleLog;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getPlayerA() {
        return playerA;
    }

    public void setPlayerA(User playerA) {
        this.playerA = playerA;
    }

    public User getPlayerB() {
        return playerB;
    }

    public void setPlayerB(User playerB) {
        this.playerB = playerB;
    }

    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public String getBattleLog() {
        return battleLog;
    }

    public void setBattleLog(String battleLog) {
        this.battleLog = battleLog;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getStart_time() {
        return start_time;
    }

    public void setStart_time(Timestamp start_time) {
        this.start_time = start_time;
    }
}
