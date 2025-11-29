package controller;

import static constants.Constants.LIFE_BAR_HEIGHT;
import static constants.Constants.BAR_WIDTH;
import static constants.Constants.CHARACTER_PATH;
import static constants.Constants.MAX_LIFE;
import static constants.Constants.PUNCH;
import static controller.PlayerController.setupShadow;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import sounds.SoundEffect;
import model.LifeBar;

public class LifeBarController {

	// Crear barras de vida
	public static LifeBar createLifeBar(String playerName, int x, int y, boolean invert, Pane root){

		Rectangle playerLifeBarBackground = new Rectangle(x - 2, y - 2, (MAX_LIFE * BAR_WIDTH) + 4, LIFE_BAR_HEIGHT + 4);
		playerLifeBarBackground.setFill(Color.BLACK);
		setupShadow(playerLifeBarBackground, invert);
		root.getChildren().add(playerLifeBarBackground);

		Image playerImage = new Image(CHARACTER_PATH + playerName + "/" + playerName + "-Icon-S.png", 80, 60, false, false);

		ImageView playerImageView = new ImageView(playerImage);
		setupShadow(playerImageView, invert);

		if(!invert){
			playerImageView.setX(x + BAR_WIDTH * MAX_LIFE + 2);
			playerImageView.setY(y - 2);
		}
		else{
			playerImageView.setX(x - playerImage.getWidth() - 2);
			playerImageView.setY(y - 2);
		}

		root.getChildren().add(playerImageView);
		LifeBar lifeBar = new LifeBar(x, y);
		root.getChildren().addAll(lifeBar.getLifeBlocks());
		return lifeBar;
	}

	// Quitarle vida a los jugadores
	public static void removeLifeBlock(LifeBar lifeBar, boolean player2, int damage, int defense) {
		SoundEffect.play(PUNCH);
		int netDamage = Math.max(damage - defense, 0);
		int currentLife = lifeBar.getCurrentLife();
		Rectangle[] lifeBlocks = lifeBar.getLifeBlocks();
		int maxLife = MAX_LIFE;

		for (int i = 0; i < netDamage; i++) {
			if (currentLife <= 0) {
				break;
			}

			if (player2 && maxLife - currentLife < maxLife) {
				lifeBlocks[maxLife - currentLife].setFill(Color.RED);
			} else if (currentLife > 0) {
				lifeBlocks[currentLife - 1].setFill(Color.RED);
			}

			currentLife--;
		}

		lifeBar.setCurrentLife(currentLife);
	}

	// Actualiza las barras de vida con la vida actual de cada jugador
	public static void updateLifeBar(LifeBar lifeBar, boolean player2) {

		int currentLife = lifeBar.getCurrentLife();
		Rectangle[] lifeBlocks = lifeBar.getLifeBlocks();
		int maxLife = MAX_LIFE;

		for (int i = 0; i < maxLife; i++) {
			if (i < currentLife) {
				if (player2) {
					lifeBlocks[maxLife - 1 - i].setFill(Color.GREEN);
				} else {
					lifeBlocks[i].setFill(Color.GREEN);
				}
			} else {
				if (player2) {
					lifeBlocks[maxLife - 1 - i].setFill(Color.RED);
				} else {
					lifeBlocks[i].setFill(Color.RED);
				}
			}
		}
	}

	// Resetea la vida de vida al máximo
	public static void resetLifeBar(LifeBar lifeBar) {
		lifeBar.setCurrentLife(MAX_LIFE);
		for (int i = 0; i < MAX_LIFE; i++) {
			lifeBar.getLifeBlocks()[i].setFill(Color.GREEN);
		}
	}
}
