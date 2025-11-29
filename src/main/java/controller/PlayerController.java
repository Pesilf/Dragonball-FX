package controller;

import static constants.Constants.ATTACK_RANGE;
import static constants.Constants.BACKGROUND_HEIGHT;
import static constants.Constants.BACKGROUND_WIDTH;
import static constants.Constants.CHARACTER_PATH;
import static constants.Constants.CHARACTER_SQUARE_SIZE;
import static constants.Constants.COLLISION_HEIGHT;
import static constants.Constants.COLLISION_WIDTH;
import static constants.Constants.COOLDOWN;
import static constants.Constants.FAILED_PUNCH;
import static constants.Constants.KI_DAMAGE;
import static constants.Constants.KI_SPHERE;
import static constants.Constants.KI_SPHERE_HEIGHT;
import static constants.Constants.KI_SPHERE_SFX;
import static constants.Constants.KI_SPHERE_WIDTH;
import static constants.Constants.PLAYER2X;
import static controller.LifeBarController.updateLifeBar;

import database.CharacterStatsDAO;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.CharacterStats;
import model.LifeBar;
import model.Player;
import sounds.SoundEffect;

public class PlayerController {

	private static long lastPunchTime = 0;
	private static final Map<Rectangle, Integer> kiDirections = new HashMap<>();

	// Crear los personajes con sus imágenes y teclas
	public static Player createPlayer(String characterName, double x, double y, KeyCode upKey, KeyCode downKey, KeyCode leftKey, KeyCode rightKey, KeyCode punchKey, KeyCode kiKey, Pane root) {

		String basePath = CHARACTER_PATH + characterName + "/" + characterName;

		Image playerOriginalImage = new Image(basePath + "-Idle.png", CHARACTER_SQUARE_SIZE, CHARACTER_SQUARE_SIZE, false, false);
		Image playerRightImage = new Image(basePath + "-Right.png", CHARACTER_SQUARE_SIZE, CHARACTER_SQUARE_SIZE, false, false);
		Image playerLeftImage = new Image(basePath + "-Left.png", CHARACTER_SQUARE_SIZE, CHARACTER_SQUARE_SIZE, false, false);
		Image playerPunchImage1 = new Image(basePath + "-Punch-1.png", CHARACTER_SQUARE_SIZE, CHARACTER_SQUARE_SIZE, false, false);
		Image playerPunchImage2 = new Image(basePath + "-Punch-2.png", CHARACTER_SQUARE_SIZE, CHARACTER_SQUARE_SIZE, false, false);
		Image playerKiImage1 = new Image(basePath + "-Ki-Sphere-1.png", CHARACTER_SQUARE_SIZE, CHARACTER_SQUARE_SIZE, false, false);
		Image playerKiImage2 = new Image(basePath + "-Ki-Sphere-2.png", CHARACTER_SQUARE_SIZE, CHARACTER_SQUARE_SIZE, false, false);
		Image defeatImage = new Image(basePath + "-defeated.png", CHARACTER_SQUARE_SIZE, CHARACTER_SQUARE_SIZE, false, false);
		ImageView playerView = new ImageView(playerOriginalImage);
		setupShadow(playerView, false);
		playerView.setX(x);
		playerView.setY(y);
		root.getChildren().addAll(playerView);

		if (x == PLAYER2X) {
			playerView.setScaleX(-1);
			playerRightImage = new Image(basePath + "-Left.png", CHARACTER_SQUARE_SIZE, CHARACTER_SQUARE_SIZE, false, false);
			playerLeftImage = new Image(basePath + "-Right.png", CHARACTER_SQUARE_SIZE, CHARACTER_SQUARE_SIZE, false, false);
			setupShadow(playerView, true);
		}

		CharacterStats stats = CharacterStatsDAO.getCharacterStats(characterName);
		if (stats == null) {
			throw new IllegalArgumentException("No se encontraron estadísticas para el personaje: " + characterName);
		}

		return new Player(playerView, playerOriginalImage, playerRightImage, playerLeftImage, playerPunchImage1, playerPunchImage2, playerKiImage1, playerKiImage2, stats.getSpeed(), upKey, downKey, leftKey, rightKey, punchKey, kiKey, stats.getDamage(), stats.getDefense(), defeatImage);
	}

