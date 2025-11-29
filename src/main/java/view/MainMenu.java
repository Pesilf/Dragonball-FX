package view;

import static constants.Constants.BACKGROUND_HEIGHT;
import static constants.Constants.BACKGROUND_WIDTH;
import static constants.Constants.ENTER;
import static constants.Constants.GAME_TITLE;
import static constants.Constants.MAIN_SONG;
import static constants.Constants.OPTIONS;
import static constants.Constants.ORANGE;
import static constants.Constants.PROGRAM_ICON_PATH;
import static constants.Constants.PROGRAM_NAME;
import static constants.Constants.SELECTION;
import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.WHITE;

import controller.TimerController;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.DragonBallFX;
import sounds.BackgroundMusic;
import sounds.SoundEffect;

public class MainMenu {

	private static int currentSelection = 0;
	public static boolean MUSIC = false;

	// Montamos la escena sobre el escenario y ponemos un nombre y un icono a la ventana
	public static void setupStage(Stage primaryStage, Scene scene) {
		primaryStage.setTitle("Dragonball FX");
		Image icon = new Image(PROGRAM_ICON_PATH, 0, 0, false, false);
		primaryStage.getIcons().add(icon);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	// mostramos el menú principal con sus tres opciones y la imagen de fondo
	public static void show(Stage primaryStage) {
		Pane root = new Pane();
		Image backgroundImage = new Image(GAME_TITLE, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, false, false);
		ImageView backgroundImageView = new ImageView(backgroundImage);
		root.getChildren().add(backgroundImageView);


		Scene scene = new Scene(root, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BLACK);
		primaryStage.setTitle(PROGRAM_NAME);
		Image icon = new Image(PROGRAM_ICON_PATH);
		primaryStage.getIcons().add(icon);
		primaryStage.setResizable(false);

		Text[] menuOptions = new Text[OPTIONS.length];
		Font menuFont = TimerController.loadCustomFont(50);

		for (int i = 0; i < OPTIONS.length; i++) {
			menuOptions[i] = new Text(OPTIONS[i]);
			menuOptions[i].setFill(i == currentSelection ? Color.web(ORANGE) : WHITE);
			menuOptions[i].setFont(menuFont);
			menuOptions[i].setX(640);
			menuOptions[i].setY((double) 390 + i * 60);
			root.getChildren().add(menuOptions[i]);
		}

		scene.setOnKeyPressed(event -> {
			switch (event.getCode()) {
				case UP:
					if (currentSelection > 0) {
						SoundEffect.play(SELECTION);
						menuOptions[currentSelection].setFill(WHITE);
						currentSelection--;
						menuOptions[currentSelection].setFill(Color.web(ORANGE));
					}
					break;
				case DOWN:
					if (currentSelection < OPTIONS.length - 1) {
						SoundEffect.play(SELECTION);
						menuOptions[currentSelection].setFill(WHITE);
						currentSelection++;
						menuOptions[currentSelection].setFill(Color.web(ORANGE));
					}
					break;
				case ENTER:
					SoundEffect.play(ENTER);
					handleSelection(primaryStage);
					break;
				default:
					break;
			}
		});

		if(!MUSIC){
			BackgroundMusic.play(MAIN_SONG);
			MUSIC = true;
		}

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	// Controlamos las entradas de teclado para seleccionar el modo de juego
	private static void handleSelection(Stage primaryStage) {
		switch (currentSelection) {
			case 0:
				DragonBallFX.startGame(primaryStage, true, false);
				break;
			case 1:
				DragonBallFX.startGame(primaryStage, false, false);
				break;
			case 2:
				DragonBallFX.startGame(primaryStage, false, true);
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + currentSelection);
		}
	}
}
