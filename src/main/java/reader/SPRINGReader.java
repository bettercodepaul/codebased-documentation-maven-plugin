package reader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

import util.Pair;

/**
 * REST Api reader for Spring Boot. Currently externalized configuration is
 * ignored. e.g. @Value annotations can't be resolved. TODO: read
 * application.properties or application.yml from src/main/resources TODO:
 * REFACTOR THIS....
 * 
 * @author gmittmann
 *
 */
public class SPRINGReader implements APIReader {

	private static final List<String> CONTROLLER_ANNOTATIONS = Arrays.asList(
			Controller.class.getSimpleName(),
			RestController.class.getSimpleName(), 
			Component.class.getSimpleName(), 
			Service.class.getSimpleName());

	private static final List<String> HTTP_METHODS = Arrays.asList(
			RequestMethod.class.getSimpleName() + "." + RequestMethod.GET.name(),
			RequestMethod.class.getSimpleName() + "." + RequestMethod.PUT.name(),
			RequestMethod.class.getSimpleName() + "." + RequestMethod.POST.name(),
			RequestMethod.class.getSimpleName() + "." + RequestMethod.DELETE.name(),
			RequestMethod.class.getSimpleName() + "." + RequestMethod.HEAD.name(),
			RequestMethod.class.getSimpleName() + "." + RequestMethod.OPTIONS.name(),
			RequestMethod.class.getSimpleName() + "." + RequestMethod.PATCH.name());

	private static final List<String> HTTP_METHODS_MAPPING = Arrays.asList(
			GetMapping.class.getSimpleName(),
			PutMapping.class.getSimpleName(), 
			PostMapping.class.getSimpleName(), 
			DeleteMapping.class.getSimpleName(),
			null, 
			"", 
			PatchMapping.class.getSimpleName());

	private Log log;

	public SPRINGReader(Log log) {
		this.log = log;
		log.info(RequestMethod.class.getSimpleName() + "." + RequestMethod.GET.name());
	}

	@Override
	public List<Pair<String, String>> getPathsAndMethods(File src) {
		JavaProjectBuilder builder = new JavaProjectBuilder();
		builder.addSourceTree(src);

		List<Pair<String, String>> paths = new ArrayList<>();

		for (JavaClass currentClass : builder.getClasses()) {

			Pair<List<Pair<String, String>>, Boolean> mappingAndController = getBaseMappingAndController(currentClass);
			boolean controller = mappingAndController.getRight();
			List<Pair<String, String>> baseMapping = mappingAndController.getLeft();

			if (controller || baseMapping != null) {
				for (JavaMethod method : currentClass.getMethods()) {
					paths.addAll(getMethodAnnotations(method, baseMapping));
				}
			}
		}

		return paths;
	}

	/**
	 * Checks whether the given JavaClass has controller annotations and/or mapping
	 * annotations. If it has,the boolean is set to true and/or the mapping found is
	 * evaluated and returned.
	 * 
	 * @param clz class under inspection.
	 * @return Pair containing the found mapping or null and boolean whether the
	 *         class is a controller class.
	 */
	private Pair<List<Pair<String, String>>, Boolean> getBaseMappingAndController(JavaClass clz) {
		boolean controller = false;
		List<Pair<String, String>> baseMapping = null;
		for (JavaAnnotation annotation : clz.getAnnotations()) {
			String annotationClass = annotation.getType().getSimpleName();

			if (CONTROLLER_ANNOTATIONS.contains(annotationClass)) {
				controller = true;
			} else if (annotationClass.equals(RequestMapping.class.getSimpleName())
					|| HTTP_METHODS_MAPPING.contains(annotationClass)) {
				baseMapping = getMapping(annotation);
			}
		}
		return new Pair<List<Pair<String, String>>, Boolean>(baseMapping, controller);
	}

