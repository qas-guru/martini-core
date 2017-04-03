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

package guru.qas.martini.result;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
public class ElapsedTime implements Serializable {

	private static final long serialVersionUID = -8934377247352143495L;

	protected Long startTimestamp;
	protected Long endTimestamp;

	protected Long getStartTimestamp() {
		return startTimestamp;
	}

	protected void setStartTimestamp(Long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	protected Long getEndTimestamp() {
		return endTimestamp;
	}

	protected void setEndTimestamp(Long endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	protected Long getExecutionTime(TimeUnit unit) {
		checkNotNull(unit, "null TimeUnit");
		Long start = getStartTimestamp();
		Long end = null == start ? null : getEndTimestamp();
		Long millis = null == end ? null : end - start;
		return null == millis ? null : unit.convert(millis, TimeUnit.MILLISECONDS);
	}
}
