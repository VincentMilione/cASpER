import com.google.inject.Inject;
import com.intellij.ide.impl.NewProjectUtil;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureDaemonAnalyzer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import java.io.File;
import java.nio.file.Paths;

@Mojo(name = "goaldelcazzo", defaultPhase = LifecyclePhase.COMPILE)
public class ExampleMojo extends AbstractMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        //codice da eseguire...

    }
}
