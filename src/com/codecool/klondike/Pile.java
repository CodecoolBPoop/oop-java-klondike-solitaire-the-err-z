package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.ListIterator;
import java.util.Observable;

public class Pile extends Pane {

    private PileType pileType;
    private String name;
    private double cardGap;
    private ObservableList<Card> cards = FXCollections.observableArrayList();

    public Pile(PileType pileType, String name, double cardGap) {
        this.pileType = pileType;
        this.cardGap = cardGap;
    }

    public PileType getPileType() {
        return pileType;
    }

    public String getName() {
        return name;
    }

    public double getCardGap() {
        return cardGap;
    }

    public int numOfCards() {
        //TODO
        return 1;
    }

    public boolean topCardIsFaceDown() {
        // TODO: remove this when autoflip is implemented
        return !isEmpty() && cards.get(cards.size()-1).isFaceDown();
    }

    public boolean acceptsAsFirst(Card card) {
        switch (pileType) {
        case FOUNDATION:
            return card.getRank() == Card.Rank.ACE;
        case TABLEAU:
            return card.getRank() == Card.Rank.KING;
        default:
            return false;
        }
    }

    /**
     * Shall be called only when there _is_ a top card.
     */
    public boolean canPlaceOnTop(Card card) {
        final boolean areOppositeColor = Card.isOppositeColor(getTopCard(), card);
        final boolean areSameSuit = Card.isSameSuit(getTopCard(), card);
        final boolean areAscending = Card.areAscending(getTopCard(), card);
        final boolean areDescending = Card.areDescending(getTopCard(), card);
        switch (pileType) {
        case FOUNDATION:
            return areSameSuit && areAscending;
        case TABLEAU:
            return areOppositeColor && areDescending;
        default:
            return false;
        }
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void clear() {
        //TODO
    }

    public void remove(Card card) {
        cards.remove(card);
    }

    public void addCard(Card card) {
        cards.add(card);
        card.setContainingPile(this);
        card.toFront();
        layoutCard(card);
    }

    private void layoutCard(Card card) {
        card.relocate(card.getLayoutX() + card.getTranslateX(), card.getLayoutY() + card.getTranslateY());
        card.setTranslateX(0);
        card.setTranslateY(0);
        card.setLayoutX(getLayoutX());
        card.setLayoutY(getLayoutY() + (cards.size() - 1) * cardGap);
    }

    public Card getTopCard() {
        if (cards.isEmpty())
            return null;
        else
            return cards.get(cards.size() - 1);
    }

    public void setBlurredBackground() {
        setPrefSize(Card.WIDTH, Card.HEIGHT);
        BackgroundFill backgroundFill = new BackgroundFill(Color.gray(0.0, 0.2), null, null);
        Background background = new Background(backgroundFill);
        GaussianBlur gaussianBlur = new GaussianBlur(10);
        setBackground(background);
        setEffect(gaussianBlur);
    }

    public ObservableList<Card> getCards() {
        return cards;
    }

    public enum PileType {
        STOCK,
        DISCARD,
        FOUNDATION,
        TABLEAU
    }
}
