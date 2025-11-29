package view;

import static constants.Constants.BACKGROUND_HEIGHT;
import static constants.Constants.BACKGROUND_WIDTH;
import static constants.Constants.ENTER;
import static constants.Constants.ORANGE;
import static constants.Constants.RANDOM_STAGE_IMAGE_PATH;
import static constants.Constants.SELECTION;
import static constants.Constants.STAGES_PATH;
import static constants.Constants.STAGE_COLUMNS;
import static constants.Constants.STAGE_IMAGE_HEIGHT;
import static constants.Constants.STAGE_IMAGE_WIDTH;
import static constants.Constants.STROKE_WIDTH;
import static javafx.scene.paint.Color.BLACK;

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
import main.DragonBallFX;
import sounds.SoundEffect;

public class StageSelectionScreen {

	private String stageName;
	private List<String> stageNames;
	private int currentSelection = 0;
	private Rectangle selectionRect;
	private Stage stage;
	private String player1Name;
	private String player2Name;

	// Ponemos la imagen del escenario de fondo en la ventana
	public static void createBackground(Pane root, String stageName) {
		Image backgroundImage = new Image("file:../Dragonball-FX/src/main/java/images/backgrounds/" + stageName + ".png", BACKGROUND_WIDTH, BACKGROUND_HEIGHT, false, false);
		ImageView backgroundImageView = new ImageView(backgroundImage);
		backgroundImageView.setFitWidth(BACKGROUND_WIDTH);
		backgroundImageView.setFitHeight(BACKGROUND_HEIGHT);
		backgroundImageView.setPreserveRatio(false);
		root.getChildren().add(backgroundImageView);
	}

	// Mostramos todos los escenarios ordenados y centrados en pantalla
	public void show(Stage primaryStage, String player1Name, String player2Name) {
		stage = primaryStage;
		this.player1Name = player1Name;
		this.player2Name = player2Name;
		Pane root = new Pane();
		GridPane gridPane = new GridPane();

		stageNames = loadStageNames();
		for (int i = 0; i < stageNames.size(); i++) {
			Image stageImage = new Image(STAGES_PATH + "/" + stageNames.get(i), STAGE_IMAGE_WIDTH,
										 STAGE_IMAGE_HEIGHT, false, false);
			ImageView imageView = new ImageView(stageImage);
			StackPane stackPane = new StackPane();
			stackPane.getChildren().add(imageView);
			gridPane.add(stackPane, i % STAGE_COLUMNS, i / STAGE_COLUMNS);
		}

		Image randomStageImage = new Image(RANDOM_STAGE_IMAGE_PATH, STAGE_IMAGE_WIDTH, STAGE_IMAGE_HEIGHT, false, false);
		ImageView randomImageView = new ImageView(randomStageImage);
		StackPane randomSelectionPane = new StackPane();
		randomSelectionPane.getChildren().add(randomImageView);
		gridPane.add(randomSelectionPane, stageNames.size() % STAGE_COLUMNS, stageNames.size() / STAGE_COLUMNS);

		gridPane.setLayoutX(Math.round((double) (STAGE_IMAGE_WIDTH - STROKE_WIDTH) / 2));
		gridPane.setLayoutY(STAGE_IMAGE_HEIGHT);

		root.getChildren().add(gridPane);

		Scene scene = new Scene(root, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BLACK);
		primaryStage.setScene(scene);

		scene.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ESCAPE) {
				SoundEffect.play(ENTER);
				CharacterSelectionScreen characterSelectionScreen = new CharacterSelectionScreen();
				characterSelectionScreen.show(stage);
			} else {
				handleInput(event.getCode());
				updateSelection(gridPane);
			}
		});

		primaryStage.show();
		updateSelection(gridPane);
	}

	// Guardamos el nombre del escenario
	private List<String> loadStageNames() {
		List<String> names = new ArrayList<>();
		File folder = new File(STAGES_PATH.substring(5));
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
				if (currentSelection >= STAGE_COLUMNS) currentSelection -= STAGE_COLUMNS;
				break;
			case DOWN:
				SoundEffect.play(SELECTION);
				if (currentSelection + STAGE_COLUMNS < stageNames.size() + 1) currentSelection += STAGE_COLUMNS;
				break;
			case LEFT:
				SoundEffect.play(SELECTION);
				if (currentSelection % STAGE_COLUMNS > 0) currentSelection--;
				break;
			case RIGHT:
				SoundEffect.play(SELECTION);
				if (currentSelection % STAGE_COLUMNS < STAGE_COLUMNS - 1 && currentSelection < stageNames.size()) currentSelection++;
				break;
			case ENTER:
				SoundEffect.play(ENTER);
				selectStage();
				break;
			default:
				break;
		}
	}

	// Mostramos la selección actual resaltándola con un borde naranja sobre la imagen
	private void updateSelection(GridPane gridPane) {
		if (selectionRect == null) {
			selectionRect = new Rectangle((double) STAGE_IMAGE_WIDTH - STROKE_WIDTH, (double) STAGE_IMAGE_HEIGHT - STROKE_WIDTH, javafx.scene.paint.Color.TRANSPARENT);
			selectionRect.setStroke(javafx.scene.paint.Color.web(ORANGE));
			selectionRect.setStrokeWidth(STROKE_WIDTH);
		}

		for (int i = 0; i < gridPane.getChildren().size(); i++) {
			Node node = gridPane.getChildren().get(i);
			if (node instanceof StackPane stackPane) {
				stackPane.getChildren().removeIf(Rectangle.class::isInstance);

				if (i == currentSelection) {
					stackPane.getChildren().add(selectionRect);
				}
			}
		}
	}

	// Elegimos un escenario al azar
	private void selectRandomStage() {
		Random random = new Random();
		currentSelection = random.nextInt(stageNames.size());
		stageName = stageNames.get(currentSelection).replace(".png", "");
	}

	// Pasamos la información de los personajes y el escenario e iniciamos la pelea
	private void selectStage() {
		if (currentSelection == stageNames.size()) {
			selectRandomStage();
		} else {
			stageName = stageNames.get(currentSelection).replace(".png", "");
		}

		DragonBallFX.startGameWithCharactersAndStage(stage, player1Name.replace(".png", ""), player2Name.replace(".png", ""), stageName);
	}
}
