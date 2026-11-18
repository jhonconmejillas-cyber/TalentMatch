package co.edu.javeriana.talentmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

// @SpringBootApplication: marca esta clase como el punto de arranque.
// Spring busca controllers, services, etc. en este paquete y sus subpaquetes.
@SpringBootApplication
// @ConfigurationPropertiesScan: busca las clases con @ConfigurationProperties
// (las del paquete config) y las llena con los valores del YAML.
@ConfigurationPropertiesScan
public class TalentmatchApplication {

	// Metodo main normal de Java: enciende Spring y el servidor en el puerto 8080.
	public static void main(String[] args) {
		SpringApplication.run(TalentmatchApplication.class, args);
	}

}