package model;

import javafx.animation.AnimationTimer;
import lombok.Data;

@Data
public class Timer {

	private long startTime;
	private long pausedTime = 0;
	private boolean isRunning = false;
	private AnimationTimer animationTimer;
}

