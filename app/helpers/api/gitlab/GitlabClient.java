package helpers.api.gitlab;

import com.sismics.sapparot.function.CheckedConsumer;
import com.sismics.sapparot.function.CheckedFunction;
import com.sismics.sapparot.okhttp.OkHttpHelper;
import helpers.api.gitlab.service.GroupGitlabService;
import helpers.api.gitlab.service.ProjectGitlabService;
import helpers.api.gitlab.service.UserGitlabService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import play.Play;

import static org.mockito.Mockito.mock;

/**
 * @author jtremeaux
 */
public class GitlabClient {
    private OkHttpClient client;

    private static GitlabClient gitlabClient;

    private ProjectGitlabService projectService;

    private GroupGitlabService groupService;

    private UserGitlabService userService;

    public static GitlabClient get() {
        if (gitlabClient == null) {
            gitlabClient = new GitlabClient();
        }
        return gitlabClient;
    }

    public GitlabClient() {
        client = createClient();
        if (isMock()) {
            projectService = mock(ProjectGitlabService.class);
            groupService = mock(GroupGitlabService.class);
            userService = mock(UserGitlabService.class);
        } else {
            projectService = new ProjectGitlabService(this);
            groupService = new GroupGitlabService(this);
            userService = new UserGitlabService(this);
        }
    }

    private boolean isMock() {
        return Boolean.parseBoolean(Play.configuration.getProperty("gitlab.mock", "false"));
    }

    private static OkHttpClient createClient() {
        return new OkHttpClient.Builder()
                .build();
    }

    public String getGitlabUrl() {
        return Play.configuration.getProperty("gitlab.url") + "/api/v4";
    }

    public String getGitlabToken() {
        return Play.configuration.getProperty("gitlab.token");
    }

    public String getUrl(String url) {
        return getGitlabUrl() + url;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public ProjectGitlabService getProjectService() {
        return projectService;
    }

    public GroupGitlabService getGroupService() {
        return groupService;
    }

    public UserGitlabService getUserService() {
        return userService;
    }

    public <T> T execute(Request request, CheckedFunction<Response, T> onSuccess, CheckedConsumer<Response> onFailure) {
        return OkHttpHelper.execute(getClient(), request, onSuccess, onFailure);
    }
}
