package model;

import static constants.Constants.LIFE_BAR_HEIGHT;
import static constants.Constants.BAR_WIDTH;
import static constants.Constants.MAX_LIFE;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Data;

@Data
public class LifeBar {

	private final Rectangle[] lifeBlocks;
	private int currentLife;

	public LifeBar(double x, double y) {
		lifeBlocks = new Rectangle[MAX_LIFE];
		currentLife = MAX_LIFE;

		for (int i = 0; i < MAX_LIFE; i++) {
			lifeBlocks[i] = new Rectangle(x + i * BAR_WIDTH, y, BAR_WIDTH * BAR_WIDTH / 2, LIFE_BAR_HEIGHT);
			lifeBlocks[i].setFill(Color.GREEN);
		}
	}
}
