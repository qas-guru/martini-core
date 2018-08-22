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

package guru.qas.martini.tag;

import java.util.Set;

import guru.qas.martini.Martini;

public interface Categories {

	String IMPLEMENTATION_KEY = "martini.categories.implementation";

	boolean isMatch(String classification, MartiniTag tag);

	/*
	 * @param martini
	 * @return names of immediate and all ancestor categories for given Martini
	 */
	Set<String> getCategorizations(Martini martini);
}
