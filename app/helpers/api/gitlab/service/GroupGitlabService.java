package helpers.api.gitlab.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import helpers.api.gitlab.GitlabClient;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jtremeaux
 */
public class GroupGitlabService {
    public GitlabClient gitlabClient;

    public GroupGitlabService(GitlabClient gitlabClient) {
        this.gitlabClient = gitlabClient;
    }

    /**
     * Get the group.
     *
     * @param id The group ID
     * @return Group found
     */
    public boolean getGroupById(Integer id) {
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/groups/" + id))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .get()
                .build();
        return gitlabClient.execute(request,
                (response) -> true,
                null) != null;
    }

    /**
     * Get the group users.
     *
     * @param groupId The group ID
     * @return The users
     */
    public List<Integer> getGroupMember(Integer groupId) {
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/groups/" + groupId + "/members"))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .get()
                .build();
        return gitlabClient.execute(request,
                (response) -> {
                    List<Integer> users = new ArrayList<>();
                    JsonArray members = new JsonParser().parse(response.body().string()).getAsJsonArray();
                    for (JsonElement json : members) {
                        JsonObject member = json.getAsJsonObject();
                        Integer userId = member.get("id").getAsInt();
                        String userName = member.get("username").getAsString();
                        Integer access_level = member.get("access_level").getAsInt();
                        if ("controlplane".equals(userName)) {
                            continue;
                        }
                        users.add(userId);
                    }
                    return users;
                },
                (response) -> {
                    throw new RuntimeException("Error getting group users: " + groupId + ", response was: " + response.body().string());
                });
    }

    /**
     * Create a new group.
     *
     * @param name The group name
     * @param path The group path
     * @return The Gitlab group ID
     */
    public Integer createGroup(String name, String path) {
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/groups"))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .post(new FormBody.Builder()
                        .add("name", name)
                        .add("path", path)
                        .add("visibility", "private")
                        .build())
                .build();
        return gitlabClient.execute(request,
                (response) -> {
                    JsonObject json = new JsonParser().parse(response.body().string()).getAsJsonObject();
                    return json.get("id").getAsInt();
                },
                (response) -> {
                    throw new RuntimeException("Error creating group: " + path + ", response was: " + response.body().string());
                });
    }

    /**
     * Update a group.
     *
     * @param name The group name
     */
    public void updateProject(Integer id, String name) {
        RequestBody formBody = new FormBody.Builder()
                .add("name", name)
                .build();
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/groups/" + id))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .put(formBody)
                .build();
        gitlabClient.execute(request,
                null,
                (response) -> {
                    throw new RuntimeException("Error updating group: " + id + ", response was: " + response.body().string());
                });
    }

    /**
     * Add a user to a group.
     *
     * @param groupId The group ID
     * @param userId The user ID
     */
    public void createGroupUser(Integer groupId, Integer userId) {
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", userId.toString())
                .add("access_level", "40")
                .build();
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/groups/" + groupId + "/members"))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .post(formBody)
                .build();
        gitlabClient.execute(request,
                null,
                (response) -> {
                    throw new RuntimeException("Error adding user: " + userId + " to group: " + groupId + ", response was: " + response.body().string());
                });
    }

    /**
     * Remove a user to a group.
     *
     * @param groupId The group ID
     * @param userId The user ID
     */
    public void deleteGroupUser(Integer groupId, Integer userId) {
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/groups/" + groupId + "/members/" + userId))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .delete()
                .build();
        gitlabClient.execute(request,
                null,
                (response) -> {
                    throw new RuntimeException("Error deleting user: " + userId + " from group: " + groupId + ", response was: " + response.body().string());
                });
    }
}
