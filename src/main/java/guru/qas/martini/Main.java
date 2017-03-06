package guru.qas.martini;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	public static void main(String[] args) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/guru/martini/applicationContext.xml");
		Martini application = context.getBean(Martini.class);
	}
}
