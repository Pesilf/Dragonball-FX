package main;

import static constants.Constants.MAX_LIFE;
import static constants.Constants.PLAYER1X;
import static constants.Constants.PLAYER2X;
import static constants.Constants.PLAYERSY;
import static constants.Constants.SECRET;
import static view.MainMenu.setupStage;
import static view.StageSelectionScreen.createBackground;
import static constants.Constants.BACKGROUND_HEIGHT;
import static constants.Constants.BACKGROUND_WIDTH;
import static constants.Constants.BATTLE_SONG;
import static constants.Constants.ENTER;
import static constants.Constants.FPS;
import static constants.Constants.SELECTION;
import static controller.LifeBarController.resetLifeBar;
import static controller.PlayerController.handlePlayerAttack;
import static controller.PlayerController.characterStopsActing;
import static controller.PlayerController.isPlayer2Key;
import static controller.PlayerController.resetPosition;
import static controller.PlayerController.setupShadow;
import static controller.TimerController.pauseTimer;
import static controller.TimerController.resetTimer;
import static controller.TimerController.resumeTimer;
import static controller.TimerController.startTimer;

import view.CharacterSelectionScreen;
import view.MainMenu;
import controller.AIController;
import controller.LifeBarController;
import controller.PlayerController;
import controller.TimerController;
import java.util.HashSet;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.LifeBar;
import model.Player;
import model.Timer;
import sounds.BackgroundMusic;
import sounds.SoundEffect;

public class DragonBallFX extends Application {

	private Player player1;
	private Player player2;
	private Set<KeyCode> pressedKeys;
	private long lastUpdateTime = 0;
	private LifeBar player1LifeBar;
	private LifeBar player2LifeBar;
	private static boolean singlePlayerMode = false;
	private static boolean trainingMode = false;
	private Pane pauseMenu;
	private boolean isPaused = false;
	private int currentPauseMenuSelection = 0;
	private static final String ORANGE = "ff7a00";
	private AnimationTimer gameLoop;
	private Text winnerMessage;
	private ImageView winnerImageView;
	private ImageView loserImageView;
	private boolean MUSIC = false;
	private final Timer timer = new Timer();

	// Enseñamos el menú principal al iniciar la ejecución
	@Override
	public void start(Stage primaryStage) {
		MainMenu.show(primaryStage);
	}

	/* Tras elegir el modo de juego en el menú prinicpal setteamos algunas variables
	que definen el modo de juego elegido y abrimos la pantalla de selección de personaje */
	public static void startGame(Stage primaryStage, boolean isSinglePlayer, boolean isTrainingMode) {
		CharacterSelectionScreen characterSelectionScreen = new CharacterSelectionScreen();
		characterSelectionScreen.show(primaryStage);
		singlePlayerMode = isSinglePlayer;
		trainingMode = isTrainingMode;
	}

	/* Tras elegir personaje y escenario pausamos la música de la pantalla de inicio
	y llamamos al método de iniciar el juego pasándole los personajes y escenario elegidos */
	public static void startGameWithCharactersAndStage(Stage primaryStage, String player1Name, String player2Name, String stageName) {
		DragonBallFX game = new DragonBallFX();
		MainMenu.MUSIC = false;
		game.initGame(primaryStage, player1Name, player2Name, stageName);
	}

	// Iniciamos el juego poniendo la música de combate y creando los personajes, barras de vida, tiempo, controles y el GameLoop
	private void initGame(Stage primaryStage, String player1Name, String player2Name, String stageName) {
		Pane root = new Pane();
		BackgroundMusic.stop();
		if(!MUSIC){
			BackgroundMusic.play(BATTLE_SONG);
			MUSIC = true;
		}

		primaryStage.setResizable(false);
		createBackground(root, stageName);

		player1 = PlayerController.createPlayer(player1Name, PLAYER1X, PLAYERSY, KeyCode.W, KeyCode.S, KeyCode.A, KeyCode.D, KeyCode.E, KeyCode.Q, root);
		player2 = PlayerController.createPlayer(player2Name, PLAYER2X, PLAYERSY, KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.NUMPAD0, KeyCode.CONTROL, root);

		player1LifeBar = LifeBarController.createLifeBar(player1Name, 100, 20, true, root);
		player2LifeBar = LifeBarController.createLifeBar(player2Name, 800, 20, false, root);

		Text timerText = TimerController.createTimerText();
		root.getChildren().add(timerText);
		startTimer(timer, timerText);

		Scene scene = new Scene(root, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
		setupInputHandlers(scene, root);

		gameLoop = createGameLoop(root, timerText);

		gameLoop.start();

		setupStage(primaryStage, scene);
	}

	// Aquí controlamos cuando hay que abrir el menú y cuando el jugador golpea y deja de golpear
	private void setupInputHandlers(Scene scene, Pane root) {
		pressedKeys = new HashSet<>();

		scene.setOnKeyPressed(event -> {
			if (isPaused) {
				handlePauseMenuInput(event.getCode());
				return;
			}

			if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER) {
				openPauseMenu(root);
				return;
			}

			if (singlePlayerMode && isPlayer2Key(event.getCode())) {
				return;
			}

			pressedKeys.add(event.getCode());
			handlePlayerAttack(event.getCode(), player1, player2, player2LifeBar, true);
			handlePlayerAttack(event.getCode(), player2, player1, player1LifeBar, false);
		});

		scene.setOnKeyReleased(event -> {
			pressedKeys.remove(event.getCode());
			characterStopsActing(event.getCode(), player1);
			characterStopsActing(event.getCode(), player2);
		});
	}