	// Controlar los ataques cuerpo a cuerpo de los jugadores
	public static void handlePlayerAttack(KeyCode keyCode, Player player, Player otherPlayer, LifeBar otherPlayerLifeBar, boolean isPlayer1) {
		long currentTime = System.nanoTime();

		if (keyCode == player.getPunchKey() && !player.isPunching()) {
			player.setPunching(true);

			if (currentTime - lastPunchTime >= COOLDOWN) {
				lastPunchTime = currentTime;
				SoundEffect.play(FAILED_PUNCH);

				if (player.isPunchToggle()) {
					player.getPlayerView().setImage(player.getPunchImage1());
				} else {
					player.getPlayerView().setImage(player.getPunchImage2());
				}
				player.setPunchToggle(!player.isPunchToggle());

				if (checkPlayersCollision(player, otherPlayer) || isInRange(player, otherPlayer)) {
					LifeBarController.removeLifeBlock(otherPlayerLifeBar, !isPlayer1, player.getDamage(), otherPlayer.getDefense());
				}
			}
		}

		if (keyCode == player.getKiKey() && !player.isShootingKi()) {
			player.setShootingKi(true);

			if (player.isPunchToggle()) {
				player.getPlayerView().setImage(player.getKiImage1());
			} else {
				player.getPlayerView().setImage(player.getKiImage2());
			}
			player.setPunchToggle(!player.isPunchToggle());
		}
	}

	// Volver al personaje al estado original después de golpear
	public static void characterStopsActing(KeyCode keyCode, Player player) {
		if (keyCode == player.getPunchKey() || keyCode == player.getKiKey()) {
			player.setPunching(false);
			player.setShootingKi(false);
			player.getPlayerView().setImage(player.getPlayerOriginalImage());
		}
	}

	// Mover al personaje, empujar y condiciones para disparar ki
	public static void move(Set<KeyCode> pressedKeys, Pane root, Player player1, Player player2, LifeBar player1LifeBar, LifeBar player2LifeBar) {

		for (KeyCode keyCode : pressedKeys) {
			if (keyCode == KeyCode.Q || keyCode == KeyCode.CONTROL) {
				shootKi(keyCode, root, player1, player2, player1LifeBar, player2LifeBar);
			}
		}

		for (Player player : new Player[]{player1, player2}) {
			if (!player.isPunching() && !player.isShootingKi()) {
				if (pressedKeys.contains(player.getLeftKey())) {
					player.getPlayerView().setImage(player.getPlayerLeftImage());
				} else if (pressedKeys.contains(player.getRightKey())) {
					player.getPlayerView().setImage(player.getPlayerRightImage());
				} else {
					player.getPlayerView().setImage(player.getPlayerOriginalImage());
				}
			}

			double dx = 0;
			double dy = 0;

			if (pressedKeys.contains(player.getUpKey())) dy -= player.getMovementSpeed();
			if (pressedKeys.contains(player.getDownKey())) dy += player.getMovementSpeed();
			if (pressedKeys.contains(player.getLeftKey())) dx -= player.getMovementSpeed();
			if (pressedKeys.contains(player.getRightKey())) dx += player.getMovementSpeed();

			double newX = player.getPlayerView().getX() + dx;
			double newY = player.getPlayerView().getY() + dy;

			double playerWidth = player.getPlayerView().getBoundsInParent().getWidth();
			double playerHeight = player.getPlayerView().getBoundsInParent().getHeight();
			double sceneWidth = root.getBoundsInLocal().getWidth();
			double sceneHeight = root.getBoundsInLocal().getHeight();

			Player otherPlayer = player == player1 ? player2 : player1;
			if (checkPlayersCollision(player, otherPlayer)) {
				double pushX = player.getPlayerView().getX() - otherPlayer.getPlayerView().getX();
				double pushY = player.getPlayerView().getY() - otherPlayer.getPlayerView().getY();
				double length = Math.sqrt(pushX * pushX + pushY * pushY);

				if (length != 0) {
					pushX /= length;
					pushY /= length;
				}

				double totalPushForce = player1.getMovementSpeed() * 0.5;
				newX += pushX * totalPushForce;
				newY += pushY * totalPushForce;

				double otherNewX = otherPlayer.getPlayerView().getX() - pushX * totalPushForce;
				double otherNewY = otherPlayer.getPlayerView().getY() - pushY * totalPushForce;

				otherNewX = Math.max(0, Math.min(otherNewX, sceneWidth - playerWidth));
				otherNewY = Math.max(0, Math.min(otherNewY, sceneHeight - playerHeight));

				otherPlayer.getPlayerView().setX(otherNewX);
				otherPlayer.getPlayerView().setY(otherNewY);
			}

			newX = Math.max(0, Math.min(newX, sceneWidth - playerWidth));
			newY = Math.max(0, Math.min(newY, sceneHeight - playerHeight));

			newX = Math.round(newX);
			newY = Math.round(newY);
			player.getPlayerView().setX(newX);
			player.getPlayerView().setY(newY);
		}
	}

