package at.technikum.apps.mtcg.dto;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;

public class WheelPrize {
    private User spinner;
    private String prizeType;
    private int coinAmount;
    private Card wonCard;

    public WheelPrize() {
    }

    public WheelPrize(User spinner, String prizeType, int coinAmount, Card wonCard) {
        this.spinner = spinner;
        this.prizeType = prizeType;
        this.coinAmount = coinAmount;
        this.wonCard = wonCard;
    }

    public User getSpinner() {
        return spinner;
    }

    public void setSpinner(User spinner) {
        this.spinner = spinner;
    }

    public String getPrizeType() {
        return prizeType;
    }

    public void setPrizeType(String prizeType) {
        this.prizeType = prizeType;
    }

    public int getCoinAmount() {
        return coinAmount;
    }

    public void setCoinAmount(int coinAmount) {
        this.coinAmount = coinAmount;
    }

    public Card getWonCard() {
        return wonCard;
    }

    public void setWonCard(Card wonCard) {
        this.wonCard = wonCard;
    }
}

