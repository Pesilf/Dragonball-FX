package sounds;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.nio.file.Paths;

public class BackgroundMusic {
	private static MediaPlayer backgroundMusicPlayer;

	// Reproducir Música
	public static void play(String filePath) {
		try {
			String uriString = Paths.get(filePath).toUri().toString();
			Media backgroundMusic = new Media(uriString);
			backgroundMusicPlayer = new MediaPlayer(backgroundMusic);
			backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
			backgroundMusicPlayer.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Detener música
	public static void stop() {
		if (backgroundMusicPlayer != null) {
			backgroundMusicPlayer.stop();
		}
	}
}
