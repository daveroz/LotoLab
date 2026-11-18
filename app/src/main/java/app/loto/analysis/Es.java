package app.loto.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * Spanish for every text in the app, keyed by its English original (see {@link T}). Generated from the keys used
 * in the code; a unit test checks that none is missing and that each keeps the same placeholders.
 */
final class Es {
    private Es() {}

    static final Map<String, String> MAP = new HashMap<>(480);

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
        p("Pata",
                "Pata");
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
        p("Not enough data",
                "No hay suficientes datos");
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
        p("The model beats chance in the tests (see Stats).",
                "El modelo supera al azar en las pruebas (ver Estadísticas).");
        p("Same as any other number: no model beats chance.",
                "La misma de cualquier otro número: ningún modelo supera al azar.");
        p("%s · %s",
                "%s · %s");
        p("Model ranking",
                "Ranking de modelos");
        p("WHAT YOU WIN",
                "LO QUE GANAS");
        p("Net prize, after withholding where it applies.",
                "Premio neto, ya descontada la retención cuando aplica.");
        p("NOTIFICATIONS",
                "NOTIFICACIONES");
        p("%s results",
                "Resultados de %s");
        p("The numbers about ten minutes after each draw.",
                "Los números unos diez minutos después de cada sorteo.");
        p("Android is blocking notifications for LotoLab.",
                "Android está bloqueando las notificaciones de LotoLab.");
        p("Allow notifications",
                "Permitir notificaciones");
    }
}
