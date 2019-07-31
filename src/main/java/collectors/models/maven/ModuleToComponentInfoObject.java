package collectors.models.maven;

import java.util.ArrayList;
import java.util.List;

import collectors.models.InfoObject;

/**
 * Class containing info about which module contains which components.
 * @author gmittmann
 *
 */
public class ModuleToComponentInfoObject extends InfoObject {
	
	private String moduleTag;
	private List<ComponentInfoObject> components; 

	public ModuleToComponentInfoObject(String name) {
		this.setModuleName(name);
	}

	public List<ComponentInfoObject> getComponents() {
		return components;
	}

	public void setComponents(List<ComponentInfoObject> components) {
		this.components = components;
	}
	
	public void addComponent(ComponentInfoObject component) {
		if (this.components == null) {
			this.components = new ArrayList<>();
		}
		components.add(component);
	}

	public String getModuleName() {
		return moduleTag;
	}

	public void setModuleName(String moduleTag) {
		this.moduleTag = moduleTag;
	}

}