	// Disparar ki
	public static void shootKi(KeyCode keyCode, Pane root, Player player1, Player player2, LifeBar player1LifeBar, LifeBar player2LifeBar) {
		Player activePlayer = null;
		if (keyCode == KeyCode.Q && !player2.isShootingKi()) {
			activePlayer = player1;
		} else if (keyCode == KeyCode.CONTROL && !player1.isShootingKi()) {
			activePlayer = player2;
		}

		if (activePlayer == null) {
			return;
		}

		long currentTime = System.nanoTime();
		long lastTimeKeyWasPressed = keyCode == KeyCode.Q ? player1.getLastTimeQPressed() : player2.getLastTimeCtrlPressed();

		if (currentTime - lastTimeKeyWasPressed < COOLDOWN) {
			return;
		}

		if (keyCode == KeyCode.Q) {
			player1.setLastTimeQPressed(currentTime);
		} else if (keyCode == KeyCode.CONTROL) {
			player2.setLastTimeCtrlPressed(currentTime);
		}

		Rectangle rectangle = createThrowRectangle(activePlayer);
		root.getChildren().add(rectangle);

		ImageView imageView = new ImageView(new Image(KI_SPHERE, KI_SPHERE_WIDTH, KI_SPHERE_HEIGHT, false, false));
		imageView.setFitWidth(rectangle.getWidth());
		imageView.setFitHeight(rectangle.getHeight());
		imageView.setX(rectangle.getX());
		imageView.setY(rectangle.getY());
		setupShadow(imageView, false);

		if (keyCode == KeyCode.CONTROL) {
			imageView.setScaleX(-1);
			setupShadow(imageView, true);
		}
		root.getChildren().add(imageView);

		AnimationTimer playerRectAnimation = new AnimationTimer() {
			final int direction = kiDirections.get(rectangle);

			@Override
			public void handle(long now) {
				double dx = direction == 1 ? player1.getMovementSpeed() : -player2.getMovementSpeed();
				double dy = 0;

				rectangle.setX(rectangle.getX() + dx);
				rectangle.setY(rectangle.getY() + dy);
				imageView.setX(rectangle.getX());
				imageView.setY(rectangle.getY());

				if (rectangle.getX() < 0 || rectangle.getX() > BACKGROUND_WIDTH ||
					rectangle.getY() < 0 || rectangle.getY() > BACKGROUND_HEIGHT ||
					(keyCode == KeyCode.Q && checkKiCollision(player2, rectangle)) ||
					(keyCode == KeyCode.CONTROL && checkKiCollision(player1, rectangle))) {
					root.getChildren().removeAll(rectangle, imageView);
					this.stop();
				}

				if (keyCode == KeyCode.Q && checkKiCollision(player2, rectangle)) {
					root.getChildren().removeAll(rectangle, imageView);
					LifeBarController.removeLifeBlock(player2LifeBar, true, KI_DAMAGE, player2.getDefense());
					this.stop();
				} else if (keyCode == KeyCode.CONTROL && checkKiCollision(player1, rectangle)) {
					root.getChildren().removeAll(rectangle, imageView);
					LifeBarController.removeLifeBlock(player1LifeBar, false, KI_DAMAGE, player2.getDefense());
					this.stop();
				}

				for (Node node : root.getChildren()) {
					if (node instanceof Rectangle otherRectangle && node != rectangle && (rectangle.getBoundsInParent().intersects(otherRectangle.getBoundsInParent()))) {
						root.getChildren().removeAll(rectangle, imageView, otherRectangle);
						this.stop();
						break;
					}
				}
			}
		};
		playerRectAnimation.start();
	}

