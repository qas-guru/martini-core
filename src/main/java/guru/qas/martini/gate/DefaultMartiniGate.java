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
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class DefaultMartiniGate implements MartiniGate {

	private final String name;
	private final Semaphore semaphore;
	private final AtomicBoolean hasPermit;

	@Override
	public String getName() {
		return name;
	}

	protected DefaultMartiniGate(String name, Semaphore semaphore) {
		this.name = checkNotNull(name, "null String");
		this.semaphore = checkNotNull(semaphore, "null Semaphore");
		this.hasPermit = new AtomicBoolean(false);
	}

	@Override
	public int getPermits() {
		return semaphore.availablePermits();
	}

	@Override
	public boolean enter() {
		synchronized (hasPermit) {
			checkState(!hasPermit.get(), "gate already holds a permit");
			try {
				hasPermit.set(semaphore.tryAcquire(0, TimeUnit.SECONDS)); // prevents barging; see javadocs
			}
			catch (InterruptedException ignored) {
			}
			return hasPermit.get();
		}
	}

	@Override
	public synchronized void leave() {
		synchronized (hasPermit) {
			if (hasPermit.get()) {
				semaphore.release();
				hasPermit.set(false);
			}
		}
	}

	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

	protected MoreObjects.ToStringHelper getToStringHelper() {
		synchronized (hasPermit) {
			return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("permits", semaphore.availablePermits())
				.add("hasPermit", hasPermit.get());
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
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
