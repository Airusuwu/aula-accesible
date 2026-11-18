:- encoding(utf8).
:- dynamic respuesta/3.

% Criterios y reglas transcritos del PDF. El orden de los hechos es el del cuestionario.
criterio(teclado_completo, navegacion_teclado, alta,
    '¿Se pueden realizar las acciones con el teclado?',
    'Permitir el uso de enlaces, botones y controles con teclado.').
criterio(foco_visible, navegacion_teclado, alta,
    '¿Se identifica visualmente el elemento que tiene el foco?',
    'Mostrar un indicador visible del foco del teclado.').
criterio(orden_foco, navegacion_teclado, media,
    '¿El foco recorre los elementos en un orden lógico?',
    'Organizar el recorrido del foco según el orden de la interfaz.').
criterio(sin_trampas_teclado, navegacion_teclado, alta,
    '¿Se puede salir de todos los componentes usando el teclado?',
    'Permitir salir de menús, cuadros de diálogo y otros componentes.').
criterio(contraste_legible, presentacion_visual, alta,
    '¿Se verificó que el contraste permite distinguir el texto?',
    'Revisar el contraste entre el texto y el fondo con una herramienta.').
criterio(informacion_sin_color, presentacion_visual, alta,
    '¿La información se entiende sin depender únicamente del color?',
    'Complementar el color con texto, símbolos o patrones.').
criterio(ampliacion_texto, presentacion_visual, media,
    '¿Se puede ampliar el texto sin perder contenido o funciones?',
    'Adaptar la interfaz para permitir ampliar el texto.').
criterio(redistribucion_contenido, presentacion_visual, media,
    '¿El contenido se adapta a una ventana estrecha sin superponerse?',
    'Redistribuir el contenido para evitar superposiciones y recortes.').
criterio(alternativas_imagenes, contenido_multimedia, alta,
    '¿Las imágenes informativas tienen una alternativa textual?',
    'Agregar alternativas textuales que comuniquen la información.').
criterio(subtitulos_videos, contenido_multimedia, alta,
    '¿Los videos con voz incluyen subtítulos?',
    'Agregar subtítulos que representen el contenido hablado y sonoro.').
criterio(transcripcion_audios, contenido_multimedia, alta,
    '¿Los materiales de solo audio tienen transcripción?',
    'Proporcionar una transcripción textual del material de audio.').
criterio(alternativa_visual_video, contenido_multimedia, alta,
    '¿La información visual importante del video también se describe?',
    'Describir la información visual necesaria para comprender el video.').
criterio(etiquetas_campos, formularios_evaluaciones, alta,
    '¿Cada campo tiene una etiqueta asociada que explica su propósito?',
    'Asignar etiquetas claras y asociadas a cada campo.').
criterio(instrucciones_campos, formularios_evaluaciones, media,
    '¿Se explican los formatos y requisitos antes de llenar los campos?',
    'Indicar formatos, campos obligatorios y requisitos de captura.').
criterio(errores_explicados, formularios_evaluaciones, alta,
    '¿Los errores indican qué ocurrió y cómo corregirlo?',
    'Identificar el campo con error y explicar cómo corregirlo.').
criterio(revision_envio, formularios_evaluaciones, media,
    '¿Se pueden revisar las respuestas antes de una entrega definitiva?',
    'Permitir revisar y corregir respuestas antes del envío definitivo.').
criterio(encabezados_estructurados, organizacion_comprension, media,
    '¿El contenido utiliza encabezados con una jerarquía lógica?',
    'Organizar el contenido con encabezados jerárquicos.').
criterio(enlaces_descriptivos, organizacion_comprension, media,
    '¿El texto de cada enlace permite entender su destino o función?',
    'Utilizar textos descriptivos en los enlaces.').
criterio(instrucciones_claras, organizacion_comprension, media,
    '¿Las actividades tienen instrucciones claras y ordenadas?',
    'Redactar instrucciones concretas y dividirlas en pasos.').
criterio(navegacion_consistente, organizacion_comprension, media,
    '¿Los menús y controles repetidos mantienen una organización consistente?',
    'Mantener ubicaciones, nombres y funciones consistentes.').
criterio(tiempo_ajustable, interaccion_control, alta,
    '¿Se puede ampliar o desactivar el tiempo límite cuando corresponde?',
    'Ofrecer ajustes de tiempo en las actividades que lo permitan.').
