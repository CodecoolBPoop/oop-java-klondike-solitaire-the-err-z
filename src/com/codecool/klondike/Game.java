package com.codecool.klondike;


import com.sun.org.apache.bcel.internal.generic.ObjectType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import sun.awt.SunHints;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            if (card.getContainingPile().getTopCard() == card){
                card.moveToPile(discardPile);
                card.flip();
                card.setMouseTransparent(false);
                System.out.println("Placed " + card + " to the waste.");
            }
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        ObservableList<Card> movedCards = activePile.getCards();
        //moveMultipleCards(movedCards);
        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();
        boolean findChosenCard = false;
        for (Card chosenCard:movedCards) {
            if (chosenCard.equals(card) && !card.isFaceDown()) {
                findChosenCard = true;
                draggedCards.add(card);
                card.getDropShadow().setRadius(20);
                card.getDropShadow().setOffsetX(10);
                card.getDropShadow().setOffsetY(10);

                card.toFront();
                card.setTranslateX(offsetX);
                card.setTranslateY(offsetY);
            } else if (findChosenCard && !chosenCard.isFaceDown()){
                draggedCards.add(chosenCard);
                chosenCard.getDropShadow().setRadius(20);
                chosenCard.getDropShadow().setOffsetX(10);
                chosenCard.getDropShadow().setOffsetY(10);

                chosenCard.toFront();
                chosenCard.setTranslateX(offsetX);
                chosenCard.setTranslateY(offsetY);
            }continue;

                //for (int j = i + 1; j < ((ObservableList) movedCards).size(); j++) {
//                    System.out.println("Click");
//                    System.out.println(card + " " + i );
//                    System.out.println(((ObservableList) movedCards).get(j) + " " + j);
                //}
          //  }
        }
        //draggedCards.add(card);

    };
    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile pile = getValidIntersectingPile(card, tableauPiles);
        Pile piles = getValidIntersectingPile(card, foundationPiles);
        //TODO
        if (piles != null && draggedCards.size() == 1) {
            handleValidMove(card, piles);

        } else if (pile != null) {

            handleValidMove(card, pile);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
    };

    public void moveMultipleCards(Object aObject) {
        for (int i = 0; i < ((ObservableList) aObject).size(); i++) {
            System.out.println(((ObservableList) aObject).get(i));
        }
    }

    public boolean isGameWon() {
        //TODO
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
        winningPopUpWindow();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        //TODO
        if (stockPile.isEmpty()) {
            while (!discardPile.isEmpty()) {
                discardPile.getTopCard().flip();
                discardPile.getTopCard().moveToPile(stockPile);

            }


        }
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean checkEmptyTableauPiles() {

        Integer emptyPilesCounter = 0;
        boolean isItTrue = false;

        for ( Pile tablePile : foundationPiles){
            if (tablePile.isEmpty()){
                emptyPilesCounter++;
            }
        }
        if ( emptyPilesCounter == 6){
            isItTrue = true;
        }
        return isItTrue;
    }

    public void winningPopUpWindow(){
        if ( stockPile.isEmpty() && discardPile.isEmpty() && checkEmptyTableauPiles()){
            System.out.println("win");

        }
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile.topCardIsFaceDown()) {
            return false;
        }

        if (destPile.isEmpty()) {
            return destPile.acceptsAsFirst(card);
        }
        return destPile.canPlaceOnTop(card);
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);

        }
    }

    public void dealCards() {
        //TODO
        Iterator<Card> deckIterator = deck.iterator();
        int tableauPileLength = tableauPiles.size();
        for (int i = 0; i < tableauPileLength; i++) {
            for (int j = 0; j < i + 1; j++) {
                Card card = deckIterator.next();
                tableauPiles.get(i).addCard(card);
                addMouseEventHandlers(card);
                getChildren().add(card);
                if (j == i) {
                    card.flip();
                }
            }
        }

        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);

        });

        // list change listener
        for (Pile i : tableauPiles) {
            i.getCards().addListener((ListChangeListener<Card>) c -> {
                if (!i.isEmpty() && i.getTopCard().isFaceDown()) {
                    i.getTopCard().flip();
                }
            });

        }
    }


    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }


}
