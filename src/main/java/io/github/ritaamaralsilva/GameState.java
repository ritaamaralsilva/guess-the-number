package io.github.ritaamaralsilva;

import java.util.Random;

public final class GameState {

    private final int MIN_NUMBER;
    private final int MAX_NUMBER;
    private final int MAX_LIVES;
    private final Random randomNumberGenerator;

    private int targetNumber;
    private int lives;

    public GameState(int minNumber, int maxNumber, int maxLives, Random randomNumberGenerator) {
        if (minNumber >= maxNumber) {
            throw new IllegalArgumentException("minNumber must be less than maxNumber");
        }
        if (maxLives <= 0) {
            throw new IllegalArgumentException("maxLives must be greater than 0");
        }   
        if (randomNumberGenerator == null) {
            throw new IllegalArgumentException("randomNumberGenerator cannot be null");
        }   
        
        this.MIN_NUMBER = minNumber;
        this.MAX_NUMBER = maxNumber;
        this.MAX_LIVES = maxLives;
        this.randomNumberGenerator = new Random();

        newGame();
    }

    public void newGame() {
        this.targetNumber = randomNumberGenerator.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
        this.lives = MAX_LIVES;
    }   
    
    public int lives() {
        return lives;
    }   

    public int maxLives() {
        return MAX_LIVES;
    }   

    public int minNumber() {
        return MIN_NUMBER;
    }

    public int maxNumber() {
        return MAX_NUMBER;
    }   
    
    // mostra número gerado aleatoriamente para o jogo, mas apenas no ecrã de game over, para que o jogador saiba qual era o número a adivinhar
    public int targetNumber() {
        return targetNumber;
    }   

    public GuessResult guess(int n) {
        if (n < MIN_NUMBER || n > MAX_NUMBER) return GuessResult.INVALID;

        if (n == targetNumber) return GuessResult.CORRECT;

        lives--;
        if (lives <= 0) return GuessResult.GAME_OVER;

        return n < targetNumber ? GuessResult.HIGHER : GuessResult.LOWER;
    }
}
