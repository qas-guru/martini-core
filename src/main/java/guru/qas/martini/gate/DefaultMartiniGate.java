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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class DefaultMartiniGate implements MartiniGate {

	private final String name;
	private final Semaphore semaphore;

	protected DefaultMartiniGate(String name, Semaphore semaphore) {
		this.name = checkNotNull(name, "null String");
		this.semaphore = checkNotNull(semaphore, "null Semaphore");
	}

	@Override
	public boolean enter() {
		boolean evaluation = false;
		try {
			evaluation = semaphore.tryAcquire(0, TimeUnit.SECONDS); // prevents barging; see javadocs
		}
		catch (InterruptedException ignored) {
		}
		return evaluation;
	}

	@Override
	public void leave() {
		semaphore.release();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DefaultMartiniGate)) {
			return false;
		}
		DefaultMartiniGate that = (DefaultMartiniGate) o;
		return Objects.equal(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}
}
