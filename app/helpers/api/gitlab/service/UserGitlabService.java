package helpers.api.gitlab.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sismics.sapparot.exception.ValidationException;
import helpers.api.gitlab.GitlabClient;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author jtremeaux
 */
public class UserGitlabService {
    public GitlabClient gitlabClient;

    public UserGitlabService(GitlabClient gitlabClient) {
        this.gitlabClient = gitlabClient;
    }

    /**
     * Get the user.
     *
     * @param id The user ID
     * @return User found
     */
    public boolean getUserById(Integer id) {
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/users/" + id))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .get()
                .build();
        return gitlabClient.execute(request,
                (response) -> true,
                null) != null;
    }

    /**
     * Create a new user.
     *
     * @param email The user email
     * @param username The username
     * @param name The full name
     * @param password The password
     * @return The Gitlab user ID
     */
    public Integer createUser(String email, String username, String name, String password) {
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("reset_password", "false")
                .add("username", username)
                .add("name", name)
                .add("projects_limit", "0")
                .add("can_create_group", "false")
                .add("confirm", "false")
                .add("external", "true")
                .build();
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/users"))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .post(formBody)
                .build();
        return gitlabClient.execute(request,
                (response) -> {
                    JsonObject json = new JsonParser().parse(response.body().string()).getAsJsonObject();
                    return json.get("id").getAsInt();
                },
                (response) -> {
                    String responseBody = response.body().string();
                    if (responseBody.contains("Username has already been taken")) {
                        throw new ValidationException("user_gitlab_create_username_error");
                    }
                    throw new RuntimeException("Error creating user: " + username + ", response was: " + responseBody);
                });
    }

    /**
     * Delete a user.
     *
     * @param id The ID of the user to delete
     */
    public void deleteUser(Integer id) {
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/users/" + id))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .delete()
                .build();
        gitlabClient.execute(request,
                (response) -> null,
                (response) -> {
                    throw new RuntimeException("Error deleting user: " + id + ", response was: " + response.body().string());
                });
    }
}
