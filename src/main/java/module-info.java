module com.example.dragonballfx {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.web;

	requires org.controlsfx.controls;
	requires com.dlsc.formsfx;
	requires net.synedra.validatorfx;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.bootstrapfx.core;
	requires eu.hansolo.tilesfx;
	requires com.almasb.fxgl.all;
	requires javafx.media;
	requires java.sql;
	requires static lombok;

	opens com.example.dragonballfx to javafx.fxml;
	exports com.example.dragonballfx;
	exports main;
	opens main to javafx.fxml;
	exports model;
	opens model to javafx.fxml;
	exports sounds;
	opens sounds to javafx.fxml;
	exports view;
	opens view to javafx.fxml;
}
