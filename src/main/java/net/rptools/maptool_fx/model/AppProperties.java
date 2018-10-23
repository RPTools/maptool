package net.rptools.maptool_fx.model;

import javafx.beans.property.SimpleBooleanProperty;

public class AppProperties {
	// TODO: Just something for now until I figure out where I want this stuff...
	SimpleBooleanProperty serverAdmin; // = new SimpleBooleanProperty(true);

	public SimpleBooleanProperty isServerAdmin() {
		return serverAdmin;
	}

	public void setServerAdmin(SimpleBooleanProperty serverAdmin) {
		this.serverAdmin = serverAdmin;
	}
	
	public void setServerAdmin(boolean serverAdmin) {
		this.serverAdmin = new SimpleBooleanProperty(serverAdmin);
	}
}
