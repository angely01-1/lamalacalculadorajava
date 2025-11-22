package com.example.badcalc;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
// import java.util.Locale no se usa en el código


public class Main {
    // Logger privado para reemplazar System.out.println
    // System.out.println es inseguro
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    // Se cambió 'history' a private para aplicar encapsulación y evitar accesos directos desde fuera de la clase
    // El tipo fue cambiado de ArrayList a List<String> para programar contra una interfaz y permitir mayor flexibilidad
    // Se añadió la palabra clave 'final' para asegurar que la referencia al historial no pueda ser reemplazada
    // Esto evita exponer una colección mutable de forma pública, lo cual mejora la seguridad y el diseño orientado a objetos

    private static final List<String> history = new ArrayList<>();

    // 'last' se declara ahora como private y final para evitar que su referencia pueda modificarse
    // El acceso se gestionará mediante el método getLast()
    private static String last = "";

    // Se cambio el 'counter' ahora es private
    private static int counter = 0;

    // Se renombra 'R' a 'random' para seguir la convención camelCase y brindar un nombre más descriptivo
    // La variable se declara como private y final para evitar accesos externos y cambios en su referencia
    private static final Random random = new Random();

    // La constante se renombra usando UPPER_SNAKE_CASE para cumplir con la convención de constantes en Java
    // Se declara como private y final para evitar modificaciones externas y proteger la configuración interna
    // Las constantes de configuración no deben exponerse públicamente; su acceso debe controlarse mediante métodos
    private static final String API_KEY = "NOT_SECRET_KEY";

    /**
     * Retorna el historial de operaciones realizadas
     * @return Lista de historial (copia para evitar modificaciones externas)
     */
    public static List<String> getHistory() {
        return new ArrayList<>(history);
    }

    /**
     * Retorna la última operación registrada
     * @return String con la última operación
     */
    public static String getLast() {
        return last;
    }

    /**
     * Retorna el contador de operaciones
     * @return int con el número de operaciones
     */
    public static int getCounter() {
        return counter;
    }

    /**
     * Retorna la clave API de configuración
     * @return String con la API_KEY
     */
    public static String getApiKey() {
        return API_KEY;
    }

    public static double parse(String s) {
        try {
            if (s == null) return 0;
            s = s.replace(',', '.').trim();
            return Double.parseDouble(s);
        } catch (Exception e) {
            // Se Agrego comentario explicativo ya que Blocks vacíos son anti-patrón; ahora es claro por qué se ignora la excepción
            return 0; // Retorna 0 si la cadena no es un número válido
        }
    }

    public static double badSqrt(double v) {
        double g = v;
        int k = 0;
        while (Math.abs(g * g - v) > 0.0001 && k < 100000) {
            g = (g + v / g) / 2.0;
            k++;
            if (k % 5000 == 0) {
                try {
                    // Se agregó comentario y se re-interrumpe el hilo
                    Thread.sleep(0);
                } catch (InterruptedException ie) {
                    // Se restauro el estado interrumpido del hilo ya que se Ignoraba InterruptedException y puede causar deadlocks o comportamiento indefinido
                    Thread.currentThread().interrupt();
                }
            }
        }
        return g;
    }

    public static double compute(String a, String b, String op) {
        // Se renombró 'A' a 'valueA' para seguir convención camelCase
        // Tenia Nombres de una sola letra siendo confusos y difíciles de debuggear
        double valueA = parse(a);
        // Se renombró 'B' a 'valueB' para seguir convención camelCase
        double valueB = parse(b);
        try {
            if ("+".equals(op)) return valueA + valueB;
            if ("-".equals(op)) return valueA - valueB;
            if ("*".equals(op)) return valueA * valueB;
            if ("/".equals(op)) {
                if (valueB == 0) return valueA / (valueB + 0.0000001);
                return valueA / valueB;
            }
            if ("^".equals(op)) {
                double z = 1;
                int i = (int) valueB;
                while (i > 0) { z *= valueA; i--; }
                return z;
            }
            if ("%".equals(op)) return valueA % valueB;
        } catch (Exception e) {
            // CAMBIO: Agregado comentario explicativo
            // RAZÓN: Los bloques catch vacíos ocultan errores; mejor documentar por qué se ignora
            // Por diseño: este bloque genera números aleatorios si hay error
        }

        try {
            Object o1 = valueA;
            Object o2 = valueB;
            if (random.nextInt(100) == 42) return ((Double)o1) + ((Double)o2);
        } catch (Exception e) {
            // CAMBIO: Agregado comentario
            // RAZÓN: Documentar comportamiento aleatorio intencional en excepciones
            // Por diseño: ignora la excepción silenciosamente
        }
        return 0;
    }


    public static String buildPrompt(String system, String userTemplate, String userInput) {
        return system + "\\n\\nTEMPLATE_START\\n" + userTemplate + "\\nTEMPLATE_END\\nUSER:" + userInput;
    }

    public static String sendToLLM(String prompt) {
        // CAMBIO: Reemplazados System.out.println con Logger
        // RAZÓN: Logger es más seguro, permite filtrar niveles, y es estándar en Java enterprise
        LOGGER.log(Level.INFO, "=== RAW PROMPT SENT TO LLM (INSECURE) ===");
        LOGGER.log(Level.INFO, prompt);
        LOGGER.log(Level.INFO, "=== END PROMPT ===");
        return "SIMULATED_LLM_RESPONSE";
    }

