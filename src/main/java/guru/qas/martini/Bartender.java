package guru.qas.martini;

import java.util.List;

import org.springframework.stereotype.Service;

import guru.qas.martini.gherkin.Muddle;
import guru.qas.martini.gherkin.MuddleFactory;

@Service
public class Bartender {

	private final MuddleFactory muddleFactory;

	Bartender(MuddleFactory muddleFactory) {
		this.muddleFactory = muddleFactory;
	}

	public void doSomething() {
		List<Muddle> muddles = muddleFactory.getMuddles();
		System.out.println("breakpoint");

		// Now, find all the steps.
		// And match the steps to the muddles to create Cocktails.
	}
}
