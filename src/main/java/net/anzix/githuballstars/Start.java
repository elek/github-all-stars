package net.anzix.githuballstars;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.RefSpec;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cli class to backup.
 */
public class Start {


    @Argument(index = 0, metaVar = "userName", usage = "github user name", required = true)
    String userName;

    Pattern linkPattern = Pattern.compile("<(.*?)>; rel=\"(.*?)\"");

    @Argument(index = 1, metaVar = "dest", usage = "Destination directory", required = false)
    String dst;

    Path destination = Paths.get(".");


    public static void main(String[] args) throws Exception {
        Start start = new Start();
        CmdLineParser parser = new CmdLineParser(start);
        try {
            parser.parseArgument(args);
            start.run();
        } catch (CmdLineException e) {
            System.err.println("ERROR: " + e.getMessage());
            parser.printUsage(System.err);
        }


    }

    private void run() throws Exception {
        if (dst != null) {
            destination = Paths.get(dst);
        }

        URL nextUrl = new URL(String.format("https://api.github.com/users/%s/starred", userName));
        while (nextUrl != null) {
            System.out.println("retrieving " + nextUrl);
            URLConnection conn = nextUrl.openConnection();
            Gson gson = new Gson();

            JsonArray js = gson.fromJson(new Scanner(conn.getInputStream()).useDelimiter("\\Z").next(), JsonArray.class);
            System.out.println(js.size());
            for (int i = 0; i < js.size(); i++) {
                JsonObject obj = js.get(i).getAsJsonObject();
                syncRepo(obj.get("name").getAsString(), obj.get("clone_url").getAsString());
            }
            nextUrl = getNextUrl(conn);
        }

    }

    private URL getNextUrl(URLConnection conn) throws MalformedURLException {
        String link = conn.getHeaderField("Link");
        if (link == null) {
            return null;
        }
        Matcher m = linkPattern.matcher(link);
        while (m.find()) {
            if (m.group(2).equals("next")) {
                return new URL(m.group(1));
            }
        }
        return null;
    }

    private void syncRepo(String name, String clone_url) {
        try {
            System.out.println("**** Syncing " + name + " repo ****");
            Path repoDir = destination.resolve(name);
            Files.createDirectories(repoDir);
            Path gitDir = repoDir.resolve(".git");
            FileRepository fr = new FileRepository(gitDir.toFile());
            Git g = new Git(fr);
            if (!Files.exists(gitDir)) {
                fr.create(true);
            }
            StoredConfig config = fr.getConfig();
            Set<String> remotes = config.getSubsections("remote");
            if (!remotes.contains("origin")) {
                config.setString("remote", "origin", "url", clone_url);
            }
            config.save();

            RefSpec spec = new RefSpec("refs/heads/*:refs/remotes/origin/*");
            g.fetch().setRemote("origin").setProgressMonitor(new TextProgressMonitor()).setRefSpecs(spec).call();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

