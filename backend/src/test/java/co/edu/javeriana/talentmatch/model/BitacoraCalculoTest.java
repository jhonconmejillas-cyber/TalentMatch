package co.edu.javeriana.talentmatch.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class BitacoraCalculoTest {

    @Test
    void laBitacoraNoTieneSettersPublicos() {
        // RNF-06: la bitacora es de solo lectura.
        List<String> setters = Arrays.stream(BitacoraCalculo.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .filter(n -> n.startsWith("set"))
                .toList();

        assertTrue(setters.isEmpty(), "BitacoraCalculo no debe tener setters publicos: " + setters);
    }

    @Test
    void elModeloNoDependeDeSpring() throws IOException {
        Path carpeta = Path.of("src/main/java/co/edu/javeriana/talentmatch/model");
        try (Stream<Path> archivos = Files.list(carpeta)) {
            List<Path> conSpring = archivos
                    .filter(a -> leer(a).contains("import org.springframework"))
                    .toList();
            assertTrue(conSpring.isEmpty(), "Clases de model/ que dependen de Spring: " + conSpring);
        }
    }

    private static String leer(Path archivo) {
        try {
            return Files.readString(archivo);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
