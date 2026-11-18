package app.loto.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * Spanish for every text in the app, keyed by its English original (see {@link T}). Generated from the keys used
 * in the code; a unit test checks that none is missing and that each keeps the same placeholders.
 */
final class Es {
    private Es() {}

    static final Map<String, String> MAP = new HashMap<>(1176);

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
        p("%d in the same place",
                "%d en el mismo lugar");
        p("%d shared",
                "%d en común");
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
        p("OF 100",
                "DE 100");
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
        p("Audit: how ordinary each draw is, from 100 (completely typical) down; unusual draws are expected now and then (about 1 in 10 scores below 10). Details in Randomness.",
                "Auditoría: qué tan ordinario es cada sorteo, de 100 (completamente típico) hacia abajo; los sorteos inusuales son de esperar de vez en cuando (más o menos 1 de cada 10 queda por debajo de 10). Detalles en Aleatoriedad.");
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
        p("%s · %s. p = %s, %s after correcting for %d tests.",
                "%s · %s. p = %s; %s tras corregir por %d pruebas.");
        p("First half p = %s, second half p = %s.",
                "Primera mitad p = %s, segunda mitad p = %s.");
        p("The deviation is there in both halves of the period, so it's unlikely to be a one-off coincidence. It points to something systematic: worn or unevenly weighted balls, a machine or procedure that changed, errors in the published results, or deliberate interference. Statistics can't tell these apart.",
                "La desviación aparece en las dos mitades del período, así que es poco probable que sea una coincidencia aislada. Apunta a algo sistemático: balotas gastadas o de peso desigual, una máquina o un procedimiento que cambió, errores en los resultados publicados o una interferencia deliberada. La estadística no puede distinguir entre estas causas.");
        p("Significant over the whole period but not in both halves on its own, which is what a chance fluctuation (or a short-lived problem) looks like. Worth watching as new draws arrive.",
                "Significativa en todo el período, pero no en cada mitad por separado, que es como se ve una fluctuación del azar (o un problema pasajero). Vale la pena vigilarla a medida que lleguen nuevos sorteos.");
        p("Data check",
                "Revisión de datos");
        p("A gap or error in the published results, not a sign of how the draws were made.",
                "Un hueco o un error en los resultados publicados, no una señal de cómo se hicieron los sorteos.");
        p("Not enough data",
                "No hay suficientes datos");
        p("This period has too few draws to test. Choose a longer period.",
                "Este período tiene muy pocos sorteos para hacer pruebas. Elige un período más largo.");
        p("Persistent deviation from randomness",
                "Desviación persistente del azar");
        p("At least one pattern is both statistically significant after correction and present in both halves of the period. That is a reason to look closer, not proof of manipulation: proving manipulation needs evidence such as audits, inspection of the balls and machines, or the draw records.",
                "Al menos un patrón es estadísticamente significativo después de la corrección y aparece en las dos mitades del período. Es un motivo para mirar más de cerca, no una prueba de manipulación: probar una manipulación requiere evidencia como auditorías, inspección de las balotas y las máquinas, o los registros de los sorteos.");
        p("Statistical anomaly, most likely chance",
                "Anomalía estadística, muy probablemente azar");
        p("Something came out significant after correction, but it doesn't hold up in both halves of the period. Unusual patterns turn up in every long run of fair draws; this one isn't persistent enough to suggest a systematic cause.",
                "Algo salió significativo después de la corrección, pero no se sostiene en las dos mitades del período. En toda serie larga de sorteos justos aparecen patrones raros; este no es lo bastante persistente como para sugerir una causa sistemática.");
        p("No sign of irregularities",
                "Sin señales de irregularidades");
        p("None of the %d tests shows a deviation that survives the correction for multiple testing. %d came out below p = 0.05 on their own, about what chance alone produces (%.1f expected). The draws look like fair random draws.",
                "Ninguna de las %d pruebas muestra una desviación que sobreviva a la corrección por pruebas múltiples. %d quedaron por debajo de p = 0,05 por sí solas, más o menos lo que produce el azar (%.1f esperadas). Los sorteos se ven como sorteos aleatorios justos.");
        p("Popular-number avoidance",
                "Evitar los números populares");
        p("Do numbers that look like a birthday (DDMM, e.g. 2512) come up less often than they should?",
                "¿Los números que parecen un cumpleaños (DDMM, p. ej. 2512) salen menos de lo que deberían?");
        p("Do the numbers 1 to 31 (birthday picks) come up less often than they should?",
                "¿Los números del 1 al 31 (los de cumpleaños) salen menos de lo que deberían?");
        p("%d date-like results (%s expected).",
                "%d resultados con forma de fecha (%s esperados).");
        p("%d of %d balls were 31 or lower (%s expected).",
                "%d de %d balotas fueron de 31 o menos (%s esperadas).");
        p("Same-day overlap",
                "Coincidencias del mismo día");
        p("Do two draws held on the same day have more (or fewer) numbers in common than chance?",
                "¿Dos sorteos del mismo día tienen más (o menos) números en común que por azar?");
        p("%d days with both draws: %s numbers in common (%s expected).",
                "%d días con ambos sorteos: %s números en común (%s esperados).");
        p("Identical results on the same day",
                "Resultados idénticos el mismo día");
        p("Do both draws ever give exactly the same result on one day, more often than chance?",
                "¿Los dos sorteos dan exactamente el mismo resultado un mismo día con más frecuencia que el azar?");
        p("%d identical (%s expected).",
                "%d idénticos (%s esperados).");
        p("%s: %d result(s) with impossible values (out of range or a repeated ball).",
                "%s: %d resultado(s) con valores imposibles (fuera de rango o una balota repetida).");
        p("%s: %d row(s) in the history couldn't be read.",
                "%s: %d fila(s) del historial no se pudieron leer.");
        p("%s: no results for %s, when it normally draws.",
                "%s: no hay resultados para %s, cuando normalmente juega.");
        p("%s: %d stretches with no results on days it normally draws, the latest: %s.",
                "%s: %d tramos sin resultados en días en que normalmente juega; el más reciente: %s.");
        p("%s: %d stretches with no results on days it normally draws: %s.",
                "%s: %d tramos sin resultados en días en que normalmente juega: %s.");
        p("%s – %s (%d draw days)",
                "%s – %s (%d días de sorteo)");
        p("persistent deviation",
                "desviación persistente");
        p("anomaly · likely chance",
                "anomalía · probable azar");
        p("data",
                "datos");
        p("clear",
                "limpio");
        p("INTEGRITY CHECK · %s",
                "REVISIÓN DE INTEGRIDAD · %s");
        p("descriptive",
                "descriptivo");
        p("Every draw of %s (all its draw times) · %s · %s draws",
                "Todos los sorteos de %s (todos sus horarios) · %s · %s sorteos");
        p("Tests run",
                "Pruebas realizadas");
        p("Below p = 0.05 on their own",
                "Por debajo de p = 0,05 por sí solas");
        p("%s expected by chance alone · more than chance? p = %s",
                "%s esperadas solo por azar · ¿más que el azar? p = %s");
        p("Significant after correction",
                "Significativas tras la corrección");
        p("Holm–Bonferroni over all %d tests",
                "Holm–Bonferroni sobre las %d pruebas");
        p("FINDINGS",
                "HALLAZGOS");
        p("EVERY CHECK",
                "TODAS LAS REVISIONES");
        p("Each draw series tested on its own, plus same-day comparisons between series and a test for results avoiding the numbers people bet most.",
                "Cada serie de sorteos probada por separado, más comparaciones del mismo día entre series y una prueba de resultados que eviten los números que más juega la gente.");
        p("All %d tests",
                "Las %d pruebas");
        p("n/a",
                "n/d");
        p("p-value on its own → after correction. Green: as expected · amber: below 0.05 only before correction · coral: significant after it · grey: not enough data.",
                "Valor p solo → tras la corrección. Verde: como se espera · ámbar: por debajo de 0,05 solo antes de corregir · coral: significativo después · gris: no hay suficientes datos.");
        p("AN ANOMALY IS NOT EVIDENCE OF MANIPULATION",
                "UNA ANOMALÍA NO ES EVIDENCIA DE MANIPULACIÓN");
        p("Statistics can show that results are unlikely under a fair draw. It can't show why. In every long series of fair draws some patterns look suspicious, and with enough tests something always comes out \"significant\".",
                "La estadística puede mostrar que unos resultados son improbables en un sorteo justo. No puede mostrar por qué. En toda serie larga de sorteos justos algunos patrones parecen sospechosos, y con suficientes pruebas siempre sale algo \"significativo\".");
        p("Ordinary explanations, and where real evidence comes from",
                "Explicaciones comunes y de dónde vienen las pruebas reales");
        p("Even a persistent deviation has ordinary explanations: balls of slightly different weight or wear, a drawing machine that changed, errors or gaps in the published results, or a change in how results are recorded. Deliberate interference is one possibility among these, not the default.",
                "Incluso una desviación persistente tiene explicaciones comunes: balotas de peso o desgaste ligeramente distinto, una máquina de sorteo que cambió, errores o huecos en los resultados publicados, o un cambio en cómo se registran. Una interferencia deliberada es una posibilidad entre ellas, no la explicación por defecto.");
        p("Real evidence of manipulation comes from outside the numbers: independent audits, inspection of the balls and machines, the draw videos and records, witnesses. Concerns go to the game's operator and its regulator (Coljuegos for Baloto and MiLoto; the departmental authority that licenses each chance game).",
                "La evidencia real de una manipulación viene de fuera de los números: auditorías independientes, inspección de las balotas y las máquinas, los videos y registros de los sorteos, testigos. Las inquietudes se dirigen al operador del juego y a su regulador (Coljuegos para Baloto y MiLoto; la autoridad departamental que autoriza cada chance).");
        p("How the check works",
                "Cómo funciona la revisión");
        p("1. Every randomness test runs on every draw series separately.\n2. Draws held on the same day are compared: do they share more numbers than chance?\n3. Popular-number test: rigged results might avoid what people bet most (birthdays, dates).\n4. All p-values are corrected together (Holm–Bonferroni), so running many tests doesn't manufacture findings.\n5. Anything that survives is re-tested on the first and second half of the period. A systematic effect shows in both; a coincidence rarely does.\n6. The data is checked for impossible values and unexplained breaks in the schedule.",
                "1. Cada prueba de aleatoriedad se corre por separado en cada serie de sorteos.\n2. Se comparan los sorteos del mismo día: ¿comparten más números que por azar?\n3. Prueba de números populares: unos resultados amañados podrían evitar lo que más juega la gente (cumpleaños, fechas).\n4. Todos los valores p se corrigen juntos (Holm–Bonferroni), así que correr muchas pruebas no fabrica hallazgos.\n5. Lo que sobrevive se vuelve a probar en la primera y la segunda mitad del período. Un efecto sistemático aparece en ambas; una coincidencia, rara vez.\n6. Se revisan los datos en busca de valores imposibles y huecos en el calendario sin explicación.");
        p("Lottery: %s. Swipe left or right to change.",
                "Lotería: %s. Desliza a la izquierda o a la derecha para cambiar.");
        p("Summary",
                "Resumen");
        p("History",
                "Historial");
        p("Stats",
                "Estadísticas");
        p("Randomness",
                "Aleatoriedad");
        p("Picks",
                "Jugadas");
        p("Integrity",
                "Integridad");
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
        p("All draws",
                "Todos");
        p("Running every test on every draw…",
                "Corriendo cada prueba en cada sorteo…");
        p("Working it out…",
                "Calculando…");
        p("SOMETHING WENT WRONG",
                "ALGO SALIÓ MAL");
        p("This view couldn't be worked out: %s",
                "No se pudo calcular esta vista: %s");
        p("NO DRAWS",
                "SIN SORTEOS");
        p("There are no stored results for %s in this period yet. Tap ↻ to fetch the newest results, or choose a longer period.",
                "Todavía no hay resultados guardados de %s en este período. Toca ↻ para traer los más recientes, o elige un período más largo.");
        p("%s (all draws)",
                "%s (todos los sorteos)");
        p("%s · %s · %s draws",
                "%s · %s · %s sorteos");
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
        p("By model · %s",
                "Por modelo · %s");
        p("last %d days",
                "últimos %d días");
        p("all history",
                "todo el historial");
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
        p("All %s draws together",
                "Todos los sorteos de %s juntos");
        p("RANDOMNESS SCORE",
                "PUNTAJE DE ALEATORIEDAD");
        p("How it's scored, and pattern or coincidence?",
                "Cómo se puntúa, y ¿patrón o coincidencia?");
        p("How it's scored: each test counts fully unless its corrected p-value is below 0.05 (0.6), 0.01 (0.3) or 0.001 (0). A fair lottery scores 90–100 most of the time; the score measures how well the results fit a fair draw, not whether the lottery is honest.",
                "Cómo se calcula: cada prueba cuenta completa salvo que su valor p corregido quede por debajo de 0,05 (0,6), 0,01 (0,3) o 0,001 (0). Una lotería justa saca 90–100 la mayor parte del tiempo; el puntaje mide qué tan bien encajan los resultados con un sorteo justo, no si la lotería es honesta.");
        p("A p-value is how often a fair draw would produce a result at least this far from expectation. At p = 0.05 that's 1 time in 20, so running a dozen tests on a perfectly fair lottery flags something about half the time. That's why the score uses corrected p-values (Holm–Bonferroni), and why a single low p-value is a coincidence until it repeats.",
                "Un valor p indica con qué frecuencia un sorteo justo daría un resultado al menos así de lejos de lo esperado. Con p = 0,05 es 1 vez de cada 20, así que correr una docena de pruebas sobre una lotería perfectamente justa marca algo más o menos la mitad de las veces. Por eso el puntaje usa valores p corregidos (Holm–Bonferroni), y por eso un valor p bajo aislado es una coincidencia hasta que se repita.");
        p("A pattern is worth taking seriously when it survives the correction, is large enough to matter, and shows up again in new draws. The Integrity tab checks exactly that, split by period.",
                "Vale la pena tomar en serio un patrón cuando sobrevive a la corrección, es lo bastante grande para importar y vuelve a aparecer en sorteos nuevos. La pestaña Integridad revisa justamente eso, dividiendo el período.");
        p("SCORE BY DRAW · %s",
                "PUNTAJE POR SORTEO · %s");
        p("%d / 100",
                "%d / 100");
        p("%s · %s draws",
                "%s · %s sorteos");
        p("THE TESTS",
                "LAS PRUEBAS");
        p("What each test checks",
                "Qué revisa cada prueba");
        p("p = %s · corrected %s",
                "p = %s · corregido %s");
        p("χ² %.1f, df %d",
                "χ² %.1f, gl %d");
        p("UNUSUAL DRAWS",
                "SORTEOS INUSUALES");
        p("Unusual draws are expected: in %s fair draws about %s would score below 10 and %s below 1. Here: %d and %d.",
                "Los sorteos inusuales son de esperar: en %s sorteos justos, unos %s quedarían por debajo de 10 y %s por debajo de 1. Aquí: %d y %d.");
        p("Are there more unusual draws than chance gives? Binomial test on the draws scoring below 10.",
                "¿Hay más sorteos inusuales de lo que da el azar? Prueba binomial sobre los sorteos por debajo de 10.");
        p("Most unusual in this period",
                "Los más inusuales del período");
        p("%d more",
                "%d más");
        p("Consistent with a fair random draw",
                "Consistente con un sorteo aleatorio justo");
        p("Small deviations, typical of chance",
                "Desviaciones pequeñas, típicas del azar");
        p("Notable deviations",
                "Desviaciones notables");
        p("Strong deviations from randomness",
                "Desviaciones fuertes del azar");
        p("Too few draws in this period for the tests to say anything. Choose a longer period.",
                "Muy pocos sorteos en este período para que las pruebas digan algo. Elige un período más largo.");
        p("%d of %d tests came out below p = 0.05 on their own (about %.1f would by chance alone). After correcting for running %d tests, the number still flagged is %d.",
                "%d de %d pruebas quedaron por debajo de p = 0,05 por sí solas (unas %.1f lo harían solo por azar). Tras corregir por correr %d pruebas, siguen marcadas %d.");
        p("Number frequencies",
                "Frecuencia de los números");
        p("Does every digit come up equally often in each of the four positions?",
                "¿Sale cada cifra con la misma frecuencia en cada una de las cuatro posiciones?");
        p("Does every ball from 1 to %d come up equally often?",
                "¿Sale cada balota del 1 al %d con la misma frecuencia?");
        p("Most frequent %s: %s times (%s expected). Least frequent %s: %s times.",
                "La más frecuente, %s: %s veces (%s esperadas). La menos frecuente, %s: %s veces.");
        p("%s frequencies",
                "Frecuencias: %s");
        p("Does every %s number come up equally often?",
                "¿Sale cada número de la %s con la misma frecuencia?");
        p("%d draws with a %s. Most frequent: %s (%s times, %s expected).",
                "%d sorteos con %s. La más frecuente: %s (%s veces, %s esperadas).");
        p("Even / odd split",
                "Reparto par / impar");
        p("Is the number of even digits per draw what a fair draw gives?",
                "¿La cantidad de cifras pares por sorteo es la que da un sorteo justo?");
        p("Is the number of even numbers per draw what a fair draw gives?",
                "¿La cantidad de números pares por sorteo es la que da un sorteo justo?");
        p("Average %.2f even per draw (fair: %.2f).",
                "Promedio de %.2f pares por sorteo (justo: %.2f).");
        p("Sum of the numbers",
                "Suma de los números");
        p("Are the totals of each draw spread the way fair draws spread?",
                "¿Los totales de cada sorteo se reparten como en los sorteos justos?");
        p("Average sum %.1f, spread %.1f (fair: %.1f and %.1f).",
                "Suma promedio %.1f, dispersión %.1f (justo: %.1f y %.1f).");
        p("Repeated digits (poker test)",
                "Cifras repetidas (prueba de póker)");
        p("Do numbers like 1123 or 7777 (repeated digits) appear as often as they should?",
                "¿Los números como 1123 o 7777 (cifras repetidas) salen tanto como deberían?");
        p("All digits different: %s (%s expected); with a pair: %s (%s); three or four alike: %s (%s).",
                "Todas las cifras distintas: %s (%s esperados); con un par: %s (%s); tres o cuatro iguales: %s (%s).");
        p("Consecutive numbers",
                "Números seguidos");
        p("Do draws with neighbouring numbers (like 17 and 18) appear as often as they should?",
                "¿Los sorteos con números vecinos (como 17 y 18) salen tanto como deberían?");
        p("%d draws had two or more consecutive numbers (%s expected).",
                "%d sorteos tuvieron dos o más números seguidos (%s esperados).");
        p("Repeats from the previous draw",
                "Repeticiones del sorteo anterior");
        p("Do digits stay in the same place from one draw to the next more (or less) than chance?",
                "¿Las cifras se quedan en el mismo lugar de un sorteo al siguiente más (o menos) que por azar?");
        p("Do numbers carry over from one draw to the next more (or less) than chance?",
                "¿Los números se repiten de un sorteo al siguiente más (o menos) que por azar?");
        p("%s repeats in %d draw-to-draw steps (%s expected).",
                "%s repeticiones en %d pasos de sorteo a sorteo (%s esperadas).");
        p("Repeated results",
                "Resultados repetidos");
        p("Does the exact same result come back more often than the \"birthday problem\" predicts?",
                "¿El mismo resultado exacto vuelve a salir más de lo que predice el \"problema del cumpleaños\"?");
        p("%d pairs of identical results (%s expected by chance).",
                "%d pares de resultados idénticos (%s esperados por azar).");
        p("Serial correlation",
                "Correlación serial");
        p("Does one draw's total say anything about the next one's?",
                "¿El total de un sorteo dice algo sobre el del siguiente?");
        p("Correlation between consecutive totals: %+.3f (fair: about 0 ± %.3f).",
                "Correlación entre totales consecutivos: %+.3f (justo: alrededor de 0 ± %.3f).");
        p("Runs above / below the middle",
                "Rachas por encima / por debajo del medio");
        p("Do high and low totals alternate the way a fair draw makes them, without long streaks or zigzags?",
                "¿Los totales altos y bajos se alternan como en un sorteo justo, sin rachas largas ni zigzags?");
        p("%d runs (fair: %.1f ± %.1f).",
                "%d rachas (justo: %.1f ± %.1f).");
        p("Waiting times (gap test)",
                "Tiempos de espera (prueba de huecos)");
        p("Do numbers come back after the waits a fair draw gives, not too regularly and not in clumps?",
                "¿Los números vuelven a salir tras las esperas que da un sorteo justo, ni demasiado regulares ni agrupados?");
        p("%d waits measured, average %.1f draws (fair: %.1f).",
                "%d esperas medidas, promedio de %.1f sorteos (justo: %.1f).");
        p("Independent positions",
                "Posiciones independientes");
        p("Is each digit independent of the one next to it (no favourite digit pairs like 3 followed by 7)?",
                "¿Cada cifra es independiente de la que está a su lado (sin parejas favoritas como un 3 seguido de un 7)?");
        p("Checked %d neighbouring digit pairs in each of %d draws.",
                "Se revisaron %d pares de cifras vecinas en cada uno de %d sorteos.");
        p("Day of the week",
                "Día de la semana");
        p("Are the numbers drawn on one day of the week different from those on other days?",
                "¿Los números que salen un día de la semana son distintos de los de otros días?");
        p("All draws in this selection fall on the same day of the week.",
                "Todos los sorteos de esta selección caen el mismo día de la semana.");
        p("Draws per day: %s.",
                "Sorteos por día: %s.");
        p("Stable over time",
                "Estable en el tiempo");
        p("Are the numbers in the first half of the period distributed like those in the second half?",
                "¿Los números de la primera mitad del período se reparten como los de la segunda mitad?");
        p("First half: %s – %s. Second half: %s – %s.",
                "Primera mitad: %s – %s. Segunda mitad: %s – %s.");
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
        p("AT A GLANCE",
                "DE UN VISTAZO");
        p("Digit frequencies",
                "Frecuencia de las cifras");
        p("Even & odd",
                "Pares e impares");
        p("Sums",
                "Sumas");
        p("Repeats from draw to draw",
                "Repeticiones de un sorteo al siguiente");
        p("Repeated digits in a number",
                "Cifras repetidas en un número");
        p("Descriptive: what happened in these draws, compared with what fair draws produce. With this many checks, one \"unusual\" now and then is expected by chance; the Randomness and Integrity tabs weigh them together.",
                "Descriptivo: lo que pasó en estos sorteos, comparado con lo que producen sorteos justos. Con tantas revisiones, es normal que por azar alguna salga \"inusual\" de vez en cuando; las pestañas Aleatoriedad e Integridad las evalúan en conjunto.");
        p("HOT & COLD DIGITS",
                "CIFRAS CALIENTES Y FRÍAS");
        p("HOT & COLD NUMBERS",
                "NÚMEROS CALIENTES Y FRÍOS");
        p("How often each digit came up in each position. A fair draw gives every cell %s; warm cells came up more, cool ones less.",
                "Cuántas veces salió cada cifra en cada posición. Un sorteo justo da %s en cada casilla; las cálidas salieron más, las frías menos.");
        p("Times each number was drawn. The dashed line is a fair draw's expectation (%s each).",
                "Veces que salió cada número. La línea punteada es lo que espera un sorteo justo (%s cada uno).");
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
        p("Most frequent ending, allowing for 100 endings: p = %s.",
                "La terminación más frecuente, teniendo en cuenta las 100: p = %s.");
        p("%d draws with a %s. Fair: %s each.",
                "%d sorteos con %s. Justo: %s cada una.");
        p("Chi-square p = %s.",
                "Chi-cuadrado p = %s.");
        p("EVEN & ODD",
                "PARES E IMPARES");
        p("%dE",
                "%dP");
        p("Draws by number of even digits (0E = all odd). Bars: observed; dots: a fair draw.",
                "Sorteos según la cantidad de cifras pares (0P = todas impares). Barras: lo observado; puntos: un sorteo justo.");
        p("Draws by number of even numbers (0E = all odd). Bars: observed; dots: a fair draw.",
                "Sorteos según la cantidad de números pares (0P = todos impares). Barras: lo observado; puntos: un sorteo justo.");
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
        p("Draws by the total of their digits; dots show a fair draw.",
                "Sorteos según el total de sus cifras; los puntos muestran un sorteo justo.");
        p("Draws by the total of their numbers; dots show a fair draw.",
                "Sorteos según el total de sus números; los puntos muestran un sorteo justo.");
        p("Average %.1f (fair %.1f), spread %.1f (fair %.1f). Half of all fair draws total %d–%d. Chi-square p = %s.",
                "Promedio %.1f (justo %.1f), dispersión %.1f (justo %.1f). La mitad de los sorteos justos suma %d–%d. Chi-cuadrado p = %s.");
        p("REPEATED NUMBERS",
                "NÚMEROS REPETIDOS");
        p("Pairs of identical results",
                "Pares de resultados idénticos");
        p("%s expected by chance (the birthday problem) · p = %s",
                "%s esperados por azar (el problema del cumpleaños) · p = %s");
        p("Same pata as the draw before",
                "Misma pata que el sorteo anterior");
        p("%s expected · p = %s",
                "%s esperados · p = %s");
        p("Numbers repeated from one draw to the next: p = %s.",
                "Números repetidos de un sorteo al siguiente: p = %s.");
        p("Inside a draw, draw to draw, same day",
                "Dentro de un sorteo, entre sorteos, el mismo día");
        p("Inside one number",
                "Dentro de un número");
        p("%s expected (%s of fair draws)",
                "%s esperados (el %s de los sorteos justos)");
        p("Poker test p = %s.",
                "Prueba de póker p = %s.");
        p("Inside one draw",
                "Dentro de un sorteo");
        p("%d draws",
                "%d sorteos");
        p("From one draw to the next",
                "De un sorteo al siguiente");
        p("Between draws on the same day",
                "Entre sorteos del mismo día");
        p("%d days · %s in common, %s expected · p = %s",
                "%d días · %s en común, %s esperados · p = %s");
        p("same ending %d× (%s expected)",
                "misma terminación %d× (%s esperadas)");
        p("%d identical",
                "%d idénticos");
        p("The same result more than once",
                "El mismo resultado más de una vez");
        p("RECURRING COMBINATIONS & PATTERNS",
                "COMBINACIONES Y PATRONES RECURRENTES");
        p("Most frequent last three digits",
                "Tres últimas cifras más frecuentes");
        p("Most frequent of the 1,000 endings: p = %s.",
                "La más frecuente de las 1.000 terminaciones: p = %s.");
        p("Pairs drawn together most often",
                "Parejas que más han salido juntas");
        p("With %s possible pairs some are bound to come up often: the top pair's p, allowing for that, is %s.",
                "Con %s parejas posibles, algunas van a salir seguido por fuerza: el valor p de la pareja más frecuente, teniendo eso en cuenta, es %s.");
        p("More pairs and triples",
                "Más parejas y tríos");
        p("Triples drawn together most often",
                "Tríos que más han salido juntos");
        p("Out of %s possible triples: p = %s.",
                "De %s tríos posibles: p = %s.");
        p("Shapes",
                "Formas");
        p("BY DAY OF THE WEEK",
                "POR DÍA DE LA SEMANA");
        p("Too few draws per weekday to test whether the numbers depend on the day.",
                "Muy pocos sorteos por día de la semana para probar si los números dependen del día.");
        p("Do numbers depend on the weekday? p = %s: no sign they do.",
                "¿Los números dependen del día de la semana? p = %s: no hay señales de que así sea.");
        p("Do numbers depend on the weekday? p = %s.",
                "¿Los números dependen del día de la semana? p = %s.");
        p("Each day",
                "Cada día");
        p("Top endings: %s",
                "Terminaciones más frecuentes: %s");
        p("Top numbers: %s",
                "Números más frecuentes: %s");
        p("Very unlikely by chance",
                "Muy improbable por azar");
        p("Unusual (p < 0.05)",
                "Inusual (p < 0,05)");
        p("Within chance",
                "Dentro del azar");
        p("Fill in all %d boxes with one digit each.",
                "Llena las %d casillas con una cifra cada una.");
        p("%d matched",
                "%d acertados");
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
        p("No results stored yet.",
                "Todavía no hay resultados guardados.");
        p("draw #%s",
                "sorteo n.º %s");
        p("%s · %d/100",
                "%s · %d/100");
        p("Today's draw was at %s: the result should be out soon.",
                "El sorteo de hoy fue a las %s: el resultado debería salir pronto.");
        p("Next draw: %s · %s (%s)",
                "Próximo sorteo: %s · %s (%s)");
        p("No draw scheduled in the next week.",
                "No hay sorteos programados en la próxima semana.");
        p("The result of %s isn't in yet, so the data may be incomplete. Tap ↻ to check again.",
                "El resultado del %s aún no ha llegado, así que los datos pueden estar incompletos. Toca ↻ para buscar de nuevo.");
        p("Plays %s",
                "Juega %s");
        p("Results not fetched yet: showing the bundled history.",
                "Aún no se han descargado resultados: se muestra el historial incluido.");
        p("Data updated %s.",
                "Datos actualizados %s.");
        p("More than 6 hours since the last update: newer results may be missing. Tap ↻.",
                "Han pasado más de 6 horas desde la última actualización: pueden faltar resultados nuevos. Toca ↻.");
        p("%d source(s) didn't answer on the last update (details in History).",
                "%d fuente(s) no respondieron en la última actualización (detalles en Historial).");
        p("TODAY'S RECOMMENDATION",
                "RECOMENDACIÓN DE HOY");
        p("RECOMMENDATION · %s",
                "RECOMENDACIÓN · %s");
        p("For the draw of %s",
                "Para el sorteo del %s");
        p("%s · ranked 1st of %d models",
                "%s · 1.º de %d modelos");
        p("Ranked first over the last %d draws, where its forecasts were exactly a fair guess's: it found nothing to prefer.",
                "Quedó primero en los últimos %d sorteos, donde sus pronósticos fueron exactamente los de una elección justa: no encontró nada que preferir.");
        p("Ranked first: over the last %d draws its forecasts gave the actual results %s probability compared with a fair guess (95%% CI %s to %s).",
                "Quedó primero: en los últimos %d sorteos sus pronósticos dieron a los resultados reales un %s de probabilidad frente a una elección justa (IC 95%% %s a %s).");
        p("Better than chance in the tests (Monte Carlo p = %s). Still check the track record before relying on it.",
                "Mejor que el azar en las pruebas (Monte Carlo p = %s). Aun así revisa el historial de aciertos antes de confiar en él.");
        p("No model shows a real advantage over chance (Monte Carlo p = %s): this number is as likely as any other, %s.",
                "Ningún modelo muestra una ventaja real sobre el azar (Monte Carlo p = %s): este número es tan probable como cualquier otro, %s.");
        p("Track record: %s past draws replayed · last two digits right %d times (%s expected, p = %s).",
                "Historial: %s sorteos pasados repetidos · las dos últimas cifras acertadas %d veces (%s esperadas, p = %s).");
        p("Track record: %s past draws replayed · %s numbers matched (%s expected, p = %s).",
                "Historial: %s sorteos pasados repetidos · %s números acertados (%s esperados, p = %s).");
        p("Generated %s from the results up to %s. It changes as new results arrive; its probability doesn't.",
                "Generada %s con los resultados hasta el %s. Cambia cuando llegan resultados nuevos; su probabilidad no.");
        p("Model ranking & track record",
                "Ranking de modelos e historial");
        p("CHECK A NUMBER",
                "REVISAR UN NÚMERO");
        p("CHECK A COMBINATION",
                "REVISAR UNA COMBINACIÓN");
        p("Choose the bet, type the digits and what you pay.",
                "Elige la apuesta, escribe las cifras y cuánto pagas.");
        p("In order",
                "En orden");
        p("Any order (combinado)",
                "En cualquier orden (combinado)");
        p("Pays %s pesos per peso bet.",
                "Paga %s pesos por cada peso apostado.");
        p("Five numbers from 1 to 43, then the superbalota (1–16, optional).",
                "Cinco números del 1 al 43 y luego la superbalota (1–16, opcional).");
        p("Five numbers from 1 to 39.",
                "Cinco números del 1 al 39.");
        p("Check",
                "Revisar");
        p("%s · %s",
                "%s · %s");
        p("Chance of winning this bet in one draw.",
                "Probabilidad de ganar esta apuesta en un sorteo.");
        p("If it wins",
                "Si gana");
        p("IVA %s",
                "IVA %s");
        p("Winning category",
                "Categoría ganadora");
        p("× %s per peso bet (official plan)",
                "× %s por peso apostado (plan oficial)");
        p("Withholding",
                "Retención");
        p("20%% when the prize exceeds 48 UVT (%s)",
                "20%% cuando el premio supera 48 UVT (%s)");
        p("Expected value: %s per bet; on average it returns %s of what you pay.",
                "Valor esperado: %s por apuesta; en promedio devuelve el %s de lo que pagas.");
        p("The same number as a shorter bet",
                "El mismo número en apuestas más cortas");
        p("%s · net prize for %s%s",
                "%s · premio neto por %s%s");
        p("wins in the latest draw",
                "gana en el último sorteo");
        p("Wins in the latest %s (%s: %s)",
                "Gana en el último %s (%s: %s)");
        p("Doesn't win in the latest %s (%s: %s)",
                "No gana en el último %s (%s: %s)");
        p("once",
                "una vez");
        p("%d times",
                "%d veces");
        p("In %s past draws it would have won %s (%s expected)%s.",
                "En %s sorteos pasados habría ganado %s (%s esperadas)%s.");
        p(", last on %s",
                ", la última el %s");
        p("The recommendation's %s is %s. Model's estimate: yours %s, the recommendation's %s; theoretical: %s both.",
                "En la recomendación, %s es %s. Estimación del modelo: la tuya %s, la de la recomendación %s; teórica: %s ambas.");
        p("Jackpot probability",
                "Probabilidad del premio mayor");
        p("Probability of these 5 numbers",
                "Probabilidad de estos 5 números");
        p("The same as any other combination.",
                "La misma de cualquier otra combinación.");
        p("Prize in the latest %s (%s): %s",
                "Premio en el último %s (%s): %s");
        p("No prize in the latest %s (%s: %s) · %s",
                "Sin premio en el último %s (%s: %s) · %s");
        p("In %s past draws: all five %d times, four or more %d, three or more %d.",
                "En %s sorteos pasados: los cinco %d veces, cuatro o más %d, tres o más %d.");
        p("Prize amounts depend on the pool and aren't fixed, so only the categories are shown (all tiers in Odds).",
                "Los montos dependen del acumulado y no son fijos, así que solo se muestran las categorías (todas en Probabilidades).");
        p("Recommendation: %s. Model's estimate: yours %s, the recommendation's %s; theoretical: %s both.",
                "Recomendación: %s. Estimación del modelo: la tuya %s, la de la recomendación %s; teórica: %s ambas.");
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
        p("Strong deviation",
                "Desviación fuerte");
        p("Clear deviation",
                "Desviación clara");
        p("Deviation",
                "Desviación");
        p("Chance-level blip",
                "Variación dentro del azar");
        p("As expected",
                "Como se espera");
    }
}