	/**
	 * Searches through the annotations on the given method for mapping annotations.
	 * Evaluates them and concatenates them to the base mapping, if given and
	 * necessary.
	 * 
	 * @param method       JavaMethod whose annotations are to be evaluated
	 * @param baseMappings mappings on class level.
	 * @return List of found mappings. One Pair for each method available on the
	 *         paths found.
	 */
	private List<Pair<String, String>> getMethodAnnotations(JavaMethod method,
			List<Pair<String, String>> baseMappings) {

		List<Pair<String, String>> pairList = new ArrayList<>();

		for (JavaAnnotation annotation : method.getAnnotations()) {
			String annotationClass = annotation.getType().getSimpleName();

			if (annotationClass.equals(RequestMapping.class.getSimpleName())
					|| HTTP_METHODS_MAPPING.contains(annotationClass)) {
				List<Pair<String, String>> methodMapping = getMapping(annotation);

				for (Pair<String, String> currentMapping : methodMapping) {
					if (currentMapping.getRight() != null && baseMappings != null) {
						for (Pair<String, String> base : baseMappings) {
							pairList.add(new Pair<String, String>(
									startWithoutSlash(base.getLeft()) + startWithSlash(currentMapping.getLeft()),
									currentMapping.getRight()));
						}
					} else if (currentMapping.getRight() != null) {
						pairList.add(currentMapping);
					} else {
						if (baseMappings != null) {
							for (Pair<String, String> base : baseMappings) {
								if (base.getRight() != null) {
									pairList.add(new Pair<String, String>(
											startWithoutSlash(base.getLeft()) + startWithSlash(currentMapping.getLeft()),
											base.getRight()));
								} else {
									for (String httpMethod : HTTP_METHOD_TYPE) {
										pairList.add(new Pair<String, String>(
												startWithoutSlash(base.getLeft()) + startWithSlash(currentMapping.getLeft()),
												httpMethod));
									}
								}
							}
						} else {
							for (String httpMethod : HTTP_METHOD_TYPE) {
								pairList.add(new Pair<String, String>(startWithoutSlash(currentMapping.getLeft()),
										httpMethod));
							}
						}
					}
				}
			}
		}

		return pairList;
	}

	/**
	 * Evaluates the given JavaAnnotation (should be of type RequestMapping or
	 * GetMapping, PutMapping etc.) and returns pairs with the found path and
	 * method.
	 * 
	 * @param annotation JavaAnnotation to be evaluated.
	 * @return List of found mappings.
	 */
	private List<Pair<String, String>> getMapping(JavaAnnotation annotation) {
		List<Pair<String, String>> pairs = new ArrayList<>();

		List<String> paths = new ArrayList<>();
		List<String> methods = new ArrayList<>();

		Object obj = annotation.getPropertyMap().get("value");
		obj = (obj == null) ? null : annotation.getPropertyMap().get("value").getParameterValue();
		if (obj instanceof String) {
			paths.add(((String) obj).trim());
		} else if (obj instanceof List) {
			paths.addAll((List<String>) obj);
		} else {
			if (obj == null) {
				paths.add("");
			} else {
				log.error("Value type: " + obj.toString());
			}
		}

		if (annotation.getType().getSimpleName().contentEquals(RequestMapping.class.getSimpleName())) {
			obj = annotation.getNamedParameter("method");

			if (obj instanceof List) {
				for (String meth : (List<String>) obj) {
					if (HTTP_METHODS.contains(meth.toString())) {
						int index = HTTP_METHODS.indexOf(meth.toString());
						methods.add(HTTP_METHOD_TYPE.get(index));
					}
				}
			} else if (obj instanceof String) {
				if (HTTP_METHODS.contains(obj.toString())) {
					int index = HTTP_METHODS.indexOf(obj.toString());
					methods.add(HTTP_METHOD_TYPE.get(index));
				}
			} else {
				if (obj == null)
					log.info("Method was null");

				methods = null;
			}
		} else {
			int index = HTTP_METHODS_MAPPING.indexOf(annotation.getType().getSimpleName());
			log.info(String.valueOf(index));
			if (index != -1) {
				methods.add(HTTP_METHOD_TYPE.get(index));
			}
		}

		for (String path : paths) {
			path = path.replace("\"", "");
			if (methods != null && !methods.isEmpty()) {
				for (String method : methods) {
					pairs.add(new Pair<String, String>(path, method));
				}
			} else {
				pairs.add(new Pair<String, String>(path, null));
			}
		}

		return pairs;
	}

	/**
	 * Makes sure the given String starts with a '/'.
	 * 
	 * @param toAdd String to be brought to form.
	 * @return the given String starting with a '/'
	 */
	private String startWithSlash(String toAdd) {
		toAdd = toAdd.trim();
		if (toAdd.isEmpty() || toAdd.startsWith("/")) {
			return toAdd;
		}
		return "/" + toAdd;
	}

	/**
	 * Makes sure the given string does not start with a '/'.
	 * 
	 * @param uri String to be brought to form.
	 * @return the given String without a '/' in front.
	 */
	private String startWithoutSlash(String uri) {
		uri = uri.trim();
		if (uri.startsWith("/")) {
			return uri.substring(1);
		} else {
			return uri;
		}
	}

}
