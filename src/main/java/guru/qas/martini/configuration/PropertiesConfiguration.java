package guru.qas.martini.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Configuration
public class PropertiesConfiguration {

	@Bean
	static PropertySourcesPlaceholderConfigurer getConfigurer(Environment environment) {
		String[] defaultProfiles = environment.getDefaultProfiles();
		String[] activeProfiles = environment.getActiveProfiles();
		List<Resource> resources = getResources(defaultProfiles, activeProfiles);
		return getConfigurer(resources);
	}

	private static List<Resource> getResources(String[] defaultProfiles, String[] activeProfiles) {
		ArrayList<String> defaultList = Lists.newArrayList(defaultProfiles);
		ArrayList<String> activeList = Lists.newArrayList(activeProfiles);
		Iterable<String> profiles = Iterables.concat(defaultList, activeList);
		return getResources(profiles);
	}

	private static List<Resource> getResources(Iterable<String> profiles) {
		List<Resource> resources = new ArrayList<>();
		for (String profile : profiles) {
			ClassPathResource resource = getResource(profile);
			resources.add(resource);
		}
		return resources.stream().filter(Objects::nonNull).collect(Collectors.toList());
	}

	private static ClassPathResource getResource(String profile) {
		String location = String.format("/guru/martini/%s.properties", profile);
		ClassPathResource resource = new ClassPathResource(location);
		return resource.exists() ? resource : null;
	}

	private static PropertySourcesPlaceholderConfigurer getConfigurer(List<Resource> resources) {
		Resource[] asArray = resources.toArray(new Resource[resources.size()]);
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		configurer.setLocations(asArray);
		return configurer;
	}
}
