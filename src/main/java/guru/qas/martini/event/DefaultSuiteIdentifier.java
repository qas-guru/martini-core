/*
Copyright 2017-2018 Penny Rohr Curich

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package guru.qas.martini.event;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings({"WeakerAccess", "unused"})
@Configurable
public class DefaultSuiteIdentifier implements SuiteIdentifier, ApplicationContextAware, InitializingBean {

	protected final Logger logger;
	protected ApplicationContext applicationContext;

	private UUID id;
	private Long startTimestamp;
	private String name;
	private String hostName;
	private String hostAddress;
	private String username;
	private ImmutableSet<String> profiles;
	private ImmutableMap<String, String> environmentVariables;

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public Optional<String> getHostName() {
		return Optional.ofNullable(hostName);
	}

	@Override
	public Optional<String> getHostAddress() {
		return Optional.ofNullable(hostAddress);
	}

	@Override
	public Long getStartTimestamp() {
		return startTimestamp;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Optional<String> getUsername() {
		return Optional.ofNullable(username);
	}

	@Override
	public ImmutableSet<String> getProfiles() {
		return profiles;
	}

	@Override
	public ImmutableMap<String, String> getEnvironmentVariables() {
		return environmentVariables;
	}

	protected DefaultSuiteIdentifier() {
		this.logger = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
		checkNotNull(applicationContext, "null ApplicationContext");
		this.applicationContext = applicationContext;
	}

	@SuppressWarnings("RedundantThrows")
	@Override
	public void afterPropertiesSet() throws Exception {
		initializeUUID();
		initializeStartupDate();
		initializeDisplayName();
		initializeHostInformation();
		initializeUsername();
		initializeActiveProfiles();
		initializeEnvironmentVariables();
	}

	protected void initializeUUID() {
		this.id = UUID.randomUUID();
	}

	protected void initializeStartupDate() {
		startTimestamp = applicationContext.getStartupDate();
	}

	protected void initializeDisplayName() {
		name = applicationContext.getDisplayName();
	}

	protected void initializeHostInformation() {
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			hostName = localHost.getHostName();
			hostAddress = localHost.getHostAddress();
		}
		catch (UnknownHostException e) {
			logger.warn("unable to ascertain hostname and address", e);
		}
	}

	protected void initializeUsername() {
		username = System.getProperty("user.name");
	}

	protected void initializeActiveProfiles() {
		Environment environment = applicationContext.getEnvironment();
		String[] activeProfiles = environment.getActiveProfiles();
		profiles = ImmutableSet.copyOf(activeProfiles);
	}

	protected void initializeEnvironmentVariables() {
		Map<String, String> variables = System.getenv();
		environmentVariables = ImmutableMap.copyOf(variables);
	}
}
