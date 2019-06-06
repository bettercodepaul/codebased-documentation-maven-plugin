package collectors.models.maven;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import collectors.models.InfoObject;

public class CollectedMavenInfoObject extends InfoObject{
	
	private String tag;
	private String system;
	private String subsystem;
	private Map<String, List<String>> moduleDependencies;
	private List<ModuleInfoObject> modules;
	private List<ComponentInfoObject> components;
	
	public CollectedMavenInfoObject(String projectName, String tag, String system, String subsystem) {
		super(projectName);
		this.tag = tag;
		this.system = system;
		this.subsystem = subsystem;
	}
	
	public Map<String, List<String>> getModuleDependencies() {
		return moduleDependencies;
	}

	public void setModuleDependencies(Map<String, List<String>> dependencyGraphEdges) {
		this.moduleDependencies = dependencyGraphEdges;
	}
	
	public void addModuleDependencies(String node, List<String> edges) {
		if (this.moduleDependencies == null) {
			this.moduleDependencies = new HashMap<>();
		}
		moduleDependencies.put(node, edges);
	}

	public List<ModuleInfoObject> getModules() {
		return modules;
	}

	public void setModules(List<ModuleInfoObject> modules) {
		this.modules = modules;
	}

	public List<ComponentInfoObject> getComponents() {
		return components;
	}

	public void setComponents(List<ComponentInfoObject> components) {
		this.components = components;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public String getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(String subsystem) {
		this.subsystem = subsystem;
	}

}
