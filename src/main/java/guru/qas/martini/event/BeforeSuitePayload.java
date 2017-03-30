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

@SuppressWarnings("WeakerAccess")
public class BeforeSuitePayload extends MartiniEventPayload {

	private static final long serialVersionUID = 4815505981404539677L;

	protected final SuiteIdentifier suiteIdentifier;

	public SuiteIdentifier getSuiteIdentifier() {
		return suiteIdentifier;
	}

	public BeforeSuitePayload(long timestamp, SuiteIdentifier suiteIdentifier) {
		super(timestamp);
		this.suiteIdentifier = suiteIdentifier;
	}
}