	// Crea el rectángulo para las bolas de ki
	public static Rectangle createThrowRectangle(Player player) {

		double x = player.getPlayerView().getX() + (player.getPlayerView().getImage().getWidth() / 2);
		double y = player.getPlayerView().getY() + (player.getPlayerView().getImage().getHeight() / 2);
		Rectangle rectangle = new Rectangle(x, y, KI_SPHERE_WIDTH, KI_SPHERE_HEIGHT);
		rectangle.setFill(Color.TRANSPARENT);

		int direction = (player.getKiKey() == KeyCode.Q) ? 1 : -1;
		kiDirections.put(rectangle, direction);

		SoundEffect.play(KI_SPHERE_SFX);
		return rectangle;
	}

	// Actualiza la posición de los personajes y sus barras de vida.
	public static void update(Set<KeyCode> pressedKeys, Pane root, Player player1, Player player2, LifeBar player1LifeBar, LifeBar player2LifeBar) {
		move(pressedKeys, root, player1, player2, player1LifeBar, player2LifeBar);

		updateLifeBar(player1LifeBar, false);
		updateLifeBar(player2LifeBar, true);
	}

	// Devuelve las posiciones originales delos personajes
	public static void resetPosition(Player player, int initialX, int initialY) {
		player.getPlayerView().setX(initialX);
		player.getPlayerView().setY(initialY);
		player.getPlayerView().setImage(player.getPlayerOriginalImage());
	}

	// Comprueba si el otro jugador está en tu rango de ataque
	public static boolean isInRange(Player player, Player otherPlayer) {
		double dx = player.getPlayerView().getX() - otherPlayer.getPlayerView().getX();
		double dy = player.getPlayerView().getY() - otherPlayer.getPlayerView().getY();
		double distanceSquared = dx * dx + dy * dy;
		double attackRangeSquared = ATTACK_RANGE * ATTACK_RANGE;

		return distanceSquared <= attackRangeSquared;
	}

	// Comprueba si hay colisión entre los dos jugadores
	public static boolean checkPlayersCollision(Player player, Player otherPlayer) {
		double thisX = player.getPlayerView().getX();
		double thisY = player.getPlayerView().getY();
		double otherX = otherPlayer.getPlayerView().getX();
		double otherY = otherPlayer.getPlayerView().getY();

		double thisRight = thisX + COLLISION_WIDTH;
		double thisBottom = thisY + COLLISION_HEIGHT;

		double otherRight = otherX + COLLISION_WIDTH;
		double otherBottom = otherY + COLLISION_HEIGHT;

		boolean xOverlap = (thisRight >= otherX && thisX <= otherRight) ||
			(otherRight >= thisX && otherX <= thisRight);
		boolean yOverlap = (thisBottom >= otherY && thisY <= otherBottom) ||
			(otherBottom >= thisY && otherY <= thisBottom);

		return xOverlap && yOverlap;
	}

	// Comprueba si las bolas de ki tienen alguna colisión
	public static boolean checkKiCollision(Player player, Rectangle rectangle) {
		double thisX = player.getPlayerView().getX();
		double thisY = player.getPlayerView().getY();
		double thisRight = thisX + COLLISION_WIDTH;
		double thisBottom = thisY + COLLISION_HEIGHT;
		double rectX = rectangle.getX();
		double rectY = rectangle.getY();
		double rectRight = rectX + rectangle.getWidth();
		double rectBottom = rectY + rectangle.getHeight();

		return (rectRight >= thisX && rectX <= thisRight) &&
			(rectBottom >= thisY && rectY <= thisBottom);
	}

	// Comprueba si hay alguna tecla del jugador 2 pulsándose
	public static boolean isPlayer2Key(KeyCode keyCode) {
		return keyCode == KeyCode.UP || keyCode == KeyCode.DOWN || keyCode == KeyCode.LEFT || keyCode == KeyCode.RIGHT || keyCode == KeyCode.NUMPAD0 || keyCode == KeyCode.CONTROL;
	}

	// Agrega sombreas a los objetos
	public static void setupShadow(Node node, boolean invert) {
		DropShadow dropShadow = new DropShadow();
		dropShadow.setColor(Color.color(0, 0, 0, 1));
		dropShadow.setRadius(10);
		dropShadow.setOffsetY(5);
		if (invert) {
			dropShadow.setOffsetX(-5);
		} else {
			dropShadow.setOffsetX(5);
		}
		node.setEffect(dropShadow);
	}
}
