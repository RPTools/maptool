package net.rptools.maptool_fx.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.ContextMenuEvent;

public class Connections_Controller {

	@FXML private ListView<?> connectionsListView;

	private static final Logger log = LogManager.getLogger(Connections_Controller.class);

	@FXML
	void initialize() {
		assert connectionsListView != null : "fx:id=\"connectionsListView\" was not injected: check your FXML file 'Connections.fxml'.";
	}
}
