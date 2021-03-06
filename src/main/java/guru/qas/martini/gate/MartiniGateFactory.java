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

package guru.qas.martini.gate;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import guru.qas.martini.step.StepImplementation;

public interface MartiniGateFactory {

	String IMPLEMENTATION_KEY = "martini.gate.factory.implementation";

	String PROPERTY_IGNORING_GATES = "martini.gates.ignored";
	String PROPERTY_DEFAULT_GATE_PERMITS = "martini.gate.permits.default";

	String PROPERTY_GATE_PERMIT_TEMPLATE = "martini.gate.permits.%s";
	String PROPERTY_GATE_IGNORED = "IGNORED";

	@Nonnull
	Collection<MartiniGate> getGates(@Nullable StepImplementation implementation);
}
