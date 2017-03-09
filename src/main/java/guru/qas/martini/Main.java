package guru.qas.martini;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	public static void main(String[] args) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/guru/martini/martiniContext.xml");
		Bartender application = context.getBean(Bartender.class);
		application.getMuddles();
	}
}