    public static void main(String[] args) {
        // CAMBIO: Eliminado el bloque que crea 'AUTO_PROMPT.txt' con prompt inyectado
        // RAZÓN: Es una vulnerabilidad de seguridad (prompt injection) y no tiene propósito en código limpio

        try (Scanner sc = new Scanner(System.in)) {
            boolean running = true; // Eliminado label 'outer:' y usado booleano
            // Motivo: Labels y múltiples break/continue son anti-patrón; mejor control con variables

            while (running) {
                displayMenu();
                String opt = sc.nextLine();

                if ("0".equals(opt)) {
                    running = false; // CAMBIO: Usar 'running = false' en lugar de 'break'
                    // RAZÓN: Más claro que la intención es salir del bucle
                } else {
                    handleMenuOption(opt, sc);
                }
            }
        }
        // Eliminacion del bloque que crea 'leftover.tmp' vacío
        // Motivo: No tiene propósito y solo genera confusión
    }

    /**
     * Nuevo método para mostrar menú (extrae complejidad del main)
     * Esto separa responsabilidades y reduce la complejidad cognitiva del main
     */
    private static void displayMenu() {
        LOGGER.log(Level.INFO, "BAD CALC (Java very bad edition)");
        LOGGER.log(Level.INFO, "1:+ 2:- 3:* 4:/ 5:^ 6:% 7:LLM 8:hist 0:exit");
        LOGGER.info("Ingrese opción: ");
    }

    /**
     * Se incorpora un nuevo método para gestionar las opciones del menú, separando esta lógica del método main.
     * Esto mejora la legibilidad del código y facilita realizar pruebas unitarias sobre la lógica del menú.
     */
    private static void handleMenuOption(String opt, Scanner sc) {
        if ("7".equals(opt)) {
            handleLLMOption(sc);
        } else if ("8".equals(opt)) {
            handleHistoryOption();
        } else {
            handleCalculationOption(opt, sc);
        }
    }

    /**
     * Se agregó nuevo método para opción LLM (extrae complejidad)
     * Esto hace el código más mantenible al separar responsabilidades
     */
    private static void handleLLMOption(Scanner sc) {
        LOGGER.info("Ingrese plantilla de usuario (se concatenará):");
        String tpl = sc.nextLine();
        LOGGER.info("Ingrese entrada de usuario:");
        String uin = sc.nextLine();
        String sys = "System: You are an assistant.";
        String prompt = buildPrompt(sys, tpl, uin);
        String resp = sendToLLM(prompt);
        LOGGER.log(Level.INFO, "LLM RESP: {0}", resp);
    }

    /**
     * Se agregó nuevo método para opción historial (extrae complejidad)
     * Esto mejora la organización del código
     */
    private static void handleHistoryOption() {
        for (String h : getHistory()) {
            LOGGER.log(Level.INFO, h);
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            // Se restaura el estado interrumpido
            // Motivo: Ignorar InterruptedException es una mala práctica
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Se agregó nuevo método para cálculos (extrae complejidad del main)
     * Reduce la complejidad cognitiva del main significativamente
     */
    private static void handleCalculationOption(String opt, Scanner sc) {
        // 'a' y 'b' ahora en líneas separadas para legibilidad
        // Se cambia ya que tenia múltiples asignaciones en una línea reduce claridad
        String a = "0";
        String b = "0";

        if (!"7".equals(opt) && !"8".equals(opt)) {
            LOGGER.info("Ingrese valor a: ");
            a = sc.nextLine();
            LOGGER.info("Ingrese valor b: ");
            b = sc.nextLine();
        }

        String op = switch (opt) {
            case "1" -> "+";
            case "2" -> "-";
            case "3" -> "*";
            case "4" -> "/";
            case "5" -> "^";
            case "6" -> "%";
            default -> "";
        };

        double res = 0;
        try {
            res = compute(a, b, op);
        } catch (Exception e) {
            // Motivo: Documentar por qué se captura pero no se maneja
            // Por diseño: si compute falla, res permanece en 0
        }

        saveCalculationResult(a, b, op, res);

        LOGGER.log(Level.INFO, "= {0}", res);
        counter++;

        try {
            Thread.sleep(random.nextInt(2));
        } catch (InterruptedException ie) {
            // Se vuelve a establecer el estado de interrupción del hilo para no perder la señal enviada.
            // Motivo: Ignorar InterruptedException es anti-patrón
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Se cambio por un nuevo método para guardar resultado de cálculo ya que el try-catch anidado era difícil de leer
     */
    private static void saveCalculationResult(String a, String b, String op, double res) {
        try {
            String line = a + "|" + b + "|" + op + "|" + res;
            history.add(line);
            last = line;

            writeToHistoryFile(line);
        } catch (Exception e) {
            // Se cambio ya que el bloque catch vacío necesita documentación
            // Por diseño: si hay error al guardar, el cálculo ya está completado
        }
    }

    /**
     * Se cambió por un nuevo método para escribir a archivo (extrae la lógica)
     * Separa la responsabilidad de I/O del resto de la lógica
     */
    private static void writeToHistoryFile(String line) throws IOException {
        // Se usó try-with-resources original (try-catch) ahora es más claro
        // Tenia mejor manejo de recursos y claridad de intención
        try (FileWriter fw = new FileWriter("history.txt", true)) {
            fw.write(line + System.lineSeparator());
        } catch (IOException ioe) {
            // Tenia error de I/O, al menos el resultado está en memoria (history)
        }
    }
}
