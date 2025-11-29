package controller;

import static constants.Constants.ATTACK_RANGE;
import static constants.Constants.BACKGROUND_HEIGHT;
import static constants.Constants.BACKGROUND_WIDTH;
import static constants.Constants.HEIGHT_THRESHOLD;
import static constants.Constants.KI_SPHERE;
import static constants.Constants.KI_SPHERE_HEIGHT;
import static constants.Constants.KI_SPHERE_WIDTH;
import static constants.Constants.COOLDOWN;
import static controller.LifeBarController.removeLifeBlock;
import static controller.PlayerController.checkKiCollision;
import static controller.PlayerController.setupShadow;
import static controller.PlayerController.createThrowRectangle;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import model.LifeBar;
import model.Player;

public class AIController {

	private static long lastAttackTime = 0;

	// Comportamiento de la IA
	public static void controlPlayer(Player player2, Player player1, LifeBar player1LifeBar, Pane root) {
		double distanceX = player2.getPlayerView().getX() - player1.getPlayerView().getX();
		double distanceY = player2.getPlayerView().getY() - player1.getPlayerView().getY();
		double absDistanceX = Math.abs(distanceX);
		long currentTime = System.nanoTime();

		boolean actionTaken = false;

		// Retroceder al recibir ataques del jugador 1 y dispararle ki
		if (player1.isPunching() || player1.isShootingKi()) {
			if (absDistanceX < BACKGROUND_HEIGHT) {
				if (distanceX > 0) {
					if (player2.getPlayerView().getX() + player2.getPlayerView().getBoundsInLocal().getWidth() < BACKGROUND_WIDTH) {
						moveRight(player2);
					}
				} else {
					moveLeft(player2);
				}
				actionTaken = true;
			} else {
				if (currentTime - lastAttackTime >= COOLDOWN) {
					performKiAttack(player2, player1, player1LifeBar, root);
					actionTaken = true;
				}
			}
		}

		if (player1.isPunching() || player1.isShootingKi()) {
			if (player2.getPlayerView().getX() > BACKGROUND_WIDTH - player2.getPlayerView().getBoundsInLocal().getWidth()) {
			} else {
				if (currentTime - lastAttackTime >= COOLDOWN) {
					performKiAttack(player2, player1, player1LifeBar, root);
					actionTaken = true;
				}
			}
		}

		// Igualar alturas con el jugador 1
		boolean alturasCasiIguales;
		if (Math.abs(distanceY) < HEIGHT_THRESHOLD) {
			alturasCasiIguales = true;
			player2.getPlayerView().setY(player1.getPlayerView().getY());
		} else {
			alturasCasiIguales = false;
		}

		if (!actionTaken && !alturasCasiIguales) {
			if (distanceY > 0) {
				moveUp(player2);
			} else {
				moveDown(player2);
			}
			actionTaken = true;
		}

		// Acercarse al jugador 1 y atacarle cuerpo a cuerpo
		if (!actionTaken && !(player1.isPunching() || player1.isShootingKi())) {
			if (absDistanceX > ATTACK_RANGE) {
				if (distanceX > 0) {
					moveLeft(player2);
				} else {
					if (player2.getPlayerView().getX() + player2.getPlayerView().getBoundsInLocal().getWidth() < BACKGROUND_WIDTH) {
						moveRight(player2);
					}
				}
			} else {
				handleAIAttack(player2, player1, player1LifeBar);
			}
		}
	}

	public static void handleAIAttack(Player player, Player targetPlayer, LifeBar targetLifeBar) {
		long currentTime = System.nanoTime();
		if (currentTime - lastAttackTime >= COOLDOWN) {

			if (!player.isPunching()) {
				if (!player.isPunchToggle()) {
					player.getPlayerView().setImage(player.getPunchImage1());
					player.setPunchToggle(true);
				} else {
					player.getPlayerView().setImage(player.getPunchImage2());
					player.setPunchToggle(false);
				}
			}
			player.setPunching(false);

			removeLifeBlock(targetLifeBar, false, player.getDamage(), targetPlayer.getDefense());

			lastAttackTime = currentTime;
		}
	}

