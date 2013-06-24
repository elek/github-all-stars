package net.anzix.githuballstars;

/**
 * Response object from the github API interrface.
 * <p/>
 * With only the interested parameters...
 */
public class GithubRepo {
    private String name;
    private String clone_url;

    public GithubRepo(String name, String clone_url) {
        this.name = name;
        this.clone_url = clone_url;
    }

    public GithubRepo() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClone_url() {
        return clone_url;
    }

    public void setClone_url(String clone_url) {
        this.clone_url = clone_url;
    }
}
