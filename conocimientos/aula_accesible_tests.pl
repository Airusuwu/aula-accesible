:- encoding(utf8).
:- ensure_loaded(aula_accesible).

:- begin_tests(aula_accesible).

test(ejemplo, [setup(cargar_ejemplo), cleanup(limpiar_evaluacion(demo))]) :-
    resumen(demo, 11, 6, 1, 6),
    estado_general(demo, requiere_atencion_prioritaria),
    findall(P, problema_compuesto(demo, P), [interaccion_restringida, captura_y_correccion_dificiles]),
    explicacion_compuesta(demo, interaccion_restringida, 17, _, [Teclado, Arrastre]),
    assertion(Teclado.criterio == teclado_completo),
    assertion(Arrastre.criterio == alternativa_arrastre),
    assertion(Teclado.valor == no),
    assertion(Arrastre.valor == no), !.

test(correccion, [setup(cargar_ejemplo), cleanup(limpiar_evaluacion(demo))]) :-
    registrar_respuesta(demo, alternativa_arrastre, si),
    resumen(demo, 12, 5, 1, 6),
    findall(P, problema_compuesto(demo, P), [captura_y_correccion_dificiles]),
    estado_general(demo, requiere_atencion_prioritaria), !.

test(reglas_combinadas, [forall(member(P-C1-C2, [
    interaccion_restringida-teclado_completo-alternativa_arrastre,
    captura_y_correccion_dificiles-etiquetas_campos-errores_explicados,
    informacion_visual_dificil-contraste_legible-informacion_sin_color,
    entrega_dificil_de_revisar-instrucciones_claras-revision_envio
])), cleanup(limpiar_evaluacion(regla))]) :-
    registrar_respuesta(regla, C1, no),
    \+ problema_compuesto(regla, P),
    registrar_respuesta(regla, C2, no),
    findall(Problema, problema_compuesto(regla, Problema), [P]),
    registrar_respuesta(regla, C1, desconocido),
    \+ problema_compuesto(regla, P), !.

test(estados, [cleanup(limpiar_evaluacion(estados))]) :-
    estado_general(estados, informacion_insuficiente),
    forall(criterio(C, _, _, _, _), registrar_respuesta(estados, C, si)),
    estado_general(estados, sin_barreras_reportadas),
    registrar_respuesta(estados, orden_foco, no),
    estado_general(estados, requiere_mejoras),
    registrar_respuesta(estados, teclado_completo, no),
    estado_general(estados, requiere_atencion_prioritaria), !.

test(estado_sin_aplicables_de_la_regla_original, [cleanup(limpiar_evaluacion(total_no_aplica))]) :-
    % Datos sintéticos para la última rama de la regla. La app exige criterios generales aplicables.
    forall(criterio(C, _, _, _, _), assertz(user:respuesta(total_no_aplica, C, no_aplica))),
    estado_general(total_no_aplica, sin_criterios_aplicables), !.

test(orden_de_prioridad_y_criterio, [cleanup(limpiar_evaluacion(orden))]) :-
    registrar_respuesta(orden, orden_foco, no),
    registrar_respuesta(orden, foco_visible, no),
    registrar_respuesta(orden, teclado_completo, no),
    recomendaciones_ordenadas(orden, [
        1-recomendacion(teclado_completo, alta, _),
        1-recomendacion(foco_visible, alta, _),
        2-recomendacion(orden_foco, media, _)
    ]), !.

test(rechazar_no_aplica_general, [fail]) :- registrar_respuesta(invalida, foco_visible, no_aplica).

:- end_tests(aula_accesible).
