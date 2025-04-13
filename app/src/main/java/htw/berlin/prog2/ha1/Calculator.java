package htw.berlin.prog2.ha1;

/**
 * Eine Klasse, die das Verhalten des Online Taschenrechners imitiert, welcher auf
 * https://www.online-calculator.com/ aufgerufen werden kann (ohne die Memory-Funktionen)
 * und dessen Bildschirm bis zu zehn Ziffern plus einem Dezimaltrennzeichen darstellen kann.
 * Enthält mit Absicht noch diverse Bugs oder unvollständige Funktionen.
 */
public class Calculator {

    private String screen = "0";

    private double acc = 0.0;
    private String pendingAddOp = "";

    private double pendingMul = 0.0;
    private String pendingMulOp = "";

    private double lastOperand = 0.0;
    private String lastOperator = "";

    private boolean justEquals = false;
    private boolean newEntry = false;

    /**
     * @return den aktuellen Bildschirminhalt als String
     */
    public String readScreen() {

        return screen;

    }

    /**
     * Empfängt den Wert einer gedrückten Zifferntaste. Da man nur eine Taste auf einmal
     * drücken kann muss der Wert positiv und einstellig sein und zwischen 0 und 9 liegen.
     * Führt in jedem Fall dazu, dass die gerade gedrückte Ziffer auf dem Bildschirm angezeigt
     * oder rechts an die zuvor gedrückte Ziffer angehängt angezeigt wird.
     * @param digit Die Ziffer, deren Taste gedrückt wurde
     */
    public void pressDigitKey(int digit) {

        if (digit < 0 || digit > 9)

            throw new IllegalArgumentException();

        if (justEquals) {

            return;

        }

        if (newEntry || screen.equals("0")) {

            screen = "";
            newEntry = false;

        }

        screen += digit;

    }

    /**
     * Empfängt den Befehl der C- bzw. CE-Taste (Clear bzw. Clear Entry).
     * Einmaliges Drücken der Taste löscht die zuvor eingegebenen Ziffern auf dem Bildschirm
     * so dass "0" angezeigt wird, jedoch ohne zuvor zwischengespeicherte Werte zu löschen.
     * Wird daraufhin noch einmal die Taste gedrückt, dann werden auch zwischengespeicherte
     * Werte sowie der aktuelle Operationsmodus zurückgesetzt, so dass der Rechner wieder
     * im Ursprungszustand ist.
     */
    public void pressClearKey() {

        screen = "0";
        acc = 0.0;
        pendingAddOp = "";
        pendingMul = 0.0;
        pendingMulOp = "";
        lastOperand = 0.0;
        lastOperator = "";
        justEquals = false;
        newEntry = false;

    }

    /**
     * Empfängt den Wert einer gedrückten binären Operationstaste, also eine der vier Operationen
     * Addition, Substraktion, Division, oder Multiplikation, welche zwei Operanden benötigen.
     * Beim ersten Drücken der Taste wird der Bildschirminhalt nicht verändert, sondern nur der
     * Rechner in den passenden Operationsmodus versetzt.
     * Beim zweiten Drücken nach Eingabe einer weiteren Zahl wird direkt des aktuelle Zwischenergebnis
     * auf dem Bildschirm angezeigt. Falls hierbei eine Division durch Null auftritt, wird "Error" angezeigt.
     * @param "+" für Addition, "-" für Substraktion, "x" für Multiplikation, "/" für Division
     */
    public void pressBinaryOperationKey(String op) {

        double current = Double.parseDouble(screen);
        justEquals = false;

        if (op.equals("x") || op.equals("/")) {

            if (pendingMulOp.isEmpty()) {

                pendingMul = current;

            } else {

                pendingMul = evaluate(pendingMul, current, pendingMulOp);

            }

            pendingMulOp = op;

        } else {

            if (!pendingMulOp.isEmpty()) {

                current = evaluate(pendingMul, current, pendingMulOp);
                pendingMulOp = "";

            }

            if (!pendingAddOp.isEmpty()) {

                current = evaluate(acc, current, pendingAddOp);

            }
            acc = current;
            pendingAddOp = op;

            lastOperator = op;
            lastOperand = current;
        }

        newEntry = true;

    }


