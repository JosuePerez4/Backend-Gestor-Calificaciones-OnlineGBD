package gestor.calificaciones.gestorcalificaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class GestorcalificacionesApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestorcalificacionesApplication.class, args);
	}

}