criterio(control_movimiento, interaccion_control, media,
    '¿Se pueden pausar los elementos animados o en movimiento?',
    'Agregar controles para pausar u ocultar contenido en movimiento.').
criterio(control_audio, interaccion_control, media,
    '¿El audio automático se puede detener o controlar de forma independiente?',
    'Evitar audio automático o proporcionar controles accesibles.').
criterio(alternativa_arrastre, interaccion_control, alta,
    '¿Las actividades de arrastrar tienen una alternativa sin arrastre?',
    'Ofrecer selección por botones o teclado como alternativa al arrastre.').

barrera(Evaluacion, Criterio, Area, Prioridad) :-
       respuesta(Evaluacion, Criterio, no),
       criterio(Criterio, Area, Prioridad, _, _).

condicion_favorable(Evaluacion, Criterio) :-
       respuesta(Evaluacion, Criterio, si),
       criterio(Criterio, _, _, _, _).

recomendacion(Evaluacion, Criterio, Prioridad, Accion) :-
       barrera(Evaluacion, Criterio, _, Prioridad),
       criterio(Criterio, _, _, _, Accion).

explicacion(Evaluacion, Criterio, Pregunta, no, Accion) :-
       barrera(Evaluacion, Criterio, _, _),
       criterio(Criterio, _, _, Pregunta, Accion).

areas_afectadas(Evaluacion, Areas) :-
       findall(Area, barrera(Evaluacion, _, Area, _), Repetidas),
       sort(Repetidas, Areas).

cantidad_barreras_area(Evaluacion, Area, Cantidad) :-
       setof(A, C^P^Q^R^criterio(C, A, P, Q, R), Areas),
       member(Area, Areas),
       findall(Criterio,
              barrera(Evaluacion, Criterio, Area, _), Barreras),
       length(Barreras, Cantidad).

area_con_varias_barreras(Evaluacion, Area) :-
       cantidad_barreras_area(Evaluacion, Area, Cantidad),
       Cantidad >= 2.

requiere_verificacion(Evaluacion, Criterio) :-
       criterio(Criterio, _, _, _, _),
       respuesta(Evaluacion, Criterio, desconocido).

sin_responder(Evaluacion, Criterio) :-
       criterio(Criterio, _, _, _, _),
       \+ respuesta(Evaluacion, Criterio, _).

pendiente(Evaluacion, Criterio) :-
       requiere_verificacion(Evaluacion, Criterio).

pendiente(Evaluacion, Criterio) :-
       sin_responder(Evaluacion, Criterio).

evaluacion_completa(Evaluacion) :-
       \+ pendiente(Evaluacion, _).

criterio_evaluado(Evaluacion, Criterio) :-
       criterio(Criterio, _, _, _, _),
       respuesta(Evaluacion, Criterio, Valor),
       memberchk(Valor, [si, no]).

resumen(Evaluacion, Favorables, Barreras, Pendientes, NoAplican) :-
       findall(C, condicion_favorable(Evaluacion, C), LF),
       findall(C, barrera(Evaluacion, C, _, _), LB),
       findall(C, pendiente(Evaluacion, C), LP),
       findall(C,
              (criterio(C, _, _, _, _),
                respuesta(Evaluacion, C, no_aplica)), LN),
       length(LF, Favorables),
       length(LB, Barreras),
       length(LP, Pendientes),
       length(LN, NoAplican).

estado_general(Evaluacion, Estado) :-
       ( barrera(Evaluacion, _, _, alta)
       -> Estado = requiere_atencion_prioritaria
       ; barrera(Evaluacion, _, _, _)
       -> Estado = requiere_mejoras
       ; pendiente(Evaluacion, _)
       -> Estado = informacion_insuficiente
       ; criterio_evaluado(Evaluacion, _)
       -> Estado = sin_barreras_reportadas
       ; Estado = sin_criterios_aplicables
       ).

orden_prioridad(alta, 1).

orden_prioridad(media, 2).

orden_prioridad(baja, 3).

recomendaciones_ordenadas(Evaluacion, Lista) :-
       findall(Orden-recomendacion(Criterio, Prioridad, Accion),
              (criterio(Criterio, _, _, _, _),
               recomendacion(Evaluacion, Criterio, Prioridad, Accion),
                orden_prioridad(Prioridad, Orden)), Resultados),
       keysort(Resultados, Lista).

