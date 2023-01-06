/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.rptools.maptool.server.MapToolServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class MapToolRegistry {
  private static final String SERVICE_URL = "https://services-mt.rptools.net";
  private static final String REGISTER_SERVER = SERVICE_URL + "/register-server";
  private static final String ACTIVE_SERVERS = SERVICE_URL + "/active-servers";
  private static final String SERVER_HEARTBEAT = SERVICE_URL + "/server-heartbeat";
  private static final String SERVER_DISCONNECT = SERVICE_URL + "/server-disconnect";
  private static final String SERVER_DETAILS = SERVICE_URL + "/server-details";

  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  private String serverRegistrationId;

  private static final Logger log = LogManager.getLogger(MapToolServer.class);

  private static MapToolRegistry instance = new MapToolRegistry();

  public static MapToolRegistry getInstance() {
    return instance;
  }

  public static class SeverConnectionDetails {
    public String address;
    public int port;
    public boolean webrtc;
  }

  public enum RegisterResponse {
    OK,
    ERROR,
    NAME_EXISTS
  }

  private MapToolRegistry() {}

  public SeverConnectionDetails findInstance(String id) {
    OkHttpClient client = new OkHttpClient();
    String requestUrl =
        HttpUrl.parse(SERVER_DETAILS).newBuilder().addQueryParameter("name", id).build().toString();

    Request request = new Request.Builder().url(requestUrl).build();

    try (Response response = client.newCall(request).execute()) {
      JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
      SeverConnectionDetails details = new SeverConnectionDetails();

      details.address = json.getAsJsonPrimitive("address").getAsString();
      details.port = json.getAsJsonPrimitive("port").getAsInt();

      // currently the webrtc property is sent as int. In the future this will
      // change to boolean. So we check what the type is. Can be removed when
      // we get it as boolean.
      var webrtcProperty = json.getAsJsonPrimitive("webrtc");
      if (webrtcProperty.isBoolean()) {
        details.webrtc = webrtcProperty.getAsBoolean();
      } else {
        details.webrtc = webrtcProperty.getAsInt() > 0;
      }

      return details;

    } catch (IOException | NullPointerException e) {
      log.error("Error fetching instance from server registry", e);
      return new SeverConnectionDetails();
    }
  }

  public List<String> findAllInstances() {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(ACTIVE_SERVERS).build();

    try (Response response = client.newCall(request).execute()) {
      JsonArray array = JsonParser.parseString(response.body().string()).getAsJsonArray();
      List<String> servers = new ArrayList<>();
      for (JsonElement ele : array) {
        JsonObject jobj = ele.getAsJsonObject();
        String val = jobj.get("name").getAsString() + ":" + jobj.get("version").getAsString();
        servers.add(val);
      }
      return servers;
    } catch (IOException | NullPointerException e) {
      e.printStackTrace(); // TODO:
      return List.of(); // Return empty list
    }
  }

  public RegisterResponse registerInstance(String id, int port, boolean webrtc) {
    JsonObject body = new JsonObject();
    body.addProperty("name", id);
    body.addProperty("port", port);
    body.addProperty("address", getAddress());
    body.addProperty("webrtc", webrtc);
    if (MapTool.isDevelopment()) {
      body.addProperty("version", "Dev");
    } else {
      body.addProperty("version", MapTool.getVersion());
    }
    body.addProperty("clientId", MapTool.getClientId());
    Locale locale = Locale.getDefault();
    body.addProperty("country", locale.getCountry());
    body.addProperty("language", locale.getLanguage());
    TimeZone timeZone = TimeZone.getDefault();
    body.addProperty("timezone", timeZone.getID());

    OkHttpClient client = new OkHttpClient();
    RequestBody requestBody = RequestBody.create(body.toString(), JSON);

    Request request = new Request.Builder().url(REGISTER_SERVER).put(requestBody).build();
    RegisterResponse registerResponse;
    try (Response response = client.newCall(request).execute()) {
      JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();

      String status = json.get("status").getAsString();
      if ("ok".equals(status)) {
        serverRegistrationId = json.get("serverId").getAsString();
        registerResponse = RegisterResponse.OK;
      } else if ("name-exists".equals(status)) {
        registerResponse = RegisterResponse.NAME_EXISTS;
      } else {
        registerResponse = RegisterResponse.ERROR;
      }
    } catch (IOException | NullPointerException e) {
      registerResponse = RegisterResponse.ERROR;
    }

    return registerResponse;
  }

  public void unregisterInstance() {
    JsonObject body = new JsonObject();
    body.addProperty("id", serverRegistrationId);
    body.addProperty("clientId", MapTool.getClientId());

    OkHttpClient client = new OkHttpClient();
    RequestBody requestBody = RequestBody.create(body.toString(), JSON);

    Request request = new Request.Builder().url(SERVER_DISCONNECT).patch(requestBody).build();

    client
        .newCall(request)
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.error("Error unregistering server ", e);
              }

              @Override
              public void onResponse(@NotNull Call call, @NotNull Response response)
                  throws IOException {
                try {
                  response.close();
                } catch (Exception e) {
                  // Not much point doing anything...
                }
              }
            });
  }

  public void heartBeat() {
    JsonObject body = new JsonObject();
    body.addProperty("id", serverRegistrationId);
    body.addProperty("clientId", MapTool.getClientId());
    body.addProperty("address", getAddress());
    body.addProperty("number_players", MapTool.getPlayerList().size());
    body.addProperty("number_maps", MapTool.getCampaign().getZones().size());

    OkHttpClient client = new OkHttpClient();
    RequestBody requestBody = RequestBody.create(body.toString(), JSON);

    Request request = new Request.Builder().url(SERVER_HEARTBEAT).patch(requestBody).build();

    client
        .newCall(request)
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.error("Error sending heart beat", e);
              }

              @Override
              public void onResponse(@NotNull Call call, @NotNull Response response)
                  throws IOException {
                try {
                  response.close();
                } catch (Exception e) {
                  // Not much point doing anything...
                }
              }
            });
  }

  /**
   * Get the external IP address of this MapTool instance.
   *
   * @return The external IP, or the empty string if none could be determined.
   */
  public String getAddress() {
    try {
      return this.getAddressAsync().get(30, TimeUnit.SECONDS);
    } catch (Exception e) {
      return "";
    }
  }

  /**
   * Asynchronously get the external IP address of this MapTool instance.
   *
   * @return A future that resolves to the external IP address.
   */
  public Future<String> getAddressAsync() {
    List<String> ipCheckURLs;
    try (InputStream ipCheckList =
        getClass().getResourceAsStream("/net/rptools/maptool/client/network/ip-check.txt")) {
      ipCheckURLs =
          new BufferedReader(new InputStreamReader(ipCheckList, StandardCharsets.UTF_8))
              .lines()
              .map(String::trim)
              .filter(s -> !s.startsWith("#"))
              .collect(Collectors.toList());
    } catch (IOException e) {
      throw new AssertionError("Unable to read ip-check list.", e); // Shouldn't happen
    }

    final CompletableFuture<String> externalIpFuture = new CompletableFuture<>();
    final ExecutorService executor = Executors.newCachedThreadPool();
    for (String urlString : ipCheckURLs) {
      executor.execute(
          () -> {
            try {
              URL url = new URL(urlString);

              try (BufferedReader reader =
                  new BufferedReader(new InputStreamReader(url.openStream()))) {
                String ip = reader.readLine();
                if (ip != null && !ip.isEmpty()) {
                  externalIpFuture.complete(ip);
                  // A result has been found. No need to continue running tasks.
                  executor.shutdownNow();
                }
              }
            } catch (Exception t) {
              // Ignore. Hopefully another request succeeds.
            }
          });
    }

    return externalIpFuture;
  }
}