	private static void performKiAttack(Player player2, Player player1, LifeBar player1LifeBar, Pane root) {
		long currentTime = System.nanoTime();
		if (currentTime - lastAttackTime >= COOLDOWN) {
			kiAttack(player1, player2, player1LifeBar, root);
			lastAttackTime = currentTime;
		}
	}

	public static void kiAttack(Player otherPlayer, Player player, LifeBar otherPlayerLifeBar, Pane root) {
		if (!player.isPunching()) {
			player.setPunching(true);
			if (player.isPunchToggle()) {
				player.getPlayerView().setImage(player.getKiImage1());
				player.setPunchToggle(false);
			} else {
				player.getPlayerView().setImage(player.getKiImage2());
				player.setPunchToggle(true);
			}

			Rectangle rectangle = createThrowRectangle(player);
			root.getChildren().add(rectangle);

			ImageView imageView = new ImageView(new Image(KI_SPHERE, KI_SPHERE_WIDTH, KI_SPHERE_HEIGHT, false, false));
			imageView.setFitWidth(rectangle.getWidth());
			imageView.setFitHeight(rectangle.getHeight());
			imageView.setX(rectangle.getX());
			imageView.setY(rectangle.getY());
			setupShadow(imageView, false);

			if (player.getPlayerView().getScaleX() == -1) {
				imageView.setScaleX(-1);
				setupShadow(imageView, true);
			}

			root.getChildren().add(imageView);

			AnimationTimer kiAnimation = new AnimationTimer() {
				final double dx = (player.getPlayerView().getScaleX() == -1) ? -15 : 15;
				final double dy = 0;

				@Override
				public void handle(long now) {
					rectangle.setX(rectangle.getX() + dx);
					rectangle.setY(rectangle.getY() + dy);
					imageView.setX(rectangle.getX());
					imageView.setY(rectangle.getY());

					boolean outOfBounds = rectangle.getX() < 0 || rectangle.getX() > root.getWidth() ||
						rectangle.getY() < 0 || rectangle.getY() > root.getHeight();
					boolean hitPlayer = checkKiCollision(otherPlayer, rectangle);

					for (Node node : root.getChildren()) {
						if (node instanceof Rectangle otherRectangle && node != rectangle &&
							checkRectangleCollisionIA(rectangle, otherRectangle)) {

							if (Math.random() < 0.5) {
								root.getChildren().removeAll(rectangle, imageView);
							} else {
								root.getChildren().removeAll(otherRectangle, imageView);
							}
							this.stop();
							return;
						}
					}

					if (outOfBounds || hitPlayer) {
						root.getChildren().removeAll(rectangle, imageView);
						if (hitPlayer) {
							removeLifeBlock(otherPlayerLifeBar, true, player.getDamage(), otherPlayer.getDefense());
						}
						this.stop();
					}
				}
			};
			kiAnimation.start();
		}
		player.setPunching(false);
	}

	public static boolean checkRectangleCollisionIA(Rectangle rect1, Rectangle rect2) {
		return rect1.getBoundsInParent().intersects(rect2.getBoundsInParent());
	}

	public static void moveLeft(Player player) {
		player.getPlayerView().setX(Math.round(player.getPlayerView().getX() - player.getMovementSpeed()));
		player.getPlayerView().setImage(player.getPlayerLeftImage());
	}

	public static void moveRight(Player player) {
		player.getPlayerView().setX(Math.round(player.getPlayerView().getX() + player.getMovementSpeed()));
		player.getPlayerView().setImage(player.getPlayerRightImage());
	}

	public static void moveUp(Player player) {
		player.getPlayerView().setY(Math.round(player.getPlayerView().getY() - player.getMovementSpeed()));
	}

	public static void moveDown(Player player) {
		player.getPlayerView().setY(Math.round(player.getPlayerView().getY() + player.getMovementSpeed()));
	}
}
