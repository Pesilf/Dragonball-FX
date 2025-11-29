package controller;

import javafx.animation.AnimationTimer;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import model.Timer;

public class TimerController {

	// Creamos el texto del timer y le damos estilo
	public static Text createTimerText() {
		Text timerText = new Text();
		timerText.setFont(loadCustomFont(25));
		timerText.setX(515);
		timerText.setY(45);
		timerText.setFill(Color.WHITE);
		DropShadow dropShadow = new DropShadow();
		dropShadow.setColor(Color.color(0, 0, 0, 1));
		dropShadow.setRadius(2);
		dropShadow.setOffsetX(2);
		dropShadow.setOffsetY(2);
		timerText.setEffect(dropShadow);
		return timerText;
	}

	// Empezar el contador del timer
	public static void startTimer(Timer timer, Text timerText) {
		if (!timer.isRunning()) {
			timer.setStartTime(System.nanoTime());
			timer.setAnimationTimer(new AnimationTimer() {
				@Override
				public void handle(long now) {
					String timeString = getTimeString(now, timer);
					timerText.setText("Time: " + timeString);
				}
			});
			timer.getAnimationTimer().start();
			timer.setRunning(true);
		}
	}

	// Pausar el contador del timer
	public static void pauseTimer(Timer timer) {
		if (timer.isRunning()) {
			timer.setPausedTime(System.nanoTime() - timer.getStartTime());
			timer.getAnimationTimer().stop();
			timer.setRunning(false);
		}
	}

	// Continuar el contador del timer después de haberlos pausado
	public static void resumeTimer(Timer timer) {
		if (!timer.isRunning()) {
			timer.setStartTime(System.nanoTime() - timer.getPausedTime());
			timer.getAnimationTimer().start();
			timer.setRunning(true);
		}
	}

	// Resetear timer
	public static void resetTimer(Timer timer) {
		timer.setPausedTime(0);
		timer.setStartTime(System.nanoTime());
	}

	// Obtener string con los minutos y segundos exactos
	public static String getTimeString(long now, Timer timer) {
		long elapsedTime = now - timer.getStartTime();
		long seconds = (elapsedTime / 1_000_000_000L) % 60;
		long minutes = (elapsedTime / (1_000_000_000L * 60)) % 60;
		return String.format("%02d:%02d", minutes, seconds);
	}

	// Cargar la fuente custom pixel art
	public static Font loadCustomFont(double size) {
		return Font.loadFont("file:../Dragonball-FX/src/main/java/fonts/Retro Gaming.ttf", size);
	}
}
