package app.loto.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * Spanish for every text in the app, keyed by its English original (see {@link T}). Generated from the keys used
 * in the code; a unit test checks that none is missing and that each keeps the same placeholders.
 */
final class Es {
    private Es() {}

    static final Map<String, String> MAP = new HashMap<>(842);

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
        p("3 (%s)",
                "3 (%s)");
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
        p("n/a",
                "n/d");
        p("p-value on its own → after correction. Green: as expected · amber: below 0.05 only before correction · coral: significant after it · grey: not enough data.",
                "Valor p solo → tras la corrección. Verde: como se espera · ámbar: por debajo de 0,05 solo antes de corregir · coral: significativo después · gris: no hay suficientes datos.");
        p("AN ANOMALY IS NOT EVIDENCE OF MANIPULATION",
                "UNA ANOMALÍA NO ES EVIDENCIA DE MANIPULACIÓN");
        p("Statistics can show that results are unlikely under a fair draw. It can't show why. In every long series of fair draws some patterns look suspicious, and with enough tests something always comes out \"significant\".",
                "La estadística puede mostrar que unos resultados son improbables en un sorteo justo. No puede mostrar por qué. En toda serie larga de sorteos justos algunos patrones parecen sospechosos, y con suficientes pruebas siempre sale algo \"significativo\".");
        p("Even a persistent deviation has ordinary explanations: balls of slightly different weight or wear, a drawing machine that changed, errors or gaps in the published results, or a change in how results are recorded. Deliberate interference is one possibility among these, not the default.",
                "Incluso una desviación persistente tiene explicaciones comunes: balotas de peso o desgaste ligeramente distinto, una máquina de sorteo que cambió, errores o huecos en los resultados publicados, o un cambio en cómo se registran. Una interferencia deliberada es una posibilidad entre ellas, no la explicación por defecto.");
        p("Real evidence of manipulation comes from outside the numbers: independent audits, inspection of the balls and machines, the draw videos and records, witnesses. Concerns go to the game's operator and its regulator (Coljuegos for Baloto and MiLoto; the departmental authority that licenses each chance game).",
                "La evidencia real de una manipulación viene de fuera de los números: auditorías independientes, inspección de las balotas y las máquinas, los videos y registros de los sorteos, testigos. Las inquietudes se dirigen al operador del juego y a su regulador (Coljuegos para Baloto y MiLoto; la autoridad departamental que autoriza cada chance).");
        p("HOW THE CHECK WORKS",
                "CÓMO FUNCIONA LA REVISIÓN");
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
        p("Chance · day, night and Saturday",
                "Chance · día, noche y sábado");
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
        p("%s per ticket · 4 cifras: the four digits in order",
                "%s por apuesta · 4 cifras: las cuatro cifras en orden");
        p("%s per ticket · %d numbers from 1–%d plus the superbalota (1–%d)",
                "%s por apuesta · %d números del 1 al %d más la superbalota (1–%d)");
        p("%s per ticket · %d numbers from 1–%d",
                "%s por apuesta · %d números del 1 al %d");
        p("Revancha is a second draw for the same ticket, with the same odds: playing both gives two independent chances, about 1 in 7,700,784 of at least one jackpot.",
                "Revancha es un segundo sorteo para el mismo tiquete, con las mismas probabilidades: jugar ambos da dos oportunidades independientes, más o menos 1 entre 7.700.784 de ganar al menos un premio mayor.");
        p("EVERY PRIZE TIER",
                "TODOS LOS NIVELES DE PREMIO");
        p("Each bet is placed separately (4, 3, 2 or 1 cifra, combinado). Prize amounts vary by operator and are not shown.",
                "Cada apuesta se hace por separado (4, 3, 2 o 1 cifra, combinado). El valor de los premios depende del operador y no se muestra.");
        p("Chance of winning any prize: %s.",
                "Probabilidad de ganar algún premio: %s.");
        p("EVERY POSSIBLE COMBINATION",
                "TODAS LAS COMBINACIONES POSIBLES");
        p("There are %s possible 4-digit numbers (0000–9999). Each one has exactly the same probability, %s (%s), in every draw.",
                "Hay %s números posibles de 4 cifras (0000–9999). Cada uno tiene exactamente la misma probabilidad, %s (%s), en cada sorteo.");
        p("There are %s possible combinations. Each one has exactly the same probability, %s (%s), in every draw.",
                "Hay %s combinaciones posibles. Cada una tiene exactamente la misma probabilidad, %s (%s), en cada sorteo.");
        p("0000 is exactly as likely as 4817. Numbers \"due\" to come out, or that came out yesterday, have the same chance as any other.",
                "El 0000 es exactamente igual de probable que el 4817. Los números que \"ya deben salir\", o que salieron ayer, tienen la misma probabilidad que cualquier otro.");
        p("01 02 03 04 05 is exactly as likely as any random-looking combination; it only looks special. What differs between combinations is how many other people play them, which matters if a jackpot is shared.",
                "01 02 03 04 05 es exactamente igual de probable que cualquier combinación que parezca aleatoria; solo se ve especial. Lo que cambia entre combinaciones es cuánta gente más las juega, y eso importa si el premio mayor se reparte.");
        p("PLAYING EVERY DRAW FOR A YEAR",
                "JUGAR TODOS LOS SORTEOS DURANTE UN AÑO");
        p("Draws in a year",
                "Sorteos en un año");
        p("Top prize at least once",
                "Premio mayor al menos una vez");
        p("With one ticket per draw. On average that's one win every %s years.",
                "Con una apuesta por sorteo. En promedio, una victoria cada %s años.");
        p("2 cifras at least once",
                "2 cifras al menos una vez");
        p("Last two digits, one bet per draw.",
                "Las dos últimas cifras, una apuesta por sorteo.");
        p("Any prize at least once",
                "Algún premio al menos una vez");
        p("Highest estimated probability under this model",
                "La probabilidad estimada más alta según este modelo");
        p("Suggested combination",
                "Combinación sugerida");
        p("MORE FROM THIS MODEL",
                "MÁS DE ESTE MODELO");
        p("New suggestions",
                "Nuevas sugerencias");
        p("BACKTEST",
                "PRUEBA HACIA ATRÁS");
        p("A model that has found a real bias should match more than chance here, draw after draw. In a fair lottery every model, including quick pick, averages the same.",
                "Un modelo que haya encontrado un sesgo real debería acertar más que el azar aquí, sorteo tras sorteo. En una lotería justa todos los modelos, incluida la jugada al azar, rinden igual en promedio.");
        p("THEORY VS. HISTORY",
                "TEORÍA VS. HISTORIA");
        p("Every single combination has a theoretical probability of %s in every draw, fixed by the rules. The models only reweight numbers by past frequencies: if the draw is fair, the past doesn't change the next result and no model beats the theoretical odds. The expected return of every ticket is negative; the suggestions are for curiosity, not a way to improve your odds.",
                "Cada combinación tiene una probabilidad teórica de %s en cada sorteo, fijada por las reglas. Los modelos solo reponderan los números según las frecuencias pasadas: si el sorteo es justo, el pasado no cambia el próximo resultado y ningún modelo supera las probabilidades teóricas. El rendimiento esperado de toda apuesta es negativo; las sugerencias son por curiosidad, no una forma de mejorar tus probabilidades.");
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
        p("Typical shape",
                "Forma típica");
        p("Least shared",
                "Menos compartida");
        p("Quick pick",
                "Al azar");
        p("Favours the numbers drawn most often in the period.",
                "Favorece los números que más han salido en el período.");
        p("Like hot numbers, but recent draws count more (weights halve every few dozen draws).",
                "Como los números calientes, pero los sorteos recientes pesan más (el peso se reduce a la mitad cada pocas docenas de sorteos).");
        p("Favours the numbers that haven't come up for longest. In a fair draw a number is never \"due\": this is the gambler's fallacy, shown for comparison.",
                "Favorece los números que llevan más tiempo sin salir. En un sorteo justo ningún número \"ya debe salir\": es la falacia del jugador, que se muestra para comparar.");
        p("Starts from a fair draw and moves away from it only as far as the data's real variation supports. If frequencies vary no more than chance, every number stays equal.",
                "Parte de un sorteo justo y se aleja de él solo tanto como lo justifica la variación real de los datos. Si las frecuencias no varían más que por azar, todos los números quedan iguales.");
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
        p("All %s draws together",
                "Todos los sorteos de %s juntos");
        p("RANDOMNESS SCORE",
                "PUNTAJE DE ALEATORIEDAD");
        p("How it's scored: each test counts fully unless its corrected p-value is below 0.05 (0.6), 0.01 (0.3) or 0.001 (0). A fair lottery scores 90–100 most of the time; the score measures how well the results fit a fair draw, not whether the lottery is honest.",
                "Cómo se calcula: cada prueba cuenta completa salvo que su valor p corregido quede por debajo de 0,05 (0,6), 0,01 (0,3) o 0,001 (0). Una lotería justa saca 90–100 la mayor parte del tiempo; el puntaje mide qué tan bien encajan los resultados con un sorteo justo, no si la lotería es honesta.");
        p("SCORE BY DRAW · %s",
                "PUNTAJE POR SORTEO · %s");
        p("%d / 100",
                "%d / 100");
        p("%s · %s draws",
                "%s · %s sorteos");
        p("THE TESTS",
                "LAS PRUEBAS");
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
        p("PATTERN OR COINCIDENCE?",
                "¿PATRÓN O COINCIDENCIA?");
        p("A p-value is how often a fair draw would produce a result at least this far from expectation. At p = 0.05 that's 1 time in 20, so running a dozen tests on a perfectly fair lottery flags something about half the time. That's why the score uses corrected p-values (Holm–Bonferroni), and why a single low p-value is a coincidence until it repeats.",
                "Un valor p indica con qué frecuencia un sorteo justo daría un resultado al menos así de lejos de lo esperado. Con p = 0,05 es 1 vez de cada 20, así que correr una docena de pruebas sobre una lotería perfectamente justa marca algo más o menos la mitad de las veces. Por eso el puntaje usa valores p corregidos (Holm–Bonferroni), y por eso un valor p bajo aislado es una coincidencia hasta que se repita.");
        p("A pattern is worth taking seriously when it survives the correction, is large enough to matter, and shows up again in new draws. The Integrity tab checks exactly that, split by period.",
                "Vale la pena tomar en serio un patrón cuando sobrevive a la corrección, es lo bastante grande para importar y vuelve a aparecer en sorteos nuevos. La pestaña Integridad revisa justamente eso, dividiendo el período.");
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
        p("HOT & COLD DIGITS",
                "CIFRAS CALIENTES Y FRÍAS");
        p("HOT & COLD NUMBERS",
                "NÚMEROS CALIENTES Y FRÍOS");
        p("How often each digit came up in each position. A fair draw gives every cell %s. Warm cells came up more, cool ones less.",
                "Cuántas veces salió cada cifra en cada posición. Un sorteo justo da %s en cada casilla. Las casillas cálidas salieron más; las frías, menos.");
        p("Times each number was drawn. The dashed line is a fair draw's expectation (%s each).",
                "Veces que salió cada número. La línea punteada es lo que espera un sorteo justo (%s cada uno).");
        p("Most frequent",
                "Los más frecuentes");
        p("Least frequent",
                "Los menos frecuentes");
        p("Longest absent",
                "Más tiempo sin salir");
        p("%s  draws  (a value waits %s draws on average)",
                "%s  sorteos  (un valor espera %s sorteos en promedio)");
        p("Are these differences real? Overall chi-square p = %s: %s The single most extreme value, allowing for the %d values compared: p = %s.",
                "¿Son reales estas diferencias? Chi-cuadrado general p = %s: %s El valor más extremo, teniendo en cuenta los %d valores comparados: p = %s.");
        p("the spread is what fair draws produce.",
                "la dispersión es la que producen los sorteos justos.");
        p("more spread than fair draws usually produce.",
                "más dispersión de la que suelen producir los sorteos justos.");
        p("TWO-DIGIT ENDINGS (2 CIFRAS)",
                "TERMINACIONES DE DOS CIFRAS (2 CIFRAS)");
        p("Each of the 100 endings is expected %s times.",
                "Se espera que cada una de las 100 terminaciones salga %s veces.");
        p("Most frequent ending, allowing for 100 endings: p = %s.",
                "La terminación más frecuente, teniendo en cuenta las 100: p = %s.");
        p("%d draws with a %s. Fair: %s each.",
                "%d sorteos con %s. Justo: %s cada una.");
        p("Chi-square p = %s.",
                "Chi-cuadrado p = %s.");
        p("REPEATED NUMBERS",
                "NÚMEROS REPETIDOS");
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
        p("%s expected",
                "%s esperados");
        p("From one draw to the next",
                "De un sorteo al siguiente");
        p("Same last two digits as the draw before",
                "Mismas dos últimas cifras que el sorteo anterior");
        p("%s expected · p = %s",
                "%s esperados · p = %s");
        p("Draw-to-draw repeats, p = %s.",
                "Repeticiones de sorteo a sorteo, p = %s.");
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
        p("Pairs of identical results",
                "Pares de resultados idénticos");
        p("%s expected by chance (the birthday problem) · p = %s",
                "%s esperados por azar (el problema del cumpleaños) · p = %s");
        p("EVEN & ODD",
                "PARES E IMPARES");
        p("%dE",
                "%dP");
        p("Draws by number of even digits (0E = all odd). Bars: observed; dots: a fair draw.",
                "Sorteos según la cantidad de cifras pares (0P = todas impares). Barras: lo observado; puntos: un sorteo justo.");
        p("Draws by number of even numbers (0E = all odd). Bars: observed; dots: a fair draw.",
                "Sorteos según la cantidad de números pares (0P = todos impares). Barras: lo observado; puntos: un sorteo justo.");
        p("%s of all digits were even (fair: %s). Chi-square p = %s.",
                "El %s de todas las cifras fueron pares (justo: %s). Chi-cuadrado p = %s.");
        p("%s of all numbers were even (fair: %s). Chi-square p = %s.",
                "El %s de todos los números fueron pares (justo: %s). Chi-cuadrado p = %s.");
        p("RECURRING COMBINATIONS & PATTERNS",
                "COMBINACIONES Y PATRONES RECURRENTES");
        p("Most frequent three-digit endings (3 cifras)",
                "Terminaciones de tres cifras más frecuentes (3 cifras)");
        p("Most frequent of the 1,000 endings: p = %s.",
                "La más frecuente de las 1.000 terminaciones: p = %s.");
        p("Pairs drawn together most often",
                "Parejas que más han salido juntas");
        p("With %s possible pairs some are bound to come up often: the top pair's p, allowing for that, is %s.",
                "Con %s parejas posibles, algunas van a salir seguido por fuerza: el valor p de la pareja más frecuente, teniendo eso en cuenta, es %s.");
        p("Triples drawn together most often",
                "Tríos que más han salido juntos");
        p("Out of %s possible triples: p = %s.",
                "De %s tríos posibles: p = %s.");
        p("Shapes",
                "Formas");
        p("SUM OF THE NUMBERS",
                "SUMA DE LOS NÚMEROS");
        p("Draws by the total of their digits; dots show a fair draw.",
                "Sorteos según el total de sus cifras; los puntos muestran un sorteo justo.");
        p("Draws by the total of their numbers; dots show a fair draw.",
                "Sorteos según el total de sus números; los puntos muestran un sorteo justo.");
        p("Average %.1f (fair %.1f), spread %.1f (fair %.1f). Half of all fair draws total %d–%d. Chi-square p = %s.",
                "Promedio %.1f (justo %.1f), dispersión %.1f (justo %.1f). La mitad de los sorteos justos suma %d–%d. Chi-cuadrado p = %s.");
        p("BY DAY OF THE WEEK",
                "POR DÍA DE LA SEMANA");
        p("Top endings: %s",
                "Terminaciones más frecuentes: %s");
        p("Top numbers: %s",
                "Números más frecuentes: %s");
        p("Too few draws per weekday to test whether the numbers depend on the day.",
                "Muy pocos sorteos por día de la semana para probar si los números dependen del día.");
        p("Do numbers depend on the weekday? p = %s: no sign they do.",
                "¿Los números dependen del día de la semana? p = %s: no hay señales de que así sea.");
        p("Do numbers depend on the weekday? p = %s.",
                "¿Los números dependen del día de la semana? p = %s.");
        p("Very unlikely by chance",
                "Muy improbable por azar");
        p("Unusual (p < 0.05)",
                "Inusual (p < 0,05)");
        p("Within chance",
                "Dentro del azar");
        p("in %d min",
                "en %d min");
        p("in %d h %d min",
                "en %d h %d min");
        p("in %d days",
                "en %d días");
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
        p("Plays %s",
                "Juega %s");
        p("SUGGESTED PICK",
                "JUGADA SUGERIDA");
        p("Model: %s · from %s draws of %s",
                "Modelo: %s · a partir de %s sorteos de %s");
        p("In a fair draw every combination has the same chance; the model only reflects past frequencies. Other models, alternatives and the reasoning are in Picks.",
                "En un sorteo justo todas las combinaciones tienen la misma probabilidad; el modelo solo refleja frecuencias pasadas. Los demás modelos, las alternativas y el razonamiento están en Jugadas.");
        p("Open Picks",
                "Abrir Jugadas");
        p("CHECK A NUMBER",
                "REVISAR UN NÚMERO");
        p("CHECK A COMBINATION",
                "REVISAR UNA COMBINACIÓN");
        p("Type a 4-digit number to see its odds and how it compares with the suggested pick.",
                "Escribe un número de 4 cifras para ver sus probabilidades y cómo se compara con la jugada sugerida.");
        p("Type 5 numbers (and optionally + the superbalota) to compare them with the suggested pick, e.g. 5 13 19 25 29 + 7.",
                "Escribe 5 números (y si quieres, + la superbalota) para compararlos con la jugada sugerida, p. ej. 5 13 19 25 29 + 7.");
        p("Type 5 numbers to compare them with the suggested pick, e.g. 5 13 19 25 29.",
                "Escribe 5 números para compararlos con la jugada sugerida, p. ej. 5 13 19 25 29.");
        p("Check",
                "Revisar");
        p("Yours",
                "El tuyo");
        p("Suggested",
                "Sugerido");
        p("Times drawn (in %s)",
                "Veces que salió (en %s)");
        p("Last three digits seen",
                "Tres últimas cifras vistas");
        p("Last two digits seen",
                "Dos últimas cifras vistas");
        p("Same digits, any order",
                "Mismas cifras, en cualquier orden");
        p("Draws matching 3+ of them",
                "Sorteos con 3+ de ellos");
        p("Draws matching 4+ of them",
                "Sorteos con 4+ de ellos");
        p("Fair draws with this shape",
                "Sorteos justos con esta forma");
        p("Both have exactly the same chance in a fair draw: %s.",
                "Ambos tienen exactamente la misma probabilidad en un sorteo justo: %s.");
        p("The model rates the suggested pick %.2f× yours, from past frequencies only; that isn't a real difference in odds unless the draw is biased.",
                "El modelo califica la jugada sugerida %.2f× la tuya, solo por frecuencias pasadas; no es una diferencia real de probabilidades a menos que el sorteo esté sesgado.");
        p("NOTIFICATIONS",
                "NOTIFICACIONES");
        p("A notification as soon as each %s result is announced, with its numbers and whether it was an ordinary or unusual draw.",
                "Una notificación apenas se anuncie cada resultado de %s, con sus números y si fue un sorteo ordinario o inusual.");
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
