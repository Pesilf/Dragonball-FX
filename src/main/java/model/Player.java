package model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import lombok.Data;

@Data
public class Player {

	private boolean isShootingKi;
	private long lastRectangleThrowTime;
	private boolean isPunching = false;
	private boolean punchToggle = false;
	private final ImageView playerView;
	private final Image playerOriginalImage;
	private final Image playerRightImage;
	private final Image playerLeftImage;
	private final Image punchImage1;
	private final Image punchImage2;
	private final Image kiImage1;
	private final Image kiImage2;
	private final double movementSpeed;
	private final KeyCode upKey;
	private final KeyCode downKey;
	private final KeyCode leftKey;
	private final KeyCode rightKey;
	private final KeyCode punchKey;
	private final KeyCode kiKey;
	private final int damage;
	private final int defense;
	private final Image defeatImage;
	private long lastTimeQPressed;
	private long lastTimeCtrlPressed;
	private long lastTimeKiPressed;
}
