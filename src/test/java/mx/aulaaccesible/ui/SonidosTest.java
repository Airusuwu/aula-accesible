package mx.aulaaccesible.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprueba que el catálogo de sonidos y los .mp3 generados con {@code scripts/generar-sonidos.ps1} están
 * completos: si se agrega una entrada al catálogo sin generar su audio, esta prueba falla en vez de quedar
 * en silencio en la aplicación.
 */
class SonidosTest {
    @Test
    void cadaEntradaDelCatalogoTieneSuAudioYSusDatos() throws IOException {
        var catalogo = Sonidos.catalogo();
        assertFalse(catalogo.isEmpty(), "No se pudo leer sonidos/catalogo.json");
        for (var entrada : catalogo) {
            assertTrue(entrada.tipo().equals("voz") || entrada.tipo().equals("efecto"),
                    "Tipo desconocido en " + entrada.id());
            assertFalse(entrada.texto().isBlank(), "Sin texto en " + entrada.id());
            assertNotNull(Sonidos.recurso(entrada.id()), "Falta " + entrada.id() + ".mp3; ejecuta generar-sonidos.ps1");
            try (InputStream audio = Sonidos.recurso(entrada.id()).openStream()) {
                assertTrue(audio.readNBytes(1024).length > 100, "Audio vacío en " + entrada.id());
            }
        }
    }

    @Test
    void losIdsSonUnicosYLasVocesDevuelvenSuTexto() {
        var ids = new HashSet<String>();
        for (var entrada : Sonidos.catalogo()) {
            assertTrue(ids.add(entrada.id()), "Id repetido: " + entrada.id());
        }
        assertEquals("Hoy veremos las fracciones.", Sonidos.texto("leccion-1"));
        assertEquals("", Sonidos.texto("no-existe"));
    }

    @Test
    void existenLosSonidosQueUsaLaInterfaz() {
        for (String id : new String[]{"respuesta", "area-completa", "diagnostico", "exportado", "error",
                "ejemplo-entrar", "ejemplo-salir"}) {
            assertNotNull(Sonidos.recurso(id), "Falta el sonido de la interfaz " + id);
        }
    }
}
