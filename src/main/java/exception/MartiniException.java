/*
Copyright 2017-2019 Penny Rohr Curich

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

package exception;

import java.io.Serializable;

import javax.annotation.Nullable;

import guru.qas.martini.Messages;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings({"unused"})
public class MartiniException extends RuntimeException implements Serializable {

	private static final long serialVersionUID = -2855987641286638769L;

	public MartiniException() {
		super();
	}

	public MartiniException(String message) {
		super(message);
	}

	public MartiniException(Throwable cause, Enum<?> messageKey, @Nullable Object... messageArgs) {
		super(
			Messages.getMessage(checkNotNull(messageKey, "null Enum"), messageArgs),
			checkNotNull(cause, "null Throwable"));
	}

	public MartiniException(Enum<?> messageKey, @Nullable Object... messageArgs) {
		super(Messages.getMessage(checkNotNull(messageKey, "null Enum"), messageArgs));
	}
}