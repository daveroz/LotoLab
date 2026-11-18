package app.loto.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * Spanish for every text in the app, keyed by its English original (see {@link T}). Generated from the keys used
 * in the code; a unit test checks that none is missing and that each keeps the same placeholders.
 */
final class Es {
    private Es() {}

    static final Map<String, String> MAP = new HashMap<>(736);

    private static void p(String en, String es) {
        MAP.put(en, es);
    }

    static {
        p("1st",
                "1ª");
        p("2nd",
                "2ª");
        p("3rd",
                "3ª");
        p("4th",
                "4ª");
        p("%d (%s)",
                "%d (%s)");
        p("%d even / %d odd",
                "%d pares / %d impares");
        p("All different (ABCD)",
                "Todas distintas (ABCD)");
        p("One pair (AABC)",
                "Un par (AABC)");
        p("Two pairs (AABB)",
                "Dos pares (AABB)");
        p("Three of a kind (AAAB)",
                "Tercia (AAAB)");
        p("Four of a kind (AAAA)",
                "Cuatro iguales (AAAA)");
        p("Mirror numbers (ABBA, e.g. 1221)",
                "Números espejo (ABBA, p. ej. 1221)");
        p("Straight runs (1234, 8765)",
                "Escaleras (1234, 8765)");
        p("Four of a kind (0000–9999)",
                "Cuatro iguales (0000–9999)");
        p("Look like a date (DDMM, e.g. 2512)",
                "Parecen una fecha (DDMM, p. ej. 2512)");
        p("At least two consecutive numbers (e.g. 17·18)",
                "Al menos dos números seguidos (p. ej. 17·18)");
        p("All numbers 31 or lower (birthday picks)",
                "Todos los números de 31 o menos (cumpleaños)");
        p("never",
                "nunca");
        p("1 in %s",
                "1 entre %s");
        p("Full history (GitHub)",
                "Historial completo (GitHub)");
        p("%d new or completed draws, %d of %d files",
                "%d sorteos nuevos o completados, %d de %d archivos");
        p("no results on the page",
                "no hay resultados en la página");
        p("%d new · %s",
                "%d nuevos · %s");
        p("Morning",
                "Mañana");
        p("Afternoon",
                "Tarde");
        p("Evening / Night",
                "Noche");
        p("(holidays: Sunday time)",
                "(festivos: horario del domingo)");
        p("(no draw on public holidays)",
                "(no juega los festivos)");
        p("HISTORY",
                "HISTORIAL");
        p("%s plays %s",
                "%s juega %s");
        p("Every day",
                "Todos los días");
        p("No draws on %s in this selection.",
                "No hay sorteos los %s en esta selección.");
        p("No results stored for this selection.",
                "No hay resultados guardados para esta selección.");
        p("%s · %d",
                "%s · %d");
        p("Show %d more days",
                "Mostrar %d días más");
        p("The blue tile is the quinta (fifth digit), where it was published.",
                "La ficha azul es la quinta (quinta cifra), donde se publicó.");
        p("The red ball is the superbalota.",
                "La balota roja es la superbalota.");
        p("Audit: how ordinary each draw is, from 100 (completely typical) down; unusual draws are expected now and then (about 3 in 100 fair draws score below 10).",
                "Auditoría: qué tan común es cada sorteo, de 100 (completamente típico) hacia abajo; es normal que de vez en cuando salga uno inusual (cerca de 3 de cada 100 sorteos justos quedan por debajo de 10).");
        p("HISTORY STORED",
                "HISTORIAL GUARDADO");
        p("%s draws",
                "%s sorteos");
        p("None yet.",
                "Todavía ninguno.");
        p("missing the result of %s",
                "falta el resultado del %s");
        p("Not refreshed yet on this phone.",
                "Aún no se ha actualizado en este teléfono.");
        p("Last refresh: %s.",
                "Última actualización: %s.");
        p("Bundled with the app, completed from the repository's copy (updated twice a day) and from the sites below each time the app refreshes. Results are only ever added, never removed.",
                "Viene con la app, se completa con la copia del repositorio (actualizada dos veces al día) y con los sitios de abajo cada vez que la app se actualiza. Los resultados solo se agregan, nunca se borran.");
        p("LAST REFRESH",
                "ÚLTIMA ACTUALIZACIÓN");
        p("Lottery: %s. Swipe left or right to change.",
                "Lotería: %s. Desliza a la izquierda o a la derecha para cambiar.");
        p("Summary",
                "Resumen");
        p("History",
                "Historial");
        p("Stats",
                "Estadísticas");
        p("Picks",
                "Jugadas");
        p("Odds",
                "Probabilidades");
        p("30 days",
                "30 días");
        p("90 days",
                "90 días");
        p("1 year",
                "1 año");
        p("All",
                "Todo");
        p("Fetch the newest results",
                "Traer los resultados más recientes");
        p("Chance · 3 draws a day",
                "Chance · 3 sorteos al día");
        p("5 of 43 + superbalota",
                "5 de 43 + superbalota");
        p("5 of 39 · 4 draws a week",
                "5 de 39 · 4 sorteos por semana");
        p("Loading the draw history…",
                "Cargando el historial de sorteos…");
        p("Fetching the newest results…",
                "Trayendo los resultados más recientes…");
        p("not fetched yet",
                "sin actualizar todavía");
        p("updated %s, %s",
                "actualizado el %s, %s");
        p("%s draws stored · %s",
                "%s sorteos guardados · %s");
        p("%d source(s) unreachable (see History)",
                "%d fuente(s) sin respuesta (ver Historial)");
        p("Working it out…",
                "Calculando…");
        p("SOMETHING WENT WRONG",
                "ALGO SALIÓ MAL");
        p("This view couldn't be worked out: %s",
                "No se pudo calcular esta vista: %s");
        p("NO DRAWS",
                "SIN SORTEOS");
        p("There are no stored results for %s yet. Tap ↻ to fetch the newest results.",
                "Aún no hay resultados guardados de %s. Toca ↻ para traer los más recientes.");
        p("%s · %s draws",
                "%s · %s sorteos");
        p("Results",
                "Resultados");
        p("A notification when a result you follow is announced",
                "Una notificación cuando se anuncia un resultado que sigues");
        p("quinta",
                "quinta");
        p("%s · %s draw (%d/100)",
                "%s · sorteo %s (%d/100)");
        p("4 cifras — all four digits in order",
                "4 cifras — las cuatro cifras en orden");
        p("the main chance bet",
                "la apuesta principal del chance");
        p("3 cifras — last three digits",
                "3 cifras — las tres últimas");
        p("2 cifras — last two digits",
                "2 cifras — las dos últimas");
        p("1 cifra — last digit",
                "1 cifra — la última");
        p("Combinado — four different digits, any order",
                "Combinado — cuatro cifras distintas, en cualquier orden");
        p("24 orders of e.g. 1234",
                "24 órdenes de p. ej. 1234");
        p("Combinado — a pair (e.g. 1123), any order",
                "Combinado — un par (p. ej. 1123), en cualquier orden");
        p("12 orders",
                "12 órdenes");
        p("Combinado — two pairs (e.g. 1122), any order",
                "Combinado — dos pares (p. ej. 1122), en cualquier orden");
        p("6 orders",
                "6 órdenes");
        p("Combinado — three alike (e.g. 1112), any order",
                "Combinado — tres iguales (p. ej. 1112), en cualquier orden");
        p("4 orders",
                "4 órdenes");
        p("4 cifras + quinta (five digits)",
                "4 cifras + quinta (cinco cifras)");
        p("where the quinta is offered",
                "donde se ofrece la quinta");
        p("%d + %s (jackpot)",
                "%d + %s (premio mayor)");
        p("every number and the superbalota",
                "todos los números y la superbalota");
        p("%d + %s",
                "%d + %s");
        p("the superbalota alone",
                "solo la superbalota");
        p("%d numbers",
                "%d números");
        p("%d numbers (jackpot)",
                "%d números (premio mayor)");
        p("Type a 4-digit number, like 2820.",
                "Escribe un número de 4 cifras, como 2820.");
        p("Type %d different numbers from 1 to %d, then + and the superbalota (1–%d).",
                "Escribe %d números distintos del 1 al %d, luego + y la superbalota (1–%d).");
        p("Type %d different numbers from 1 to %d.",
                "Escribe %d números distintos del 1 al %d.");
        p("Numbers must be different and between 1 and %d.",
                "Los números deben ser distintos y estar entre 1 y %d.");
        p("The superbalota must be between 1 and %d.",
                "La superbalota debe estar entre 1 y %d.");
        p("CHANCE OF THE TOP PRIZE · %s",
                "PROBABILIDAD DEL PREMIO MAYOR · %s");
        p("theoretical",
                "teórico");
        p("%s per bet · 4 cifras: the four digits in order",
                "%s por apuesta · 4 cifras: las cuatro cifras en orden");
        p("%s per ticket · %d numbers from 1–%d plus the superbalota (1–%d)",
                "%s por apuesta · %d números del 1 al %d más la superbalota (1–%d)");
        p("%s per ticket · %d numbers from 1–%d",
                "%s por apuesta · %d números del 1 al %d");
        p("All %s numbers from 0000 to 9999 have exactly this probability in every draw: 0000 is as likely as 4817, and a number that came out yesterday or is \"due\" is no different.",
                "Los %s números de 0000 a 9999 tienen exactamente esta probabilidad en cada sorteo: 0000 es tan probable como 4817, y un número que salió ayer o que \"ya debe salir\" no es distinto.");
        p("All %s combinations have exactly this probability in every draw: 01 02 03 04 05 is as likely as any other. What differs is how many people play them, which matters only if a jackpot is shared.",
                "Las %s combinaciones tienen exactamente esta probabilidad en cada sorteo: 01 02 03 04 05 es tan probable como cualquier otra. Lo que cambia es cuánta gente las juega, y eso solo importa si un premio mayor se comparte.");
        p("Revancha is a second draw for the same ticket with the same odds: playing both gives two independent chances, about 1 in 7,700,784 of at least one jackpot.",
                "La Revancha es un segundo sorteo para el mismo tiquete con las mismas probabilidades: jugar ambos da dos oportunidades independientes, cerca de 1 entre 7.700.784 de ganar al menos un premio mayor.");
        p("PLAYING EVERY DRAW FOR A YEAR",
                "JUGAR TODOS LOS SORTEOS DURANTE UN AÑO");
        p("Draws in a year",
                "Sorteos en un año");
        p("Top prize at least once",
                "Premio mayor al menos una vez");
        p("With one ticket per draw. On average that's one win every %s years.",
                "Con una apuesta por sorteo. En promedio, una victoria cada %s años.");
        p("La pata at least once",
                "La pata al menos una vez");
        p("Last two digits, one bet per draw.",
                "Las dos últimas cifras, una apuesta por sorteo.");
        p("Any prize at least once",
                "Algún premio al menos una vez");
        p("EL DORADO PRIZE PLAN",
                "PLAN DE PREMIOS DE EL DORADO");
        p("official rules",
                "reglas oficiales");
        p("What a bet paid at %s wins, by category. Probability and average return are per bet.",
                "Lo que gana una apuesta de %s, por categoría. La probabilidad y el retorno promedio son por apuesta.");
        p("(all digits different)",
                "(todas las cifras distintas)");
        p("%s · × %s · %s Average return %s.",
                "%s · × %s · %s Retorno promedio %s.");
        p("How the prize is worked out",
                "Cómo se calcula el premio");
        p("Paid (IVA included)",
                "Pagado (IVA incluido)");
        p("Bet value before IVA (19%)",
                "Valor apostado sin IVA (19%)");
        p("× 4,500 (4 cifras)",
                "× 4.500 (4 cifras)");
        p("Gross prize",
                "Premio bruto");
        p("Withholding 20%",
                "Retención 20%");
        p("Only on prizes above 48 UVT = %s (UVT %d: %s).",
                "Solo en premios de más de 48 UVT = %s (UVT %d: %s).");
        p("Net prize",
                "Premio neto");
        p("Only the highest category hit is paid. The 4, 3, 2 and 1-digit bets look at the last digits of the result: for 4817, la pata is 17 and the last three are 817. Sources: Decreto 1068 de 2015 (chance prize plan) as modified by Decreto 1390 de 2024; Estatuto Tributario on withholding; DIAN for the UVT.",
                "Solo se paga la categoría más alta acertada. Las apuestas de 4, 3, 2 y 1 cifra miran las últimas cifras del resultado: en 4817, la pata es 17 y las tres últimas son 817. Fuentes: Decreto 1068 de 2015 (plan de premios del chance), modificado por el Decreto 1390 de 2024; Estatuto Tributario para la retención; DIAN para la UVT.");
        p("EVERY PRIZE TIER",
                "TODOS LOS NIVELES DE PREMIO");
        p("Ticket %s (IVA included).",
                "Tiquete %s (IVA incluido).");
        p("Ticket %s; Revancha %s more (IVA included).",
                "Tiquete %s; Revancha %s más (IVA incluido).");
        p("Chance of winning any prize: %s. Prize amounts come from the pool of each draw and aren't fixed, so they aren't estimated here.",
                "Probabilidad de ganar algún premio: %s. Los montos salen del acumulado de cada sorteo y no son fijos, así que aquí no se estiman.");
        p("Any prize",
                "Algún premio");
        p("Win back the cost",
                "Recuperar lo apostado");
        p("Top prize",
                "Premio mayor");
        p("BETTING PACK",
                "PAQUETE DE APUESTAS");
        p("The fewest bets, across the next draws of every lottery, that reach the chance you choose.",
                "La menor cantidad de apuestas, entre los próximos sorteos de todas las loterías, que alcanza la probabilidad que elijas.");
        p("Goal",
                "Objetivo");
        p("Chance to reach",
                "Probabilidad buscada");
        p("Each chance bet",
                "Cada apuesta de chance");
        p("No upcoming draws found in the next week.",
                "No hay sorteos en la próxima semana.");
        p("%s: %s · %d bets · total cost",
                "%s: %s · %d apuestas · costo total");
        p("%s isn't reachable for this goal: more bets cost more than the fixed prizes return, so this is the highest chance of winning back the cost.",
                "%s no se puede alcanzar con este objetivo: más apuestas cuestan más de lo que devuelven los premios fijos, así que esta es la mayor probabilidad de recuperar lo apostado.");
        p("%s isn't reachable within three chance draws and the next lotto draws; this is the closest.",
                "%s no se alcanza con tres sorteos de chance y los próximos sorteos de lotería; esto es lo más cercano.");
        p("%d × %s at %s · each %s · this draw %s",
                "%d × %s a %s · cada una %s · este sorteo %s");
        p("1 ticket · %s %s",
                "1 tiquete · %s %s");
        p("jackpot",
                "premio mayor");
        p("any prize",
                "algún premio");
        p("+ %d more different endings",
                "+ %d terminaciones distintas más");
        p("A winning bet pays %s net.",
                "Una apuesta ganadora paga %s netos.");
        p("Best single bet",
                "Mejor apuesta individual");
        p("Whole pack",
                "Todo el paquete");
        p("Total cost",
                "Costo total");
        p("Expected chance prizes",
                "Premios de chance esperados");
        p("Expected prizes",
                "Premios esperados");
        p("Lotto prizes depend on the pool and aren't included.",
                "Los premios de lotería dependen del acumulado y no se incluyen.");
        p("Expected result",
                "Resultado esperado");
        p("On average the pack returns %s of its cost.",
                "En promedio el paquete devuelve el %s de su costo.");
        p("How the pack is worked out",
                "Cómo se calcula el paquete");
        p("Bets on different numbers in the same draw can't both win, so their chances add (3 different patas: 3 in 100). Different draws are independent, so the pack misses only if every part misses: 1 − (1 − p₁)(1 − p₂)…. One bet type per draw keeps that exact, without counting any overlap twice. Revancha uses the same numbers as Baloto but is a separate draw. Chance prizes follow the official plan (IVA and withholding included); lotto prizes depend on the pool, so lotto tickets don't count towards winning back the cost.",
                "Apuestas a números distintos en el mismo sorteo no pueden ganar a la vez, así que sus probabilidades se suman (3 patas distintas: 3 entre 100). Sorteos distintos son independientes, así que el paquete pierde solo si fallan todas sus partes: 1 − (1 − p₁)(1 − p₂)…. Un solo tipo de apuesta por sorteo mantiene el cálculo exacto, sin contar dos veces ninguna coincidencia. La Revancha usa los mismos números del Baloto pero es otro sorteo. Los premios del chance siguen el plan oficial (con IVA y retención); los de lotería dependen del acumulado, así que esos tiquetes no cuentan para recuperar lo apostado.");
        p("The numbers come from each draw's recommended model, only so the list is concrete: every number has the same chance. No pack changes the odds of a single bet, nothing is guaranteed, and every pack loses money on average.",
                "Los números vienen del modelo recomendado para cada sorteo, solo para que la lista sea concreta: todos tienen la misma probabilidad. Ningún paquete cambia la probabilidad de una apuesta, nada está garantizado y todo paquete pierde dinero en promedio.");
        p("All four digits",
                "Las cuatro cifras");
        p("Last three digits",
                "Las tres últimas cifras");
        p("La pata (last two)",
                "La pata (dos últimas)");
        p("La uña (last digit)",
                "La uña (última cifra)");
        p("No hit (%s)",
                "Sin acierto (%s)");
        p("%d of %d",
                "%d de %d");
        p("superbalota",
                "superbalota");
        p("EXPLORE A MODEL",
                "EXPLORA UN MODELO");
        p("model ranking",
                "ranking de modelos");
        p("Every model's own top pick, its reasons and alternatives.",
                "La mejor opción de cada modelo, sus razones y alternativas.");
        p("Highest estimated probability under this model",
                "La probabilidad estimada más alta según este modelo");
        p("Suggested combination",
                "Combinación sugerida");
        p("Why these numbers",
                "Por qué estos números");
        p("%d more from this model",
                "%d más de este modelo");
        p("New suggestions",
                "Nuevas sugerencias");
        p("Backtest",
                "Prueba histórica");
        p("THEORY VS. HISTORY",
                "TEORÍA VS. HISTORIA");
        p("Every combination has a theoretical probability of %s in every draw, fixed by the rules. The models only reweight numbers by past results: in a fair draw no model beats the theoretical odds, and a recommendation that changes from day to day doesn't make any number more likely. Every ticket's expected return is negative.",
                "Cada combinación tiene una probabilidad teórica de %s en cada sorteo, fijada por las reglas. Los modelos solo reponderan los números según resultados pasados: en un sorteo justo ningún modelo supera la probabilidad teórica, y que la recomendación cambie de un día a otro no hace más probable ningún número. El retorno esperado de todo tiquete es negativo.");
        p("MODEL RANKING · %s",
                "RANKING DE MODELOS · %s");
        p("MODEL RANKING",
                "RANKING DE MODELOS");
        p("predictive evidence",
                "evidencia predictiva");
        p("Each model forecasts every draw from the earlier ones only. Score: how much more probability its forecasts gave the actual results than a fair guess, over the last %d draws (above 0 = better).",
                "Cada modelo pronostica cada sorteo solo con los anteriores. Puntaje: cuánta más probabilidad dieron sus pronósticos a los resultados reales que una elección justa, en los últimos %d sorteos (más de 0 = mejor).");
        p("Model",
                "Modelo");
        p("Score",
                "Puntaje");
        p("CI (95%)",
                "IC (95%)");
        p("Too few draws to test for an advantage.",
                "Muy pocos sorteos para probar si hay ventaja.");
        p("The top model beats chance (Monte Carlo p = %s, allowing for picking the best of %d).",
                "El mejor modelo supera al azar (Monte Carlo p = %s, teniendo en cuenta que se elige el mejor de %d).");
        p("No real advantage: in simulated fair draws the best of %d models scores this well %s of the time (p = %s). The ranking only decides which candidate is shown.",
                "Sin ventaja real: en sorteos justos simulados el mejor de %d modelos obtiene este puntaje el %s de las veces (p = %s). El ranking solo decide qué candidato se muestra.");
        p("Each model's candidate today",
                "El candidato de cada modelo hoy");
        p("Model's estimate %s · theoretical %s",
                "Estimación del modelo %s · teórica %s");
        p("How the ranking works",
                "Cómo funciona el ranking");
        p("The log score is a proper scoring rule: a model can only score above 0 on average by putting more probability on what actually comes out. The Ensemble mixes the four forecasters by Bayesian model averaging. The Monte Carlo test runs the same models on 99 simulated fair histories of the same length, so picking the best of several doesn't pass for skill. Ties are broken with a seed made from the lottery and the draw date, so the same data always gives the same recommendation.",
                "El puntaje logarítmico es una regla de puntuación propia: un modelo solo puede superar 0 en promedio si le da más probabilidad a lo que de verdad sale. El Ensamble combina los cuatro pronosticadores con promedio bayesiano de modelos. La prueba de Monte Carlo corre los mismos modelos sobre 99 historiales justos simulados del mismo largo, para que elegir el mejor de varios no pase por habilidad. Los empates se resuelven con una semilla hecha de la lotería y la fecha del sorteo, así que los mismos datos siempre dan la misma recomendación.");
        p("TRACK RECORD · %s",
                "HISTORIAL DE ACIERTOS · %s");
        p("TRACK RECORD",
                "HISTORIAL DE ACIERTOS");
        p("The daily recommendation replayed: before every past draw, what it would have chosen from the earlier results only, against what came out. Expected = what random picks average.",
                "La recomendación diaria repetida en el pasado: antes de cada sorteo, lo que habría elegido solo con los resultados anteriores, frente a lo que salió. Esperado = el promedio de elegir al azar.");
        p("Pata",
                "Pata");
        p("2+ hit",
                "2+ aciertos");
        p("Period",
                "Período");
        p("Draws",
                "Sorteos");
        p("Expected",
                "Esperado");
        p("All four digits %d times (%s expected), last three %d (%s), la pata %d (%s), la uña %d (%s). Pata hit rate %s, 95%% CI %s–%s; random: 1%%.",
                "Las cuatro cifras %d veces (%s esperadas), las tres últimas %d (%s), la pata %d (%s), la uña %d (%s). Tasa de acierto de la pata %s, IC 95%% %s–%s; al azar: 1%%.");
        p("%s numbers matched in all (%s expected); 2 or more in %d draws (%s expected), 3 or more in %d. 2+ rate %s, 95%% CI %s–%s; random: %s.",
                "%s números acertados en total (%s esperados); 2 o más en %d sorteos (%s esperados), 3 o más en %d. Tasa de 2+ %s, IC 95%% %s–%s; al azar: %s.");
        p("Superbalota right %d of %d (%s expected).",
                "Superbalota acertada %d de %d (%s esperadas).");
        p("More hits than chance over the whole history. With several periods and models compared, confirm it holds in new draws.",
                "Más aciertos que el azar en todo el historial. Como se comparan varios períodos y modelos, confirma que se mantenga en sorteos nuevos.");
        p("No better than random selection: the hits are what chance alone gives.",
                "No es mejor que elegir al azar: los aciertos son los que da el azar.");
        p("By model",
                "Por modelo");
        p("With 5 models and 4 periods, a p below 0.05 somewhere is expected by chance about once in a while; only a result that keeps holding in new draws would point to a real edge.",
                "Con 5 modelos y 4 períodos, es normal que por azar aparezca de vez en cuando una p menor que 0,05; solo un resultado que se mantenga en sorteos nuevos indicaría una ventaja real.");
        p("RECOMMENDATIONS SHOWN · %s",
                "RECOMENDACIONES MOSTRADAS · %s");
        p("The log starts with today's recommendation: every one the app shows is kept here with its result.",
                "El registro empieza con la recomendación de hoy: cada una que muestra la app se guarda aquí con su resultado.");
        p("%d settled: %d with all four digits, %d more with at least la pata.",
                "%d con resultado: %d con las cuatro cifras, %d más con al menos la pata.");
        p("%d settled: %d jackpots, %d more with 2 or more numbers.",
                "%d con resultado: %d premios mayores, %d más con 2 o más números.");
        p("Draw of %s",
                "Sorteo del %s");
        p("generated %s",
                "generada %s");
        p("Waiting for the result",
                "Esperando el resultado");
        p("Result: %s",
                "Resultado: %s");
        p("Theoretical probability",
                "Probabilidad teórica");
        p("Model's estimate",
                "Estimación del modelo");
        p("Same as theory: this model treats every combination alike.",
                "Igual a la teoría: este modelo trata todas las combinaciones por igual.");
        p("%.2f× the theoretical, if the model's assumption were true.",
                "%.2f× la teórica, si el supuesto del modelo fuera cierto.");
        p("Hot numbers",
                "Números calientes");
        p("Recent form",
                "Forma reciente");
        p("Overdue",
                "Atrasados");
        p("Bayesian (fair-adjusted)",
                "Bayesiano (ajustado a lo justo)");
        p("Ensemble (performance-weighted)",
                "Ensamble (ponderado por desempeño)");
        p("Typical shape",
                "Forma típica");
        p("Least shared",
                "Menos compartida");
        p("Quick pick",
                "Al azar");
        p("Bayesian",
                "Bayesiano");
        p("Ensemble",
                "Ensamble");
        p("Favours the numbers drawn most often in the period.",
                "Favorece los números que más han salido en el período.");
        p("Like hot numbers, but recent draws count more (weights halve every few dozen draws).",
                "Como los números calientes, pero los sorteos recientes pesan más (el peso se reduce a la mitad cada pocas docenas de sorteos).");
        p("Favours the numbers that haven't come up for longest. In a fair draw a number is never \"due\": this is the gambler's fallacy, shown for comparison.",
                "Favorece los números que llevan más tiempo sin salir. En un sorteo justo ningún número \"ya debe salir\": es la falacia del jugador, que se muestra para comparar.");
        p("Starts from a fair draw and moves away from it only as far as the data's real variation supports. If frequencies vary no more than chance, every number stays equal.",
                "Parte de un sorteo justo y se aleja de él solo tanto como lo justifica la variación real de los datos. Si las frecuencias no varían más que por azar, todos los números quedan iguales.");
        p("Mixes the four forecasting models, each weighted by how well it predicted the last 200 draws (Bayesian model averaging on the log score). It leans on whichever model works, if any does.",
                "Combina los cuatro modelos de pronóstico, cada uno ponderado según qué tan bien predijo los últimos 200 sorteos (promedio bayesiano de modelos sobre el puntaje logarítmico). Se apoya en el modelo que funcione, si alguno funciona.");
        p("A random combination with the shape most draws have: usual even/odd split, sum in the middle half, no long runs. Typical shapes are common only because there are more of them.",
                "Una combinación al azar con la forma que tienen la mayoría de los sorteos: el reparto par/impar usual, la suma en la mitad central, sin series largas. Las formas típicas son comunes solo porque hay más combinaciones con esa forma.");
        p("Avoids what most people play (birthdays 1–31, dates, patterns, sequences). Same odds, but if it wins, the prize is less likely to be split.",
                "Evita lo que juega la mayoría (cumpleaños del 1 al 31, fechas, patrones, secuencias). Las mismas probabilidades, pero si gana es menos probable que el premio se reparta.");
        p("Uniformly random, like the terminal's quick pick. The benchmark the others are measured against.",
                "Aleatoria uniforme, como la jugada al azar de la terminal. La referencia con la que se miden los demás.");
        p("Every combination has exactly the same chance; this is what the other models are compared with.",
                "Todas las combinaciones tienen exactamente la misma probabilidad; contra esto se comparan los demás modelos.");
        p("Doesn't change your odds: each typical-looking combination is exactly as likely as any other single combination.",
                "No cambia tus probabilidades: cada combinación de aspecto típico es exactamente tan probable como cualquier otra combinación.");
        p("Doesn't change your odds of winning. It only changes how many people you might share a jackpot with.",
                "No cambia tus probabilidades de ganar. Solo cambia con cuántas personas podrías compartir un premio mayor.");
        p("Past draws don't make a number due. If the draw is fair, this model can't beat chance; see the backtest.",
                "Los sorteos pasados no hacen que un número \"ya deba salir\". Si el sorteo es justo, este modelo no puede superar al azar; mira la prueba hacia atrás.");
        p("Not enough draws to judge.",
                "No hay suficientes sorteos para juzgar.");
        p("The frequency differences this model uses are within what chance produces (chi-square p = %s). Expect its picks to do no better than random.",
                "Las diferencias de frecuencia que usa este modelo están dentro de lo que produce el azar (chi-cuadrado p = %s). No esperes que sus jugadas rindan mejor que al azar.");
        p("Frequencies vary more than chance usually allows (p = %s). That could be a real bias or a fluke; the backtest shows whether it actually helped predict.",
                "Las frecuencias varían más de lo que el azar suele permitir (p = %s). Podría ser un sesgo real o una casualidad; la prueba hacia atrás muestra si de verdad ayudó a predecir.");
        p("%s digit %d: %d times in the last %d draws (%s expected).",
                "%s cifra, %d: %d veces en los últimos %d sorteos (%s esperadas).");
        p("%s digit %d: not seen there for %d draws (typical wait 10).",
                "%s cifra, %d: no ha salido ahí en %d sorteos (espera típica: 10).");
        p("%s digit %d: estimated %s chance (fair: 10%%).",
                "%s cifra, %d: probabilidad estimada %s (justa: 10%%).");
        p("%s digit %d: %s of %d draws (%s; fair 10%%), z %s.",
                "%s cifra, %d: %s de %d sorteos (%s; justo 10%%), z %s.");
        p("%02d: drawn %s times (%s expected, z %s), last seen %d draws ago.",
                "%02d: salió %s veces (%s esperadas, z %s); última vez hace %d sorteos.");
        p("%02d: %d times in the last %d draws (%s expected).",
                "%02d: %d veces en los últimos %d sorteos (%s esperadas).");
        p("%02d: not drawn for %d draws (typical wait %s).",
                "%02d: no ha salido en %d sorteos (espera típica: %s).");
        p("%02d: estimated %s chance per draw (fair: %s).",
                "%02d: probabilidad estimada %s por sorteo (justa: %s).");
        p("%s %02d: %s times (%s expected), last seen %d draws ago.",
                "%s %02d: %s veces (%s esperadas); última vez hace %d sorteos.");
        p("%d of the numbers are above 31, which birthday players can't choose.",
                "%d de los números son mayores que 31, que quienes juegan cumpleaños no pueden elegir.");
        p("No sequence, no arithmetic pattern, at most two month numbers (1–12).",
                "Sin secuencias, sin patrón aritmético, como mucho dos números de mes (1–12).");
        p("Not a date, no repeated digits, no sequence or mirror, doesn't start with 0–2 like most years and dates.",
                "No es una fecha, no repite cifras, no es secuencia ni espejo, y no empieza por 0–2 como la mayoría de años y fechas.");
        p("Drawn uniformly at random, like a quick pick.",
                "Elegida de forma aleatoria uniforme, como una jugada al azar.");
        p("Mix: %s",
                "Mezcla: %s");
        p("Shape: %d even / %d odd (%s of fair draws) · sum %d (half of fair draws: %d–%d)",
                "Forma: %d pares / %d impares (%s de los sorteos justos) · suma %d (la mitad de los sorteos justos: %d–%d)");
        p("consecutive pairs: %d",
                "pares seguidos: %d");
        p("Not enough draws to replay.",
                "No hay suficientes sorteos para repetir.");
        p("Replaying the last %d draws, letting the model see only earlier results: its pick matched %.2f digits in the right place per draw on average; pure chance gives %.2f.",
                "Repitiendo los últimos %d sorteos, dejando que el modelo vea solo los resultados anteriores: su jugada acertó en promedio %.2f cifras en el lugar correcto por sorteo; el puro azar da %.2f.");
        p("Replaying the last %d draws, letting the model see only earlier results: its pick matched %.2f numbers per draw on average; pure chance gives %.2f.",
                "Repitiendo los últimos %d sorteos, dejando que el modelo vea solo los resultados anteriores: su jugada acertó en promedio %.2f números por sorteo; el puro azar da %.2f.");
        p("That's better than chance (p = %s), though with several models tried one doing well by luck is expected now and then.",
                "Eso es mejor que el azar (p = %s), aunque al probar varios modelos es de esperar que alguno salga bien por suerte de vez en cuando.");
        p("No better than chance (p = %s).",
                "No es mejor que el azar (p = %s).");
        p("Last two digits right: %d times (%s expected).",
                "Últimas dos cifras acertadas: %d veces (%s esperadas).");
        p("4 cifras",
                "4 cifras");
        p("3 cifras",
                "3 cifras");
        p("Pata (2 cifras)",
                "Pata (2 cifras)");
        p("Uña (1 cifra)",
                "Uña (1 cifra)");
        p("4 cifras combinado",
                "4 cifras combinado");
        p("3 cifras combinado",
                "3 cifras combinado");
        p("Uña",
                "Uña");
        p("All four digits, in order.",
                "Las cuatro cifras, en orden.");
        p("The last three digits, in order.",
                "Las tres últimas cifras, en orden.");
        p("The last two digits, in order (la pata).",
                "Las dos últimas cifras, en orden (la pata).");
        p("The last digit (la uña).",
                "La última cifra (la uña).");
        p("The four digits in any order.",
                "Las cuatro cifras en cualquier orden.");
        p("The last three digits in any order.",
                "Las tres últimas cifras en cualquier orden.");
        p("Sum %d is far from the middle (half of fair draws total %d–%d).",
                "La suma %d está lejos del centro (la mitad de los sorteos justos suma %d–%d).");
        p("%d of %d digits are even (happens in %s of fair draws).",
                "%d de %d cifras son pares (pasa en el %s de los sorteos justos).");
        p("%d of %d numbers are even (happens in %s of fair draws).",
                "%d de %d números son pares (pasa en el %s de los sorteos justos).");
        p("%s: %s of fair draws.",
                "%s: el %s de los sorteos justos.");
        p("A straight run of digits: 14 of the 10,000 numbers are like that.",
                "Una escalera de cifras: 14 de los 10.000 números son así.");
        p("Same last two digits as the previous %s draw (1 in 100).",
                "Las mismas dos últimas cifras que el sorteo anterior de %s (1 entre 100).");
        p("%d consecutive numbers in a row (%s of fair draws).",
                "%d números seguidos (el %s de los sorteos justos).");
        p("%d numbers repeated from the previous draw (%s of fair draws).",
                "%d números repetidos del sorteo anterior (el %s de los sorteos justos).");
        p("Nothing out of the ordinary.",
                "Nada fuera de lo común.");
        p("Ordinary",
                "Ordinario");
        p("A little unusual",
                "Algo inusual");
        p("Unusual",
                "Inusual");
        p("Very unusual",
                "Muy inusual");
        p("HOT & COLD DIGITS",
                "CIFRAS CALIENTES Y FRÍAS");
        p("HOT & COLD NUMBERS",
                "NÚMEROS CALIENTES Y FRÍOS");
        p("descriptive",
                "descriptivo");
        p("How often each digit came up in each position. A fair draw gives every cell %s; warm cells came up more, cool ones less.",
                "Cuántas veces salió cada cifra en cada posición. Un sorteo justo da %s en cada casilla; las cálidas salieron más, las frías menos.");
        p("Times each number was drawn; a fair draw gives each %s.",
                "Veces que salió cada número; un sorteo justo da %s a cada uno.");
        p("Most frequent",
                "Los más frecuentes");
        p("Least frequent",
                "Los menos frecuentes");
        p("Overall chi-square p = %s: %s",
                "Chi cuadrado global p = %s: %s");
        p("the spread is what fair draws produce.",
                "la dispersión es la que producen los sorteos justos.");
        p("more spread than fair draws usually produce.",
                "más dispersión de la que suelen producir los sorteos justos.");
        p("Longest absent and the most extreme value",
                "Las más ausentes y el valor más extremo");
        p("%s  draws  (a value waits %s draws on average)",
                "%s  sorteos  (un valor espera %s sorteos en promedio)");
        p("The single most extreme value, allowing for the %d values compared: p = %s. Being absent for a long time doesn't make a value more likely next.",
                "El valor más extremo, teniendo en cuenta los %d valores comparados: p = %s. Llevar mucho tiempo sin salir no hace más probable que salga.");
        p("TWO-DIGIT ENDINGS (LA PATA)",
                "TERMINACIONES DE DOS CIFRAS (LA PATA)");
        p("Each of the 100 endings is expected %s times.",
                "Se espera que cada una de las 100 terminaciones salga %s veces.");
        p("%d more",
                "%d más");
        p("Most frequent ending, allowing for 100 endings: p = %s.",
                "La terminación más frecuente, teniendo en cuenta las 100: p = %s.");
        p("%d draws with a %s. Fair: %s each.",
                "%d sorteos con %s. Justo: %s cada una.");
        p("Chi-square p = %s.",
                "Chi-cuadrado p = %s.");
        p("EVEN & ODD",
                "PARES E IMPARES");
        p("%s of all digits were even and %s odd (fair: %s each). Chi-square p = %s.",
                "El %s de las cifras fueron pares y el %s impares (justo: %s cada uno). Chi cuadrado p = %s.");
        p("%s of all numbers were even and %s odd (fair: %s even). Chi-square p = %s.",
                "El %s de los números fueron pares y el %s impares (justo: %s pares). Chi cuadrado p = %s.");
        p("Draw by draw counts",
                "Conteo por sorteo");
        p("%s expected",
                "%s esperados");
        p("SUM OF THE NUMBERS",
                "SUMA DE LOS NÚMEROS");
        p("Average %.1f (fair %.1f), spread %.1f (fair %.1f). Half of all fair draws total %d–%d. Chi-square p = %s.",
                "Promedio %.1f (justo %.1f), dispersión %.1f (justo %.1f). La mitad de los sorteos justos suma %d–%d. Chi-cuadrado p = %s.");
        p("Not enough data",
                "No hay suficientes datos");
        p("Very unlikely by chance",
                "Muy improbable por azar");
        p("Unusual (p < 0.05)",
                "Inusual (p < 0,05)");
        p("Within chance",
                "Dentro del azar");
        p("Jackpot (5 + superbalota)",
                "Premio mayor (5 + superbalota)");
        p("%d + superbalota",
                "%d + superbalota");
        p("Jackpot (5 numbers)",
                "Premio mayor (5 números)");
        p("in %d min",
                "en %d min");
        p("in %d h %d min",
                "en %d h %d min");
        p("in %d days",
                "en %d días");
        p("Evens %d · Odds %d · Sum %d",
                "Pares %d · Impares %d · Suma %d");
        p("Pata %d%d",
                "Pata %d%d");
        p("Quinta %d",
                "Quinta %d");
        p("LATEST RESULT",
                "ÚLTIMO RESULTADO");
        p("LATEST RESULTS",
                "ÚLTIMOS RESULTADOS");
        p("%s · %d/100",
                "%s · %d/100");
        p("No results stored yet.",
                "Todavía no hay resultados guardados.");
        p("Today's %s draw: result soon.",
                "Sorteo de hoy a las %s: resultado pronto.");
        p("Next draw: %s · %s (%s)",
                "Próximo sorteo: %s · %s (%s)");
        p("No draw scheduled in the next week.",
                "No hay sorteos programados en la próxima semana.");
        p("Result of %s missing. Tap ↻.",
                "Falta el resultado del %s. Toca ↻.");
        p("Not updated yet: showing the bundled history.",
                "Aún sin actualizar: se muestra el historial incluido.");
        p("Last updated %s. Tap ↻.",
                "Última actualización %s. Toca ↻.");
        p("%d source(s) didn't answer (see History).",
                "%d fuente(s) no respondieron (ver Historial).");
        p("TODAY'S RECOMMENDATION",
                "RECOMENDACIÓN DE HOY");
        p("RECOMMENDATION · %s",
                "RECOMENDACIÓN · %s");
        p("For %s",
                "Para el %s");
        p("Probability",
                "Probabilidad");
        p("The model beats chance in the tests (see Picks).",
                "El modelo supera al azar en las pruebas (ver Jugadas).");
        p("Same as any other number: no model beats chance.",
                "La misma de cualquier otro número: ningún modelo supera al azar.");
        p("%s · %s",
                "%s · %s");
        p("Model ranking & track record",
                "Ranking de modelos e historial");
        p("CHECK A COMBINATION",
                "REVISAR UNA COMBINACIÓN");
        p("Five numbers from 1 to 43, then the superbalota (1–16, optional).",
                "Cinco números del 1 al 43 y luego la superbalota (1–16, opcional).");
        p("Five numbers from 1 to 39.",
                "Cinco números del 1 al 39.");
        p("Check",
                "Revisar");
        p("Latest draw: %s (%s)",
                "Último sorteo: %s (%s)");
        p("Latest draw: no prize, %d matched (%s)",
                "Último sorteo: sin premio, %d acertados (%s)");
        p("WHAT YOU WIN",
                "LO QUE GANAS");
        p("Net prize, after withholding where it applies.",
                "Premio neto, ya descontada la retención cuando aplica.");
        p("NOTIFICATIONS",
                "NOTIFICACIONES");
        p("%s results",
                "Resultados de %s");
        p("On",
                "Activadas");
        p("Off",
                "Desactivadas");
        p("Android is blocking notifications for LotoLab.",
                "Android está bloqueando las notificaciones de LotoLab.");
        p("Allow notifications",
                "Permitir notificaciones");
    }
}
