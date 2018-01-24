/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.server;

import java.io.IOException;
import java.net.Socket;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Player;

/**
 * @author trevor
 */
public class Handshake {

	public interface Code {
		public static final int UNKNOWN = 0;
		public static final int OK = 1;
		public static final int ERROR = 2;
	}

	/**
	 * Server side of the handshake
	 */
	public static Player receiveHandshake(MapToolServer server, Socket s) throws IOException {
		// TODO: remove server config as a param
		ServerConfig config = server.getConfig();

		HessianInput input = new HessianInput(s.getInputStream());
		HessianOutput output = new HessianOutput(s.getOutputStream());
		output.findSerializerFactory().setAllowNonSerializable(true);

		Request request = (Request) input.readObject();

		Response response = new Response();
		response.code = Code.OK;

		boolean passwordMatches = Player.Role.valueOf(request.role) == Player.Role.GM ? config.gmPasswordMatches(request.password) : config.playerPasswordMatches(request.password);
		if (!passwordMatches) {

			// PASSWORD
			response.code = Code.ERROR;
			response.message = I18N.getString("Handshake.msg.wrongPassword");
		} else if (server.isPlayerConnected(request.name)) {

			// UNIQUE NAME
			response.code = Code.ERROR;
			response.message = I18N.getString("Handshake.msg.duplicateName");
		} else if (!MapTool.isDevelopment() && !MapTool.getVersion().equals(request.version) && !"DEVELOPMENT".equals(request.version) && !"@buildNumber@".equals(request.version)) {
			// Allows a version running without a 'version.txt' to act as client or server to any other version

			// CORRECT VERSION
			response.code = Code.ERROR;
			String clientUsed = request.version;
			String serverUsed = MapTool.getVersion();
			response.message = I18N.getText("Handshake.msg.wrongVersion", clientUsed, serverUsed);
		}
		response.policy = server.getPolicy();
		output.writeObject(response);
		return response.code == Code.OK ? new Player(request.name, Player.Role.valueOf(request.role), request.password) : null;
	}

	/**
	 * Client side of the handshake
	 */
	public static Response sendHandshake(Request request, Socket s) throws IOException {
		HessianInput input = new HessianInput(s.getInputStream());
		HessianOutput output = new HessianOutput(s.getOutputStream());
		output.findSerializerFactory().setAllowNonSerializable(true);
		output.writeObject(request);

		return (Response) input.readObject();
	}

	public static class Request {
		public String name;
		public String password;
		public String role;
		public String version;

		public Request() {
			// for serialization
		}

		public Request(String name, String password, Player.Role role, String version) {
			this.name = name;
			this.password = password;
			this.role = role.name();
			this.version = version;
		}
	}

	public static class Response {
		public int code;
		public String message;
		public ServerPolicy policy;
	}
}
