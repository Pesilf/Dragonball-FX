package com.example.dragonballfx;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

// Esto podría haberlo eliminado, pero una vez lo hice y se me fastidió el proyecto.
// Para evitar cualquier problema no quité nada que viniera por defecto
public class HelloController {

	@FXML
	private Label welcomeText;

	@FXML
	protected void onHelloButtonClick() {

		welcomeText.setText("Welcome to JavaFX Application!");
	}
}
