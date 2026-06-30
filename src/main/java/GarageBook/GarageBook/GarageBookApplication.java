package GarageBook.GarageBook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GarageBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(GarageBookApplication.class, args);
	}

}