registrar_respuesta(Evaluacion, Criterio, Valor) :-
       atom(Evaluacion),
       ground(Criterio-Valor),
       criterio(Criterio, _, _, _, _),
       memberchk(Valor, [si, no, desconocido, no_aplica]),
       (Valor == no_aplica -> motivo_no_aplica(Criterio, _) ; true),
       retractall(respuesta(Evaluacion, Criterio, _)),
       assertz(respuesta(Evaluacion, Criterio, Valor)).

ejemplo([
    teclado_completo-no,
    foco_visible-no,
    orden_foco-si,
    sin_trampas_teclado-si,
    contraste_legible-desconocido,
    informacion_sin_color-si,
    ampliacion_texto-si,
    redistribucion_contenido-si,
    alternativas_imagenes-no,
    subtitulos_videos-no_aplica,
    transcripcion_audios-no_aplica,
    alternativa_visual_video-no_aplica,
    etiquetas_campos-no,
    instrucciones_campos-si,
    errores_explicados-no,
    revision_envio-si,
    encabezados_estructurados-si,
    enlaces_descriptivos-si,
    instrucciones_claras-si,
    navegacion_consistente-si,
    tiempo_ajustable-no_aplica,
    control_movimiento-no_aplica,
    control_audio-no_aplica,
    alternativa_arrastre-no
]).

area(navegacion_teclado, 'Navegación con teclado').
area(presentacion_visual, 'Presentación visual').
area(contenido_multimedia, 'Imágenes y contenido multimedia').
area(formularios_evaluaciones, 'Formularios y evaluaciones').
area(organizacion_comprension, 'Organización y comprensión').
area(interaccion_control, 'Interacción y control').

motivo_no_aplica(alternativas_imagenes, 'No hay imágenes informativas').
motivo_no_aplica(subtitulos_videos, 'No hay videos con voz').
motivo_no_aplica(transcripcion_audios, 'No hay materiales de solo audio').
motivo_no_aplica(alternativa_visual_video, 'No hay información visual importante en videos').
motivo_no_aplica(etiquetas_campos, 'No hay campos de captura').
motivo_no_aplica(instrucciones_campos, 'No hay campos de captura').
motivo_no_aplica(errores_explicados, 'No hay acciones que produzcan errores de validación').
motivo_no_aplica(revision_envio, 'No hay una entrega definitiva').
motivo_no_aplica(encabezados_estructurados, 'No hay contenido organizado en secciones').
motivo_no_aplica(enlaces_descriptivos, 'No hay enlaces').
motivo_no_aplica(instrucciones_claras, 'No hay actividades').
motivo_no_aplica(navegacion_consistente, 'No hay menús o controles repetidos').
motivo_no_aplica(tiempo_ajustable, 'No hay límite de tiempo').
motivo_no_aplica(control_movimiento, 'No hay elementos animados o en movimiento').
motivo_no_aplica(control_audio, 'No hay audio automático').
motivo_no_aplica(alternativa_arrastre, 'No hay actividades de arrastre').

% Reglas 17–20: una definición compartida por la detección y la explicación.
regla_compuesta(17, interaccion_restringida, 'Interacción restringida',
    [teclado_completo, alternativa_arrastre]).
regla_compuesta(18, captura_y_correccion_dificiles, 'Captura y corrección difíciles',
    [etiquetas_campos, errores_explicados]).
regla_compuesta(19, informacion_visual_dificil, 'Información visual difícil',
    [contraste_legible, informacion_sin_color]).
regla_compuesta(20, entrega_dificil_de_revisar, 'Entrega difícil de revisar',
    [instrucciones_claras, revision_envio]).

problema_compuesto(Evaluacion, Problema) :-
    explicacion_compuesta(Evaluacion, Problema, _, _, _).

explicacion_compuesta(Evaluacion, Problema, Regla, Titulo, Evidencias) :-
    regla_compuesta(Regla, Problema, Titulo, Criterios),
    maplist(evidencia(Evaluacion), Criterios, Evidencias).

evidencia(Evaluacion, Criterio, _{criterio:Criterio, pregunta:Pregunta, valor:no}) :-
    explicacion(Evaluacion, Criterio, Pregunta, no, _).

limpiar_evaluacion(Evaluacion) :-
    atom(Evaluacion),
    retractall(respuesta(Evaluacion, _, _)).

cargar_ejemplo :- cargar_ejemplo(demo).
cargar_ejemplo(Evaluacion) :-
    limpiar_evaluacion(Evaluacion),
    ejemplo(Datos),
    forall(member(Criterio-Valor, Datos), registrar_respuesta(Evaluacion, Criterio, Valor)).
