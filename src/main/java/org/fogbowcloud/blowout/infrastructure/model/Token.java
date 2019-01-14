package org.fogbowcloud.blowout.infrastructure.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Token {

    private String accessId;
    private User user;

    public Token(String acessId, User user) {
        this.accessId = acessId;
        this.user = user;
    }

    public String getAccessId() {
        return accessId;
    }

    public void setAcessId(String acessId) {
        this.accessId = acessId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public JSONObject toJSON() throws JSONException {
        return new JSONObject().put("access_id", accessId).put("user", user != null ? user.toJSON() : null);
    }

    public static Token fromJSON(String jsonStr) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonStr);
        String accessId = jsonObject.optString("access_id");
        JSONObject userJson = jsonObject.optJSONObject("user");
        return new Token(!accessId.isEmpty() ? accessId : null,
                userJson != null ? User.fromJSON(userJson.toString()) : null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return accessId.equals(token.accessId) &&
                user.equals(token.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessId, user);
    }

    @Override
    public String toString() {
        return "Token{" +
                "accessId='" + accessId + '\'' +
                ", user=" + user +
                '}';
    }
}
