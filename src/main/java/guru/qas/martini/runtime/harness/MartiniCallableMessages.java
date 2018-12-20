/*
Copyright 2018 Penny Rohr Curich

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

package guru.qas.martini.runtime.harness;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("guru.qas.martini.runtime.martiniCallableMessages")
@LocaleData({@Locale("en")})
public enum MartiniCallableMessages {
	EXECUTING_STEP,
	PASSED, FAILED,
	FAILED_WITH_EXCEPTION,
	SKIPPED,
	INTERRUPTED,
	UNEXPECTED_EXCEPTION,
	MISSING_EXAMPLES,
	INVALID_EXAMPLES_HEADER,
	INVALID_EXAMPLES_FORMAT,
	INVALID_SUBSTITUTION,
	NO_MATCHER,
	STARTING,
	EXECUTION_EXCEPTION;
}
