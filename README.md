# Aula Accesible

Aula Accesible ayuda a revisar la accesibilidad básica de una sección de software educativo, como un cuestionario, una lección o una actividad interactiva. Surgió como proyecto de Programación Lógica y Funcional para mostrar cómo un sistema experto explica el resultado de una evaluación.

La aplicación presenta 24 criterios sobre navegación con teclado, presentación visual, multimedia, formularios, organización del contenido e interacción. Puedes avanzar, regresar y corregir respuestas. Al generar el diagnóstico, verás las barreras reportadas, los criterios pendientes y las recomendaciones por prioridad. También puedes abrir un problema combinado para ver qué respuestas activaron su regla.

JavaFX muestra el cuestionario y recoge las respuestas. SWI-Prolog consulta la base de conocimientos y genera el diagnóstico. El resultado orienta la revisión de la interfaz a partir de lo que reporta la persona que la evalúa.

## Ejecutar desde el código en Windows

Necesitas [JDK 17](https://learn.microsoft.com/java/openjdk/download) y [SWI-Prolog](https://www.swi-prolog.org/download/stable). Probamos el proyecto con SWI-Prolog 10.0.2 x64. Abre PowerShell en la carpeta del repositorio y sustituye las rutas de ejemplo:

```powershell
$env:JAVA_HOME = 'C:\ruta\al\jdk-17'
$env:AULA_SWIPL = 'C:\ruta\a\swipl\bin\swipl.exe'
.\mvnw.cmd javafx:run
```

El Maven Wrapper descarga Maven y resuelve JavaFX y las demás dependencias en la primera ejecución. Si `swipl.exe` ya está en el PATH, puedes omitir `AULA_SWIPL`. Si usas VS Code, **Extension Pack for Java** facilita la edición y la depuración.

## Probar el ejemplo

1. Pulsa **Probar con un ejemplo** y luego **Ver diagnóstico parcial**. El ejemplo completo muestra **11 favorables, 6 barreras, 1 pendiente y 6 criterios que no aplican**.
2. En **Problemas combinados**, abre **Interacción restringida**. La regla 17 se activa porque tanto la operación con teclado como la alternativa al arrastre tienen respuesta «No».
3. Pulsa **Editar respuestas**, entra a **Interacción y control** y elige **Alternativa al arrastre** en **Ir a un criterio**. Cámbiala a «Sí» y pulsa **Ver diagnóstico**. Verás **12 favorables y 5 barreras**. La interacción restringida desaparece y el estado sigue siendo **Requiere atención prioritaria**.
4. En **Pendientes**, usa **Ir al criterio** para retomar la pregunta que falta verificar. También puedes iniciar una evaluación nueva, responder solo la primera pregunta y revisar los 23 criterios sin responder.

El caso está incluido en la base de conocimientos y se carga desde Prolog.

En el cuestionario, abre **Cómo evaluar este criterio** si necesitas pasos para comprobarlo y saber cuándo marcar «Sí». La ayuda comienza cerrada en cada pregunta.

Para conservar el diagnóstico, pulsa **Exportar informe** y elige dónde guardar el HTML. Puedes abrirlo sin conexión en un navegador e imprimirlo como cualquier página.

## Archivos y pruebas

- `src/` contiene la interfaz JavaFX, el modelo de respuestas y la conexión con Prolog.
- `conocimientos/aula_accesible.pl` contiene los criterios, las reglas y el ejemplo. `conocimientos/puente.pl` recibe las solicitudes de Java en JSON y devuelve el diagnóstico.
- `scripts/` incluye las tareas de compilación, pruebas y empaquetado para Windows.

Para ejecutar las pruebas de Prolog y las de integración con Java, conserva las variables de la sección anterior y usa:

```powershell
powershell -NoProfile -File .\scripts\probar.ps1
```

Los siguientes pasos son guardar y reabrir evaluaciones y comparar los resultados antes y después de corregir una interfaz.

La interfaz incluye Atkinson Hyperlegible del Braille Institute. [Licencia SIL OFL](src/main/resources/mx/aulaaccesible/fuentes/OFL.txt).
