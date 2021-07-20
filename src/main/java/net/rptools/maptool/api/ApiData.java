package net.rptools.maptool.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface ApiData {

  default JsonObject asJsonObject() {
    Gson gson = new Gson();
    return gson.toJsonTree(this).getAsJsonObject();
  }

}
