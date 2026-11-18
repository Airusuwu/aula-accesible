:- encoding(utf8).
:- use_module(library(http/json)).
:- ensure_loaded(aula_accesible).

% Un mensaje UTF-8 por proceso. Los datos de entrada nunca se ejecutan como código.
main :-
    set_stream(user_input, encoding(utf8)),
    set_stream(user_output, encoding(utf8)),
    catch(
        ( json_read_dict(user_input, Peticion, [value_string_as(atom)]),
          ( operacion(Peticion, Resultado) -> true ; throw(error(solicitud_invalida, _)) ),
          json_write_dict(user_output, Resultado), nl
        ),
        Error, (print_message(error, Error), halt(2))
    ).

operacion(P, _{criterios:Catalogo}) :-
    P.operacion == catalogo,
    findall(Id, criterio(Id, _, _, _, _), Ids),
    findall(Ficha, (nth1(Orden, Ids, Id), ficha(Id, Orden, Ficha)), Catalogo).
operacion(P, _{respuestas:Respuestas}) :-
    P.operacion == ejemplo,
    ejemplo(Datos),
    maplist(respuesta_ejemplo, Datos, Respuestas).
operacion(P, Diagnostico) :-
    P.operacion == evaluar,
    Id = P.evaluacion,
    atom(Id), Id \== '',
    Respuestas = P.respuestas,
    maplist(respuesta_valida, Respuestas, Criterios),
    sort(Criterios, Unicos), same_length(Criterios, Unicos),
    maplist(registrar(Id), Respuestas),
    diagnostico(Id, Diagnostico).

ficha(Id, Orden, _{id:Id, orden:Orden, area:Area, nombreArea:NombreArea,
    prioridad:Prioridad, pregunta:Pregunta, recomendacion:Accion, motivoNoAplica:Motivo}) :-
    criterio(Id, Area, Prioridad, Pregunta, Accion),
    area(Area, NombreArea),
    (motivo_no_aplica(Id, Motivo) -> true ; Motivo = '').

respuesta_ejemplo(Id-Valor, _{criterio:Id, valor:Valor, ubicacion:'', motivo:Motivo}) :-
    (Valor == no_aplica -> motivo_no_aplica(Id, Motivo) ; Motivo = '').

respuesta_valida(R, Id) :-
    Id = R.criterio,
    criterio(Id, _, _, _, _),
    memberchk(R.valor, [si, no, desconocido, no_aplica]),
    atom(R.ubicacion),
    ( R.valor == no_aplica -> motivo_no_aplica(Id, Motivo), R.motivo == Motivo
    ; R.motivo == ''
    ).

registrar(Evaluacion, R) :- registrar_respuesta(Evaluacion, R.criterio, R.valor).

diagnostico(Id, _{estado:Estado, conteos:Conteos, pendientes:Pendientes,
    recomendaciones:Recomendaciones, problemas:Problemas}) :-
    estado_general(Id, Estado),
    resumen(Id, F, B, P, N),
    Conteos = _{favorables:F, barreras:B, pendientes:P, noAplican:N},
    findall(_{criterio:C, motivo:Motivo},
        (criterio(C, _, _, _, _), motivo_pendiente(Id, C, Motivo)), Pendientes),
    recomendaciones_ordenadas(Id, Ordenadas),
    maplist(ficha_recomendacion, Ordenadas, Recomendaciones),
    findall(_{id:Problema, regla:Regla, titulo:Titulo, evidencias:Evidencias},
        explicacion_compuesta(Id, Problema, Regla, Titulo, Evidencias), Problemas).

motivo_pendiente(Id, C, desconocido) :- requiere_verificacion(Id, C).
motivo_pendiente(Id, C, omitido) :- sin_responder(Id, C).

ficha_recomendacion(_-recomendacion(C, Prioridad, Accion),
    _{criterio:C, prioridad:Prioridad, accion:Accion}).
