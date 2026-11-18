package mx.aulaaccesible.prolog;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record ConfiguracionProlog(String ejecutable, Path puente) {
    Path directorioSwi() {
        Path binario = Path.of(ejecutable).toAbsolutePath();
        if (!Files.isRegularFile(binario) || binario.getParent().getParent() == null) {
            return null;
        }
        Path carpeta = binario.getParent().getParent();
        return Files.isRegularFile(carpeta.resolve("boot.prc")) && Files.isRegularFile(carpeta.resolve("ABI"))
                ? carpeta : null;
    }

    public static ConfiguracionProlog localizar() {
        Path app = carpetaAplicacion();
        Path proyecto = Path.of("").toAbsolutePath();
        String prolog = System.getenv("AULA_SWIPL");
        if (prolog == null || prolog.isBlank()) {
            String binario = System.getProperty("os.name").startsWith("Windows") ? "swipl.exe" : "swipl";
            prolog = List.of(app.resolve("swipl/bin").resolve(binario),
                            proyecto.resolve(".tools/swipl-full/bin").resolve(binario))
                    .stream().filter(Files::isRegularFile).findFirst().map(Path::toString).orElse(binario);
        }
        String base = System.getenv("AULA_CONOCIMIENTOS");
        Path incluida = app.resolve("conocimientos");
        Path carpeta = base != null && !base.isBlank() ? Path.of(base)
                : Files.isDirectory(incluida) ? incluida : proyecto.resolve("conocimientos");
        return new ConfiguracionProlog(prolog, carpeta.resolve("puente.pl").toAbsolutePath().normalize());
    }

    private static Path carpetaAplicacion() {
        try {
            Path origen = Path.of(ConfiguracionProlog.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            return Files.isDirectory(origen) ? origen : origen.getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("No se pudo localizar Aula Accesible.", e);
        }
    }
}