    /**
     * Empfängt den Wert einer gedrückten unären Operationstaste, also eine der drei Operationen
     * Quadratwurzel, Prozent, Inversion, welche nur einen Operanden benötigen.
     * Beim Drücken der Taste wird direkt die Operation auf den aktuellen Zahlenwert angewendet und
     * der Bildschirminhalt mit dem Ergebnis aktualisiert.
     * @param "√" für Quadratwurzel, "%" für Prozent, "1/x" für Inversion
     */
    public void pressUnaryOperationKey(String op) {

        double value = Double.parseDouble(screen);
        double result;

        switch (op) {

            case "√":

                result = Math.sqrt(value);
                break;

            case "%":

                result = value / 100;
                break;

            case "1/x":

                result = 1 / value;
                break;

            default:

                throw new IllegalArgumentException();

        }

        screen = formatResult(result);
        if (Double.isNaN(result))
            screen = "Error";

        justEquals = true;
        newEntry = true;

    }

    /**
     * Empfängt den Befehl der gedrückten Dezimaltrennzeichentaste, im Englischen üblicherweise "."
     * Fügt beim ersten Mal Drücken dem aktuellen Bildschirminhalt das Trennzeichen auf der rechten
     * Seite hinzu und aktualisiert den Bildschirm. Daraufhin eingegebene Zahlen werden rechts vom
     * Trennzeichen angegeben und daher als Dezimalziffern interpretiert.
     * Beim zweimaligem Drücken, oder wenn bereits ein Trennzeichen angezeigt wird, passiert nichts.
     */
    public void pressDotKey() {

        if (!screen.contains(".")) {

            screen += ".";
            newEntry = false;

        }

    }

    /**
     * Empfängt den Befehl der gedrückten Vorzeichenumkehrstaste ("+/-").
     * Zeigt der Bildschirm einen positiven Wert an, so wird ein "-" links angehängt, der Bildschirm
     * aktualisiert und die Inhalt fortan als negativ interpretiert.
     * Zeigt der Bildschirm bereits einen negativen Wert mit führendem Minus an, dann wird dieses
     * entfernt und der Inhalt fortan als positiv interpretiert.
     */
    public void pressNegativeKey() {

        if (screen.startsWith("-")) {

            screen = screen.substring(1);

        } else {

            screen = "-" + screen;

        }

    }

    /**
     * Empfängt den Befehl der gedrückten "="-Taste.
     * Wurde zuvor keine Operationstaste gedrückt, passiert nichts.
     * Wurde zuvor eine binäre Operationstaste gedrückt und zwei Operanden eingegeben, wird das
     * Ergebnis der Operation angezeigt. Falls hierbei eine Division durch Null auftritt, wird "Error" angezeigt.
     * Wird die Taste weitere Male gedrückt (ohne andere Tasten dazwischen), so wird die letzte
     * Operation (ggf. inklusive letztem Operand) erneut auf den aktuellen Bildschirminhalt angewandt
     * und das Ergebnis direkt angezeigt.
     */
    public void pressEqualsKey() {

        double current = Double.parseDouble(screen);

        if (!pendingMulOp.isEmpty()) {

            current = evaluate(pendingMul, current, pendingMulOp);
            pendingMulOp = "";

        }
        double result = current;

        if (!pendingAddOp.isEmpty()) {

            result = evaluate(acc, current, pendingAddOp);

            lastOperator = pendingAddOp;
            lastOperand = current;
            pendingAddOp = "";
            acc = result;

        } else if (justEquals && !lastOperator.isEmpty()) {

            result = evaluate(result, result, lastOperator);
            acc = result;

        }

        if (Double.isInfinite(result) || Double.isNaN(result)) {

            screen = "Error";

        } else {

            screen = formatResult(result);

        }

        justEquals = true;
        newEntry = true;

    }

    private double evaluate(double a, double b, String op) {

        switch (op) {
            case "+":

                return a + b;

            case "-":

                return a - b;

            case "x":

                return a * b;

            case "/":

                return b == 0 ? Double.POSITIVE_INFINITY : a / b;

            default:

                throw new IllegalArgumentException();

        }

    }

    private String formatResult(double result) {

        String res = Double.toString(result);

        if (res.endsWith(".0")) {

            res = res.substring(0, res.length() - 2);

        }

        if (res.contains(".") && res.length() > 11) {

            res = res.substring(0, 10);

        }

        return res;

    }

}
