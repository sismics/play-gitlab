package helpers.api.gitlab.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import helpers.api.gitlab.GitlabClient;
import helpers.api.gitlab.model.GitlabProject;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jtremeaux
 */
public class ProjectGitlabService {
    public GitlabClient gitlabClient;

    public ProjectGitlabService(GitlabClient gitlabClient) {
        this.gitlabClient = gitlabClient;
    }

    /**
     * Get the project.
     *
     * @param id The project ID
     * @return Project found
     */
    public GitlabProject getProjectById(Integer id) {
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/projects/" + id))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .get()
                .build();
        return gitlabClient.execute(request,
                (response) -> {
                    JsonObject json = new JsonParser().parse(response.body().string()).getAsJsonObject();
                    GitlabProject gitlabProject = new GitlabProject();
                    gitlabProject.sshUrlToRepo = json.get("ssh_url_to_repo").getAsString();
                    return gitlabProject;
                },
                (response) -> {
                    throw new RuntimeException("Error getting project: " + id + ", response was: " + response.body().string());
                });
    }

    /**
     * Create a new project.
     *
     * @param name The project name
     * @param path The project path
     * @param namespaceId The Gitlab namespace ID (= group ID)
     * @return The Gitlab project ID
     */
    public Integer createProject(String name, String path, Integer namespaceId) {
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/projects"))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .post(new FormBody.Builder()
                        .add("name", name)
                        .add("path", path)
                        .add("visibility", "private")
                        .add("namespace_id", namespaceId.toString())
                        .build())
                .build();
        return gitlabClient.execute(request,
                (response) -> {
                    JsonObject json = new JsonParser().parse(response.body().string()).getAsJsonObject();
                    return json.get("id").getAsInt();
                },
                (response) -> {
                    throw new RuntimeException("Error creating project: " + path + ", response was: " + response.body().string());
                });
    }

    /**
     * Update a project.
     *
     * @param name The project name
     */
    public void updateProject(Integer id, String name) {
        RequestBody formBody = new FormBody.Builder()
                .add("name", name)
                .build();
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/projects/" + id))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .put(formBody)
                .build();
        gitlabClient.execute(request,
                null,
                (response) -> {
                    throw new RuntimeException("Error updating project: " + id + ", response was: " + response.body().string());
                });
    }

    /**
     * Create a new project.
     *
     * @param projectId The Gitlab project ID
     */
    public void deleteProject(Integer projectId) {
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/projects/" + projectId))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .delete()
                .build();
        gitlabClient.execute(request,
                null,
                (response) -> {
                    throw new RuntimeException("Error deleting project: " + request + ", response was: " + response.body().string());
                });
    }

    /**
     * Get a project secret variable by its key.
     *
     * @return Variable found
     */
    public Map<String, String> getVariables(Integer projectId) {
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/projects/" + projectId + "/variables"))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .get()
                .build();
        return gitlabClient.execute(request,
                (response) -> {
                    Map<String, String> variables = new HashMap<>();
                    JsonArray json = new JsonParser().parse(response.body().string()).getAsJsonArray();
                    for (JsonElement variable : json) {
                        String key = variable.getAsJsonObject().get("key").getAsString();
                        String value = variable.getAsJsonObject().get("value").getAsString();
                        variables.put(key, value);
                    }
                    return variables;
                },
                (response) -> {
                    throw new RuntimeException("Error getting project variables" + ", response was: " + response.body().string());
                });
    }

    /**
     * Get a project secret variable by its key.
     *
     * @return Variable found
     */
    public String getVariableByKey(Integer projectId, String key) {
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/projects/" + projectId + "/variables" + key))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .get()
                .build();
        return gitlabClient.execute(request,
                (response) -> {
                    JsonObject json = new JsonParser().parse(response.body().string()).getAsJsonObject();
                    return json.get("value").getAsString();
                },
                (response) -> {
                    throw new RuntimeException("Error getting variable: " + key + ", response was: " + response.body().string());
                });
    }

    /**
     * Create a project secret variable.
     *
     * @param projectId The Gitlab project Id
     * @param key The variable key
     * @param value The variable value
     * @param variableProtected The variable is protected
     */
    public void createVariable(Integer projectId, String key, String value, boolean variableProtected) {
        RequestBody formBody = new FormBody.Builder()
                .add("key", key)
                .add("value", value)
                .add("variableProtected", Boolean.toString(variableProtected))
                .build();
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/projects/" + projectId + "/variables"))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .post(formBody)
                .build();
        gitlabClient.execute(request,
                null,
                (response) -> {
                    throw new RuntimeException("Error creating secret variable: " + key + ", response was: " + response.body().string());
                });
    }

    /**
     * Update a project secret variable.
     *
     * @param projectId The Gitlab project Id
     * @param key The variable key
     * @param value The variable value
     * @param variableProtected The variable is protected
     */
    public void updateVariable(Integer projectId, String key, String value, boolean variableProtected) {
        RequestBody formBody = new FormBody.Builder()
                .add("value", value)
                .add("variableProtected", Boolean.toString(variableProtected))
                .build();
        Request request = new Request.Builder()
                .url(gitlabClient.getUrl("/projects/" + projectId + "/variables/" + key))
                .header("PRIVATE-TOKEN", gitlabClient.getGitlabToken())
                .put(formBody)
                .build();
        gitlabClient.execute(request,
                null,
                (response) -> {
                    throw new RuntimeException("Error updating secret variable: " + key + ", response was: " + response.body().string());
                });
    }
}
