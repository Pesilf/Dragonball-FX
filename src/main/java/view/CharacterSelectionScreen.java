package view;

import static constants.Constants.BACKGROUND_HEIGHT;
import static constants.Constants.BACKGROUND_WIDTH;
import static constants.Constants.BLUE;
import static constants.Constants.CHARACTER_ICON_PATH;
import static constants.Constants.CHARACTER_IMAGE_HEIGHT;
import static constants.Constants.CHARACTER_IMAGE_WIDTH;
import static constants.Constants.ENTER;
import static constants.Constants.ORANGE;
import static constants.Constants.CHARACTER_COLUMNS;
import static constants.Constants.RANDOM_CHARACTER_IMAGE_PATH;
import static constants.Constants.SELECTION;
import static constants.Constants.STROKE_WIDTH;
import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.TRANSPARENT;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import sounds.BackgroundMusic;
import sounds.SoundEffect;

public class CharacterSelectionScreen {

	private List<String> characterNames;
	private int currentSelection = 0;
	private int player1Selection = -1;
	private int player2Selection = -1;
	private boolean isPlayer1Turn = true;
	private Stage stage;
	private Rectangle player1SelectionRect;
	private Rectangle player2SelectionRect;


	// Mostrar Iconos de los personajes centrados en pantalla
	public void show(Stage primaryStage) {
		stage = primaryStage;
		Pane root = new Pane();
		GridPane gridPane = new GridPane();

		characterNames = loadCharacterNames();
		for (int i = 0; i < characterNames.size(); i++) {
			Image characterImage = new Image(CHARACTER_ICON_PATH + "/" + characterNames.get(i), CHARACTER_IMAGE_WIDTH, CHARACTER_IMAGE_HEIGHT, false, false);
			ImageView imageView = new ImageView(characterImage);
			StackPane stackPane = new StackPane();
			stackPane.getChildren().add(imageView);
			gridPane.add(stackPane, i % CHARACTER_COLUMNS, i / CHARACTER_COLUMNS);
		}

		Image randomCharacterImage = new Image(RANDOM_CHARACTER_IMAGE_PATH, CHARACTER_IMAGE_WIDTH,
											   CHARACTER_IMAGE_HEIGHT, false, false);
		ImageView randomImageView = new ImageView(randomCharacterImage);
		StackPane randomSelectionPane = new StackPane();
		randomSelectionPane.getChildren().add(randomImageView);
		gridPane.add(randomSelectionPane, characterNames.size() % CHARACTER_COLUMNS, characterNames.size() / CHARACTER_COLUMNS);

		gridPane.setLayoutX(Math.round((float) (BACKGROUND_WIDTH - (CHARACTER_IMAGE_WIDTH + STROKE_WIDTH) * CHARACTER_COLUMNS) / 2));
		gridPane.setLayoutY(Math.round((double) CHARACTER_IMAGE_HEIGHT / 3));

		root.getChildren().add(gridPane);

		Scene scene = new Scene(root, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BLACK);
		primaryStage.setScene(scene);

		scene.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ESCAPE) {
				BackgroundMusic.stop();
				SoundEffect.play(ENTER);
				MainMenu.show(primaryStage);
			} else {
				handleInput(event.getCode());
				updateSelection(gridPane);
			}
		});

		primaryStage.show();
		updateSelection(gridPane);
	}

	// Substraemos el nombre del personaje desde la imagen
	private List<String> loadCharacterNames() {
		List<String> names = new ArrayList<>();
		File folder = new File(CHARACTER_ICON_PATH.substring(5)); // Remove "file:" from path
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".png")) {
					names.add(file.getName());
				}
			}
		}
		return names;
	}

	// Controlamos las entradas de teclado
	private void handleInput(KeyCode keyCode) {
		switch (keyCode) {
			case UP:
				SoundEffect.play(SELECTION);
				if (currentSelection >= CHARACTER_COLUMNS) currentSelection -= CHARACTER_COLUMNS;
				break;
			case DOWN:
				SoundEffect.play(SELECTION);
				if (currentSelection + CHARACTER_COLUMNS < characterNames.size() + 1) currentSelection += CHARACTER_COLUMNS;
				break;
			case LEFT:
				SoundEffect.play(SELECTION);
				if (currentSelection % CHARACTER_COLUMNS > 0) currentSelection--;
				break;
			case RIGHT:
				SoundEffect.play(SELECTION);
				if (currentSelection % CHARACTER_COLUMNS < CHARACTER_COLUMNS - 1 && currentSelection < characterNames.size()) currentSelection++;
				break;
			case ENTER:
				SoundEffect.play(ENTER);
				selectCharacter();
				break;
			default:
				break;
		}
	}

	// Actualizamos en pantalla el personaje que estamos seleccionando con un recuadro naranja
	private void updateSelection(GridPane gridPane) {
		if (player1SelectionRect == null) {
			player1SelectionRect = new Rectangle((double) CHARACTER_IMAGE_WIDTH - STROKE_WIDTH,
												 (double) CHARACTER_IMAGE_HEIGHT - STROKE_WIDTH,
												 TRANSPARENT);

			player1SelectionRect.setStroke(javafx.scene.paint.Color.web(ORANGE));
			player1SelectionRect.setStrokeWidth(STROKE_WIDTH);
		}
		if (player2SelectionRect == null) {
			player2SelectionRect = new Rectangle((double) CHARACTER_IMAGE_WIDTH - STROKE_WIDTH,
												 (double) CHARACTER_IMAGE_HEIGHT - STROKE_WIDTH,
												 TRANSPARENT);

			player2SelectionRect.setStroke(javafx.scene.paint.Color.web(BLUE));
			player2SelectionRect.setStrokeWidth(STROKE_WIDTH);
		}

		for (int i = 0; i < gridPane.getChildren().size(); i++) {
			Node node = gridPane.getChildren().get(i);
			if (node instanceof StackPane stackPane) {
				stackPane.getChildren().removeIf(Rectangle.class::isInstance);

				if (i == currentSelection && isPlayer1Turn) {
					stackPane.getChildren().add(player1SelectionRect);
				} else if (i == currentSelection && player2Selection == -1) {
					stackPane.getChildren().add(player2SelectionRect);
				} else if (i == player1Selection) {
					stackPane.getChildren().add(player1SelectionRect);
				} else if (i == player2Selection) {
					stackPane.getChildren().add(player2SelectionRect);
				}
			}
		}
	}

	// Guardamos el personaje seleccionado
	private void selectCharacter() {
		if (currentSelection == characterNames.size()) {
			selectRandomCharacter();
		} else {
			if (isPlayer1Turn) {
				player1Selection = currentSelection;
				isPlayer1Turn = false;
			} else {
				player2Selection = currentSelection;
				showStageSelectionScreen();
			}
		}
	}

	// Guardamos un personaje aleatorio sacándolo desde la array de nombres
	private void selectRandomCharacter() {
		Random random = new Random();
		int randomIndex = random.nextInt(characterNames.size());
		if (isPlayer1Turn) {
			player1Selection = randomIndex;
			isPlayer1Turn = false;
		} else {
			player2Selection = randomIndex;
			showStageSelectionScreen();
		}
	}

	// Pasamos la información a la pantalla de selección de escenario y la abrimos
	private void showStageSelectionScreen() {
		StageSelectionScreen stageSelectionScreen = new StageSelectionScreen();
		stageSelectionScreen.show(stage, characterNames.get(player1Selection), characterNames.get(player2Selection));
	}
}
