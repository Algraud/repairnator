package fr.inria.spirals.repairnator.process.git;

import fr.inria.spirals.repairnator.process.inspectors.JobStatus;
import fr.inria.spirals.repairnator.process.inspectors.Metrics;
import fr.inria.spirals.repairnator.process.inspectors.metrics4bears.Metrics4Bears;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class GitHelperTest {

    private File tmpDir;

    @After
    public void tearDown() throws IOException {
        GitHelper.deleteFile(tmpDir);
    }

    @Test
    public void testcomputePatchStats() throws GitAPIException, IOException {
        JobStatus jobStatus = new JobStatus("fakePomDirPath");

        String remoteRepo = "https://github.com/Spirals-Team/jtravis.git";
        String parentCommit = "2d65266f9a52b27f955ec9a74aa9ab4dac5537d7";
        String commit = "f267c73200e2ebb9431d6ffe80e507222567696c"; // GH says: 14 changed files, 443 additions, 104 deletions,
                                                                    // on java files is: 13 changed files, 405 additions, 104 deletions
        tmpDir = java.nio.file.Files.createTempDirectory("jtravis").toFile();
        Git git = Git.cloneRepository().setURI(remoteRepo).setBranch("master").setDirectory(tmpDir).call();

        RevWalk revwalk = new RevWalk(git.getRepository());

        RevCommit revParentCommit = revwalk.parseCommit(ObjectId.fromString(parentCommit));
        RevCommit revCommit = revwalk.parseCommit(ObjectId.fromString(commit));

        GitHelper gitHelper = new GitHelper();
        Metrics metrics = jobStatus.getMetrics();
        Metrics4Bears metrics4Bears = jobStatus.getMetrics4Bears();
        gitHelper.computePatchStats(jobStatus, git, revCommit, revParentCommit);
        assertEquals(13, metrics.getPatchChangedFiles());
        assertEquals(8, metrics4Bears.getPatchDiff().getFiles().getNumberAdded());
        assertEquals(1, metrics4Bears.getPatchDiff().getFiles().getNumberDeleted());
        assertEquals(4, metrics4Bears.getPatchDiff().getFiles().getNumberChanged());
        assertEquals(405, metrics.getPatchAddedLines());
        assertEquals(104, metrics.getPatchDeletedLines());
    }
}