	// Creamos el menú de pausa con todas sus opciones
	private void openPauseMenu(Pane root) {
		isPaused = true;
		pauseTimer(timer);

		pauseMenu = new Pane();
		pauseMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
		pauseMenu.setPrefSize(BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

		Rectangle pauseBackground = new Rectangle(BACKGROUND_WIDTH / 3.0, BACKGROUND_HEIGHT / 2.0);
		pauseBackground.setFill(Color.BLACK);
		pauseBackground.setOpacity(0.8);
		pauseBackground.setArcWidth(20);
		pauseBackground.setArcHeight(20);
		pauseBackground.setX((BACKGROUND_WIDTH - pauseBackground.getWidth()) / 2);
		pauseBackground.setY((BACKGROUND_HEIGHT - pauseBackground.getHeight()) / 2);

		Font pauseFont = TimerController.loadCustomFont(40);

		Text pauseTitle = new Text("-PAUSE-");
		pauseTitle.setFont(pauseFont);
		pauseTitle.setX((BACKGROUND_WIDTH - pauseTitle.getLayoutBounds().getWidth()) / 2);
		pauseTitle.setY(pauseBackground.getY() + 50);

		Text resumeText = createMenuOption("Resume", (int) (pauseBackground.getY() + 110), pauseFont);
		Text resetText = createMenuOption("Reset", (int) (pauseBackground.getY() + 160), pauseFont);
		Text characterSelectionText = createMenuOption("Change", (int) (pauseBackground.getY() + 210), pauseFont);
		Text exitText = createMenuOption("Exit", (int) (pauseBackground.getY() + 260), pauseFont);

		pauseMenu.getChildren().addAll(pauseBackground, pauseTitle, resumeText, resetText, characterSelectionText, exitText);
		root.getChildren().add(pauseMenu);

		currentPauseMenuSelection = 1;
		updatePauseMenuSelection();
	}

	// Le damos estilo a las opciones
	private Text createMenuOption(String text, int yPosition, Font font) {
		Text menuOption = new Text(text);
		menuOption.setFill(Color.WHITE);
		menuOption.setFont(font);
		menuOption.setX((double) (BACKGROUND_WIDTH - text.length() * 40) / 2 + 33);
		menuOption.setY(yPosition);
		menuOption.setEffect(new DropShadow(5, Color.BLACK));
		SoundEffect.play(ENTER);
		return menuOption;
	}

	// Cerrar el menú
	private void closePauseMenu() {
		isPaused = false;
		resumeTimer(timer);
		((Pane) pauseMenu.getParent()).getChildren().remove(pauseMenu);
	}

	// Reiniciar el juego y eliminar mensajes de victoria si hay
	public void resetGame() {
		closePauseMenu();
		resetPosition(player1, 150, 300);
		resetPosition(player2, 900, 300);
		resetLifeBar(player1LifeBar);
		resetLifeBar(player2LifeBar);
		player1.getPlayerView().setVisible(true);
		player2.getPlayerView().setVisible(true);
		resetTimer(timer);

		if (winnerMessage != null) {
			((Pane) winnerMessage.getParent()).getChildren().remove(winnerMessage);
			winnerMessage = null;
		}
		if (winnerImageView != null) {
			((Pane) winnerImageView.getParent()).getChildren().remove(winnerImageView);
			winnerImageView = null;

			((Pane) loserImageView.getParent()).getChildren().remove(loserImageView);
			loserImageView = null;
		}

		pressedKeys.clear();

		lastUpdateTime = 0;
		gameLoop.start();

		isPaused = false;
	}

	// Controlamos las entradas de teclado en el menú de pausa
	private void handlePauseMenuInput(KeyCode keyCode) {
		switch (keyCode) {
			case UP:
				if (currentPauseMenuSelection > 0) {
					currentPauseMenuSelection--;
					updatePauseMenuSelection();
					SoundEffect.play(SELECTION);
				}
				break;
			case DOWN:
				if (currentPauseMenuSelection < pauseMenu.getChildren().size() - 1) {
					SoundEffect.play(SELECTION);
					currentPauseMenuSelection++;
					updatePauseMenuSelection();
				}
				break;
			case ENTER:
				SoundEffect.play(ENTER);
				executePauseMenuSelection();
				break;
			default:
				break;
		}
	}

	// Actualizamos la opción seleccionada en el menú de pausa resaltándola de color naranja
	private void updatePauseMenuSelection() {
		for (int i = 0; i < pauseMenu.getChildren().size(); i++) {
			Node node = pauseMenu.getChildren().get(i);
			if (node instanceof Text) {
				((Text) node).setFill(i == currentPauseMenuSelection ? Color.web(ORANGE) : Color.WHITE);
			}
		}
	}

	// Controlamos los métodos a los que llamamos según la opción del menú de pausa que seleccionemos
	private void executePauseMenuSelection() {
		Node selectedOption = pauseMenu.getChildren().get(currentPauseMenuSelection);
		if (selectedOption instanceof Text) {
			String optionText = ((Text) selectedOption).getText();
			switch (optionText) {
				case "Resume":
					SoundEffect.play(ENTER);
					closePauseMenu();
					break;
				case "Reset":
					SoundEffect.play(ENTER);
					BackgroundMusic.stop();
					MUSIC = false;
					BackgroundMusic.play(BATTLE_SONG);
					resetGame();
					break;
				case "Change":
					SoundEffect.play(ENTER);
					CharacterSelectionScreen characterSelectionScreen = new CharacterSelectionScreen();
					MUSIC = true;
					characterSelectionScreen.show((Stage) pauseMenu.getScene().getWindow());
					break;
				case "Exit":
					SoundEffect.play(ENTER);
					BackgroundMusic.stop();
					MUSIC = false;
					MainMenu.show((Stage) pauseMenu.getScene().getWindow());
					break;
				default:
					SoundEffect.play(SECRET);
					break;
			}
		}
	}

	// Comprobamos si algún jugador tiene 0 de vida para declarar al otro como ganador
	private void checkForWinner(Pane root) {
		if (player1LifeBar.getCurrentLife() <= 0) {
			showWinner(player1, player2, "Player2 ", player2.getPlayerOriginalImage(), player1.getDefeatImage(), root);
		} else if (player2LifeBar.getCurrentLife() <= 0) {
			showWinner(player1, player2,"Player1 !", player1.getPlayerOriginalImage(), player2.getDefeatImage(), root);
		}
	}

	// Mostramos el ganador
	private void showWinner(Player player1, Player player2, String winnerName, Image winnerImage, Image loserImage, Pane root) {

		gameLoop.stop();

		winnerMessage = new Text(winnerName + " Wins!");
		winnerMessage.setFont(TimerController.loadCustomFont(80));
		winnerMessage.setFill(Paint.valueOf(ORANGE));
		winnerMessage.setX((BACKGROUND_WIDTH - winnerMessage.getLayoutBounds().getWidth()) / 2);
		winnerMessage.setY((double) BACKGROUND_HEIGHT / 2 - 50);
		DropShadow dropShadow = new DropShadow();
		dropShadow.setColor(Color.color(0, 0, 0, 1));
		dropShadow.setRadius(5);
		dropShadow.setOffsetX(2);
		dropShadow.setOffsetY(5);
		winnerMessage.setEffect(dropShadow);

		winnerImageView = new ImageView(winnerImage);
		winnerImageView.setFitWidth(150);
		winnerImageView.setFitHeight(150);
		winnerImageView.setX((BACKGROUND_WIDTH - winnerImageView.getFitWidth()) / 3);
		winnerImageView.setY(BACKGROUND_HEIGHT - 2 * winnerImageView.getFitHeight());
		setupShadow(winnerImageView, false);

		loserImageView = new ImageView(loserImage);
		loserImageView.setFitWidth(150);
		loserImageView.setFitHeight(150);
		loserImageView.setX((BACKGROUND_WIDTH - winnerImageView.getFitWidth())/1.5);
		loserImageView.setY(BACKGROUND_HEIGHT - 2 * winnerImageView.getFitHeight());
		setupShadow(loserImageView, false);

		player1.getPlayerView().setVisible(false);
		player2.getPlayerView().setVisible(false);

		root.getChildren().addAll(winnerMessage, winnerImageView, loserImageView);
	}

	// Creamos el bucle de juego
	private AnimationTimer createGameLoop(Pane root, Text timerText) {
		return new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (!isPaused) {
					long elapsedTime = now - lastUpdateTime;
					if (elapsedTime >= 1_000_000_000 / FPS) {
						PlayerController.update(pressedKeys, root, player1, player2, player1LifeBar, player2LifeBar);
						lastUpdateTime = now;

						if (singlePlayerMode) {
							AIController.controlPlayer(player2, player1, player1LifeBar, root);
						}

						if (trainingMode){
							player1LifeBar.setCurrentLife(MAX_LIFE);
							player2LifeBar.setCurrentLife(MAX_LIFE);
						}

						String timeString = TimerController.getTimeString(now, timer);
						timerText.setText("Time: " + timeString);

						checkForWinner(root);
					}
				}
			}
		};
	}

	public static void main(String[] args) {
		launch(args);
	}
}
