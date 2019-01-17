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

package guru.qas.martini.gherkin;

import org.testng.annotations.Test;

import gherkin.pickles.PickleTag;
import exception.MartiniException;
import guru.qas.martini.tag.DefaultMartiniTag;

import static org.testng.Assert.*;

public class DefaultMartiniTagTest {

	@Test
	public void testSimpleSyntax() throws MartiniException {
		PickleTag pickleTag = new PickleTag(null, "@Smoke ");
		DefaultMartiniTag tag = DefaultMartiniTag.builder().setPickleTag(pickleTag).build();
		assertEquals("Smoke", tag.getName(), "wrong name returned");
		assertNull(tag.getArgument(), "non-null argument returned");
	}

	@Test
	public void testArgumentedSyntax() throws MartiniException {
		PickleTag pickleTag = new PickleTag(null, "@Meta-Data(\"beta\")");
		DefaultMartiniTag tag = DefaultMartiniTag.builder().setPickleTag(pickleTag).build();
		assertEquals("Meta-Data", tag.getName(), "wrong name returned");
		assertEquals("beta", tag.getArgument(), "wrong argument returned");
	}

	/*
	 * Gherkin's actual parsing behavior:
	 * @Tag("one") yields one tag with name @Tag("one")
	 *
	 * but
	 *
	 * @Tag("one", "two") yields two tags:
	 * 	@Tag("one")
	 * 	"two"
	 */
	@Test(
		expectedExceptions = {MartiniException.class},
		expectedExceptionsMessageRegExp = "^illegal tag syntax:\\s.*$")
	public void testUntenableSyntax() throws MartiniException {
		PickleTag pickleTag = new PickleTag(null, "two");
		DefaultMartiniTag.builder().setPickleTag(pickleTag).build();
	}
}
