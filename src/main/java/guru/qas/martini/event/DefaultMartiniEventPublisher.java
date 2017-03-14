/*
Copyright 2017 Penny Rohr Curich

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Default implementation of a MartiniEventPublisher.
 * This implementation publishes events through Spring.
 */
@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultMartiniEventPublisher implements MartiniEventPublisher {

	protected final ApplicationEventPublisher publisher;

	@Autowired
	public DefaultMartiniEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void publish(MartiniEvent event) {
		publisher.publishEvent(event);
	}
}
