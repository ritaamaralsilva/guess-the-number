package io.github.ritaamaralsilva;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.scene.Node;
import javafx.util.Duration;
import java.util.Objects;
import java.util.Random;
import java.net.URL;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;



public class GuessTheNumberApp extends Application {
    // static final para variaveis fixas aka regras do jogo . nr max de vidas nunca muda, e nr gerado random tem de ser entre 1 e 100 
    private static final int MAX_LIVES = 5;
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 100;

    private static final double HEART_FONT_SIZE = 22;

    private final Random randomNumberGenerator = new Random(); // gera o numero random para o jogo
    private Stage stage; // variavel de instancia para a janela do jogo, que vai ser usada para atualizar a interface

    // UI
    private HBox livesContainer;
    private Label feedbackLabel;
    private TextField guessInput;
    
    // Engine do jogo
    private GameState gameState;
    
    private Text[] heartNodes;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage; // atribui a janela principal à variavel de instancia para uso
        stage.setTitle("Guess The Number Game");
        stage.setResizable(false);

        showMainMenu(); // chama o metodo para mostrar o menu principal do jogo
        stage.show(); // exibe a janela do jogo
    }

    private void setSceneWithFade(Scene nextScene) {
        // aplica CSS sempre
        applyStyles(nextScene);
    
        // se for a primeira scene, mete direto
        if (stage.getScene() == null) {
            stage.setScene(nextScene);
            return;
        }
    
        // fade out da scene atual
        var currentRoot = stage.getScene().getRoot();
    
        FadeTransition fadeOut = new FadeTransition(Duration.millis(140), currentRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
    
        fadeOut.setOnFinished(ev -> {
            stage.setScene(nextScene);
    
            // fade in da nova scene
            var nextRoot = nextScene.getRoot();
            nextRoot.setOpacity(0.0);
    
            FadeTransition fadeIn = new FadeTransition(Duration.millis(180), nextRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
    
        fadeOut.play();
    }

    private void shake(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(55), node);
        tt.setFromX(0);
        tt.setToX(8);
        tt.setAutoReverse(true);
        tt.setCycleCount(6); // 6 * 55ms ~ 330ms
        tt.setInterpolator(Interpolator.EASE_BOTH);
    
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    private void playWinCelebration(Pane root, Label title) {
    // 1) flash verde (overlay via background color swap)
    // Nota: isto funciona bem se o teu CSS define background no .app-root.
    // Vamos só "piscar" via inline style temporário.
    String normalStyle = root.getStyle(); // pode ser "" se não houver inline style

    Timeline flashes = new Timeline(
            new KeyFrame(Duration.ZERO, e -> root.setStyle(normalStyle)),
            new KeyFrame(Duration.millis(400), e -> root.setStyle(normalStyle + "; -fx-background-color: #1fd17a;")),
            new KeyFrame(Duration.millis(800), e -> root.setStyle(normalStyle)),
            new KeyFrame(Duration.millis(1200), e -> root.setStyle(normalStyle + "; -fx-background-color: #1fd17a;")),
            new KeyFrame(Duration.millis(1600), e -> root.setStyle(normalStyle)),
            new KeyFrame(Duration.millis(2000), e -> root.setStyle(normalStyle + "; -fx-background-color: #1fd17a;")),
            new KeyFrame(Duration.millis(2800), e -> root.setStyle(normalStyle)),
            new KeyFrame(Duration.millis(3200), e -> root.setStyle(normalStyle + "; -fx-background-color: #1fd17a;")),
            new KeyFrame(Duration.millis(3600), e -> root.setStyle(normalStyle))

    );

    // 2) pop do título
    ScaleTransition pop = new ScaleTransition(Duration.millis(450), title);
    pop.setFromX(0.92);
    pop.setFromY(0.92);
    pop.setToX(1.08);
    pop.setToY(1.08);
    pop.setAutoReverse(true);
    pop.setCycleCount(5);
    pop.setInterpolator(Interpolator.EASE_OUT);

    // 3) pequena rotação/tilt do título
    RotateTransition tilt = new RotateTransition(Duration.millis(450), title);
    tilt.setFromAngle(0);
    tilt.setToAngle(-6);
    tilt.setAutoReverse(true);
    tilt.setCycleCount(5);
    tilt.setInterpolator(Interpolator.EASE_BOTH);

    new ParallelTransition(flashes, pop, tilt).play();
}

    private void applyStyles(Scene scene) {
        URL css = Objects.requireNonNull(
                getClass().getResource("/styles.css"),
                "styles.css not found. Put it in: src/main/resources/styles.css"
        );
        scene.getStylesheets().add(css.toExternalForm());
    }


    private void showMainMenu() {
        Label title = new Label("Guess The Number");
        title.getStyleClass().add("title");

        Button startGameButton = new Button("Start Game");
        startGameButton.setOnAction(e -> startNewGame()); // inicia o jogo quando o botão é clicado
        startGameButton.getStyleClass().add("menu-button");

        Button rulesButton = new Button("Rules");
        rulesButton.setOnAction(e -> showRulesScreen());
        rulesButton.getStyleClass().add("menu-button");

        Button aboutButton = new Button("About");
        aboutButton.setOnAction(e -> showAboutScreen());
        aboutButton.getStyleClass().add("menu-button");

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> stage.close()); // fecha o jogo quando o botão é clicado
        exitButton.getStyleClass().add("menu-button");

        VBox menuLayout = new VBox(20, title, startGameButton, rulesButton, aboutButton, exitButton);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setPadding(new Insets(20));
        menuLayout.setMinWidth(520);
        menuLayout.setMinHeight(360);

        menuLayout.getStyleClass().addAll("app-root", "menu-layout", "screen-menu");

        Scene scene = new Scene(menuLayout, 520, 360);
        applyStyles(scene);
        stage.setScene(scene);
    }

    private void startNewGame() {
        if (gameState == null) {
            gameState = new GameState(MIN_NUMBER, MAX_NUMBER, MAX_LIVES, randomNumberGenerator);
        } else {
            gameState.newGame();
        }

        // Top bar com corações (vidas)
        livesContainer = new HBox(6);
        livesContainer.setAlignment(Pos.CENTER_RIGHT);
        livesContainer.getStyleClass().add("lives");
        updateLivesDisplay();

        HBox topBar = new HBox(livesContainer);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(12));
        topBar.getStyleClass().add("top-bar");

        // Centro do jogo
        Label promptLabel = new Label("Guess a number between " + gameState.minNumber() + " and " + gameState.maxNumber());
        promptLabel.getStyleClass().add("subtitle");

        guessInput = new TextField();
        guessInput.setPromptText("Enter your guess");
        guessInput.setMaxWidth(220);

        Button guessButton = new Button("Guess");
        guessButton.setDefaultButton(true);

        Runnable submit = () -> {
            String raw = guessInput.getText();
            if (raw == null || raw.trim().isEmpty()) {
                feedbackLabel.setText("Enter a valid number.");
                guessInput.requestFocus();
            return;
            }

            processGuess(raw);

            guessInput.clear();
            guessInput.requestFocus();
        };

        guessButton.setOnAction(e -> submit.run());
        guessInput.setOnAction(e -> submit.run());
        

        HBox inputLayout = new HBox(10, guessInput, guessButton);
        inputLayout.setAlignment(Pos.CENTER);

        feedbackLabel = new Label("");
        feedbackLabel.setId("feedback");

        feedbackLabel.setWrapText(true);
        feedbackLabel.setTextAlignment(TextAlignment.CENTER);
        feedbackLabel.setAlignment(Pos.CENTER);
        feedbackLabel.setMaxWidth(Double.MAX_VALUE);

        feedbackLabel.maxWidthProperty().bind(inputLayout.widthProperty());
        feedbackLabel.prefWidthProperty().bind(inputLayout.widthProperty());

        VBox center = new VBox(18, promptLabel, inputLayout, feedbackLabel);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(20));
        center.getStyleClass().add("game-layout");

        // barra inferior com botão para voltar ao menu
        Button backToMenu = new Button("Back to Menu");
        backToMenu.setOnAction(e -> showMainMenu());

        HBox bottomBar = new HBox(backToMenu);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(12));
        bottomBar.getStyleClass().add("bottom-bar");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(center);
        root.setBottom(bottomBar);

        root.getStyleClass().add("app-root");
        root.getStyleClass().add("screen-game");

        Scene scene = new Scene(root, 520, 360);
        applyStyles(scene);
        stage.setScene(scene);
        guessInput.requestFocus();
    }

    private void animateLifeLost(int heartIndex) {
        if (heartNodes == null) return;
        if (heartIndex < 0 || heartIndex >= heartNodes.length) return;
    
        Text heart = heartNodes[heartIndex];
    
        ScaleTransition pop = new ScaleTransition(Duration.millis(140), heart);
        pop.setFromX(1.0);
        pop.setFromY(1.0);
        pop.setToX(1.35);
        pop.setToY(1.35);
        pop.setInterpolator(Interpolator.EASE_OUT);
    
        TranslateTransition shake = new TranslateTransition(Duration.millis(140), heart);
        shake.setFromX(0);
        shake.setToX(6);
        shake.setAutoReverse(true);
        shake.setCycleCount(4);
    
        FadeTransition fade = new FadeTransition(Duration.millis(180), heart);
        fade.setFromValue(1.0);
        fade.setToValue(0.75);
    
        ParallelTransition combo = new ParallelTransition(pop, shake, fade);
        combo.setOnFinished(e -> {
            heart.setScaleX(1.0);
            heart.setScaleY(1.0);
            heart.setTranslateX(0.0);
            heart.setOpacity(1.0);
        });
        combo.play();
    }

    private void processGuess(String rawInput) {
        String trimmed = rawInput == null ? "" : rawInput.trim();

        Integer guess = parseIntOrNull(trimmed);
        if (guess == null) {
            feedbackLabel.setText("Enter a valid number.");
            shake(guessInput);
            return;
        }

        int beforeLives = gameState.lives();
        GuessResult result = gameState.guess(guess);
        int afterLives = gameState.lives();
        updateLivesDisplay();
        
        if (afterLives < beforeLives) {
            animateLifeLost(beforeLives - 1); 
        }

        switch (result) {
            case INVALID -> {
                feedbackLabel.setText(
                    "Out of range. Use " + gameState.minNumber() + "–" + gameState.maxNumber() + ".");
                    shake(guessInput);
            }

            case CORRECT -> showWinScreen();

            case HIGHER -> feedbackLabel.setText("Wrong. The correct number is higher.");

            case LOWER -> feedbackLabel.setText("Wrong. The correct number is lower.");

            case GAME_OVER -> showGameOverScreen();
        }
    }

    private void updateLivesDisplay() {
        livesContainer.getChildren().clear();

        int lives = gameState.lives();
        int maxLives = gameState.maxLives();

        heartNodes = new Text[maxLives];

        for (int i = 0; i < maxLives; i++) {
            Text heart = new Text("♥");
            heart.setFont(Font.font(HEART_FONT_SIZE));
            heart.setFill(i < lives ? Color.RED : Color.BLACK);
            heart.getStyleClass().add("heart");

            heartNodes[i] = heart;
            livesContainer.getChildren().add(heart);
        }
    }
    
    private void showWinScreen() {
        Label title = new Label("You Won");
        title.getStyleClass().add("title");

        Label hint = new Label("You guessed the number with " + gameState.lives() + " lives remaining.");
        hint.getStyleClass().add("congrats");

        Button newGameButton = new Button("New Game");
        newGameButton.setOnAction(e -> startNewGame());
        newGameButton.getStyleClass().add("win-button");

        Button quitButton = new Button("Quit");
        quitButton.setOnAction(e -> stage.close());
        quitButton.getStyleClass().add("win-button");

        VBox layout = new VBox(18, title, hint, newGameButton, quitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24));
        layout.setMinWidth(520);
        layout.setMinHeight(360);

        layout.getStyleClass().addAll("app-root", "screen-win");
        layout.setOpacity(0.0);

        Scene scene = new Scene(layout, 520, 360);
        setSceneWithFade(scene);

        playWinCelebration(layout, title);
        stage.setScene(scene);
    }

    private void showGameOverScreen() {
        Label title = new Label("GAME OVER");
        title.getStyleClass().add("title");

        Label hint = new Label("The number was: " + gameState.targetNumber());
        hint.getStyleClass().add("subtitle");

        Button newGameButton = new Button("New Game");
        newGameButton.setOnAction(e -> startNewGame());
        newGameButton.getStyleClass().add("over-button");

        Button quitButton = new Button("Quit");
        quitButton.setOnAction(e -> stage.close());
        quitButton.getStyleClass().add("over-button");

        VBox layout = new VBox(18, title, hint, newGameButton, quitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24));
        layout.setMinWidth(520);
        layout.setMinHeight(360);

        layout.getStyleClass().addAll("app-root", "screen-gameover", "over-button", "over-button:hover");
        layout.setOpacity(0.0);

        Scene scene = new Scene(layout, 520, 360);
        setSceneWithFade(scene);
        stage.setScene(scene);
    }

    private void showRulesScreen() {
        Label title = new Label("Rules");
        title.getStyleClass().add("title");

        Label rules = new Label(
            "• The game picks a random number between " + MIN_NUMBER + " and " + MAX_NUMBER + ".\n" +
            "• You have " + MAX_LIVES + " lives.\n" +
            "• Each wrong guess costs 1 life.\n" +
            "• You’ll be told whether the correct number is higher or lower.\n" +
            "• At 0 lives: GAME OVER."
        );
        rules.getStyleClass().add("body-text");
        rules.setWrapText(true);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("secondary");
        backButton.setOnAction(e -> showMainMenu());

        VBox layout = new VBox(18, title, rules, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24));
        layout.setMinWidth(520);
        layout.setMinHeight(360);

        layout.getStyleClass().addAll("app-root", "screen-rules");

        Scene scene = new Scene(layout, 520, 360);
        applyStyles(scene);
        stage.setScene(scene);
    }

    private void showAboutScreen() {
        Label title = new Label("About");
        title.getStyleClass().add("title");

        Text tGame = new Text("Guess The Number");
        tGame.getStyleClass().addAll("about-text", "about-bold"); 

        Text tBy = new Text("\nCreated by ");
        tBy.getStyleClass().add("about-text");

        Text tName = new Text("Rita Silva");
        tName.getStyleClass().addAll("about-text", "about-bold");

        Text tBuilt = new Text("\n\nBuilt with ");
        tBuilt.getStyleClass().add("about-text");

        Text tTech = new Text("Java & JavaFX");
        tTech.getStyleClass().add("about-text");

        Text tYear = new Text("\n© 2026");
        tYear.getStyleClass().add("about-text");

        TextFlow about = new TextFlow(tGame, tBy, tName, tBuilt, tTech, tYear);
        about.setTextAlignment(TextAlignment.CENTER);
        about.setMaxWidth(420);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("secondary");
        backButton.setOnAction(e -> showMainMenu());

        VBox layout = new VBox(18, title, about, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24));
        layout.setMinWidth(520);
        layout.setMinHeight(360);

        layout.getStyleClass().addAll("app-root", "screen-about");

        Scene scene = new Scene(layout, 520, 360);
        applyStyles(scene);
        stage.setScene(scene);
    }

    private static Integer parseIntOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}