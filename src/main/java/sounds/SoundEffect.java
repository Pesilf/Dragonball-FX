package sounds;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.nio.file.Paths;

public class SoundEffect {

	// Reproducir efecto de sonido
	public static void play(String filePath) {
		try {
			String uriString = Paths.get(filePath).toUri().toString();
			Media soundEffect = new Media(uriString);
			MediaPlayer soundEffectPlayer = new MediaPlayer(soundEffect);
			soundEffectPlayer.play();
		} catch (Exception e) {
					e.printStackTrace();
		}
	}
}
