package mojos;

import java.io.File;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import collectors.MavenInfoCollector;
import creators.FileAggregator;

/**
 * Mojo for creating the documentation of the project in which it is running.
 * Currently only documentation about Modules.
 * 
 * @author gmittmann
 *
 */
@Mojo(name = "generateDoc")
public class DocumentationMojo extends AbstractMojo {

	@Parameter(property = "documentLocation")
	private File documentLocation;

	@Parameter(property = "project", defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(property = "session", defaultValue = "${session}")
	private MavenSession session;

	public void execute() throws MojoExecutionException, MojoFailureException {

		getLog().info(project.isExecutionRoot() ? "ROOT" : "NOT A ROOT");

		/* Collect Info */
		if(project.getPackaging().equals("jar")) {
			MavenInfoCollector mavenInfoCollector = new MavenInfoCollector(project, getLog());
			mavenInfoCollector.collectInfo();
		} else {
			getLog().info("Skipping data collection: not a jar");
		}

		/* If this is the last project/module start file aggregation */
		List<MavenProject> sortedProjects = session.getProjectDependencyGraph().getSortedProjects();
		if (sortedProjects.get(sortedProjects.size() - 1).equals(project)) {
			setDocumentLocation();
			FileAggregator aggregator = new FileAggregator(project, getLog());
			aggregator.aggregateFilesTo(documentLocation, "ALL.txt");
		}

	}

	/**
	 * Sets the location to which the aggregated file will be written to. If the
	 * current project is not the executionRoot, the parameter is searched in the
	 * aggregator pom. If there is no value there, the default value of the root
	 * directory is set. If the current project is the executionRoot, the defined
	 * value is used, if available, else the default value is applied.
	 */
	private void setDocumentLocation() {
		if (!project.isExecutionRoot()) {
			/* check value in root/ aggregator pom */
			for (MavenProject prj : session.getProjects()) {
				if (prj.isExecutionRoot()) {
					String pathInAggregatorPom = extractDocumentLocationFromConfigurationDOM(
							prj.getPlugin("codebased-documentation:cd-maven-plugin").getConfiguration());
					if (pathInAggregatorPom != null && !pathInAggregatorPom.isEmpty()) {
						documentLocation = new File(pathInAggregatorPom);
						getLog().info("Documentation location set to: " + documentLocation.getAbsolutePath());
					}
				}
			}
		}

		/*
		 * If the documentLocation is still null, it was not set in the aggregator pom.
		 * Set default value
		 */
		if (documentLocation == null) {
			documentLocation = new File(session.getExecutionRootDirectory() + "\\documentation");
			if (!documentLocation.mkdirs()) {
				documentLocation = new File(session.getExecutionRootDirectory());
			}
			getLog().info("Documentation location was set to default: " + documentLocation.getAbsolutePath());
		}

	}

	/**
	 * Tries to extract the content of the documentLocation tag.
	 * 
	 * @return String with value defined in documentLocationTag. Null if not
	 *         defined.
	 */
	private String extractDocumentLocationFromConfigurationDOM(Object domObject) {
		if (domObject instanceof Xpp3Dom) {
			Xpp3Dom docLocationChild = ((Xpp3Dom) domObject).getChild("documentLocation");
			if (docLocationChild != null) {
				return docLocationChild.getValue();
			}
		}

		return null;
	}

}
