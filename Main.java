import java.util.Scanner;
public class Main {
    private static double[][] tableau;
    private static double[][] tableau_1;
    private static int numVariables;
    private static int numVariables_1;
    private static int numConstraints;
    private static int numConstraints_1;
    private static double[][] prev_pivotColumnIndex;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Ввод количества переменных и ограничений
        System.out.print("Введите количество переменных: ");
        numVariables = scanner.nextInt();
        System.out.print("Введите количество ограничений: ");
        numConstraints = scanner.nextInt();

        // Инициализация таблицы
        tableau = new double[numConstraints + 2][numVariables + numConstraints + 2];

        // Ввод коэффициентов целевой функции
        System.out.println("Введите коэффициенты целевой функции:");
        for (int j = 0; j < numVariables; j++) {
            System.out.print("x" + (j + 1) + ": ");
            tableau[0][j] = scanner.nextDouble();
        }
        System.out.print("Введите свободный член целевой функции: ");
        tableau[0][numVariables + numConstraints] = scanner.nextDouble();

        // Ввод коэффициентов ограничений
        for (int i = 1; i <= numConstraints; i++) {
            System.out.println("Введите коэффициенты " + i + "-ого ограничения:");
            for (int j = 0; j < numVariables; j++) {
                System.out.print("x" + (j + 1) + ": ");
                tableau[i][j] = scanner.nextDouble();
            }
            System.out.print("Введите свободный член " + i + "-ого ограничения: ");
            tableau[i][numVariables + numConstraints] = scanner.nextDouble();
        }

        int choose_method = 1;

        while (choose_method != 0) {
            System.out.println("Выберите каким методом хотите решить ЗЛП: (1)-прямым/(2)-двойственным/(0)-выйти из программы:");
            choose_method = scanner.nextInt();
            if (choose_method == 1) {
                for (int i = 1; i <= numConstraints; i++)
                    tableau[i][numVariables + i - 1] = 1;

                solve();
            } else if (choose_method == 2) {
                numVariables_1 = numConstraints;
                numConstraints_1 = numVariables;

                // Инициализация таблицы
                tableau_1 = new double[numConstraints_1 + 3][numVariables_1 + numConstraints_1 + 1];

                // Коэффициенты целевой функции двойственной задачи
                for (int j = 0; j < numVariables_1; j++)
                    tableau_1[0][j] = tableau[j + 1][numVariables_1 + numConstraints_1];

                tableau_1[0][numVariables_1 + numConstraints_1] = tableau[0][numVariables + numConstraints];

                // Коэффициенты ограничений двойственной задачи
                for (int i = 1; i <= numConstraints_1; i++) {
                    for (int j = 0; j < numVariables_1; j++)
                        tableau_1[i][j] = tableau[j + 1][i - 1];
                    tableau_1[i][numVariables_1 + numConstraints_1] = tableau[0][i - 1];
                }

                for (int i = 0; i < numConstraints_1 + 2; i++)
                    for (int j = 0; j < numVariables_1 + numConstraints_1 + 1; j++) {
                        if (tableau_1[i][j] != 0)
                            tableau_1[i][j] *= -1;
                    }

                for (int i = 1; i <= numConstraints_1; i++)
                    tableau_1[i][numVariables_1 + i - 1] = 1;

                solve_1();
            }
        }
    }

    private static void solve() {
        prev_pivotColumnIndex = new double[numConstraints][1];
        while (true) {
            int pivotColumnIndex;
            pivotColumnIndex = findPivotColumn(prev_pivotColumnIndex);
            if (pivotColumnIndex == -1) {
                printTableau(1);
                // Целевая функция достигла оптимума
                break;
            }

            int pivotRowIndex;
            pivotRowIndex = findPivotRow(pivotColumnIndex);
            if (pivotRowIndex == -1) {
                System.out.println("Задача не имеет ограничений");
                System.exit(0);
                break;
            }

            printTableau(1);
            printSolution(false, 1);

            // Обновление таблицы с помощью операции пересчёта
            tableau = updateTableau(pivotRowIndex, pivotColumnIndex, 1);

            prev_pivotColumnIndex[pivotRowIndex - 1][0] = tableau[0][pivotColumnIndex];

            System.out.print("Базисные переменные: (");
            int k = 0;
            for (double[] row_index : prev_pivotColumnIndex) {
                for (double index : row_index)
                    System.out.print(index);
                if (k <= row_index.length)
                    System.out.print("; ");
                k++;
            }
            System.out.println(")");
        }
        printSolution(true, 1);
    }

    private static void solve_1() {
        prev_pivotColumnIndex = new double[numConstraints_1][1];
        while (true) {
            int pivotRowIndex = findPivotRow_1();
            if (pivotRowIndex == -1) {
                findPivotColumn_1(prev_pivotColumnIndex, pivotRowIndex, true);
                printTableau(2);
                // Целевая функция достигла оптимума
                break;
            }

            int pivotColumnIndex = findPivotColumn_1(prev_pivotColumnIndex, pivotRowIndex, false);
            if (pivotColumnIndex == -1) {
                System.out.println("Задача не имеет ограничений");
                System.exit(0);
            }
            printTableau(2);
            printSolution(false, 2);

            // Обновление таблицы с помощью операции пересчёта
            tableau_1 = updateTableau(pivotRowIndex, pivotColumnIndex, 2);

            prev_pivotColumnIndex[pivotRowIndex - 1][0] = tableau_1[0][pivotColumnIndex];

            System.out.print("Базисные переменные: (");
            int k = 0;
            for (double[] row_index : prev_pivotColumnIndex) {
                for (double index : row_index)
                        System.out.print(index);
                if (k <= row_index.length)
                    System.out.print("; ");
                k++;
            }
            System.out.println(")");
        }
        printSolution(true, 2);
    }

    private static int findPivotColumn(double[][] prev_pivotColumnIndex) {
        int pivotColumnIndex = -1;
        double minCoefficient = 0;
        for (int j = 0; j < numVariables + numConstraints + 1; j++) {
            tableau[numConstraints + 1][j] = 0;
            for (int i = 1; i <= numConstraints; i++)
                tableau[numConstraints + 1][j] += prev_pivotColumnIndex[i - 1][0] * tableau[i][j];
            tableau[numConstraints + 1][j] -= tableau[0][j];

            if (tableau[numConstraints + 1][j] < minCoefficient) {
                minCoefficient = tableau[numConstraints + 1][j];
                pivotColumnIndex = j;
            }
        }
        return pivotColumnIndex;
    }

    private static int findPivotRow(int pivotColumnIndex) {
        int pivotRowIndex = -1;
        double minRatio = Double.MAX_VALUE;

        for (int i = 1; i <= numConstraints; i++)
            if (tableau[i][pivotColumnIndex] > 0) {
                tableau[i][numVariables + numConstraints + 1] = tableau[i][numVariables + numConstraints] / tableau[i][pivotColumnIndex];
                if (tableau[i][numVariables + numConstraints + 1] < minRatio) {
                    minRatio = tableau[i][numVariables + numConstraints + 1];
                    pivotRowIndex = i;
                }
            }

        return pivotRowIndex;
    }

    private static int findPivotRow_1() {
        int pivotRowIndex = -1;
        double minElement = 0;
        for (int i = 1; i < numConstraints_1 + 1; i++)
            if (tableau_1[i][numConstraints_1 + numVariables_1] < 0)
                if (tableau_1[i][numConstraints_1 + numVariables_1] < minElement) {
                    minElement = tableau_1[i][numConstraints_1 + numVariables_1];
                    pivotRowIndex = i;
                }
        return pivotRowIndex;
    }

    private static int findPivotColumn_1(double[][] prev_pivotColumnIndex, int pivotRowIndex, boolean end) {
        int pivotColumnIndex = -1;
        double minValue = -Double.MAX_VALUE;
        for (int j = 0; j < numVariables_1 + numConstraints_1 + 1; j++) {
            tableau_1[numConstraints_1 + 1][j] = 0;
            for (int i = 1; i <= numConstraints_1; i++)
                tableau_1[numConstraints_1 + 1][j] += prev_pivotColumnIndex[i - 1][0] * tableau_1[i][j];
            tableau_1[numConstraints_1 + 1][j] -= tableau_1[0][j];

            if (!end) {
                if (tableau_1[pivotRowIndex][j] < 0)
                    tableau_1[numConstraints_1 + 2][j] = tableau_1[numConstraints_1 + 1][j] / tableau_1[pivotRowIndex][j];
                else
                    tableau_1[numConstraints_1 + 2][j] = 0;

                if (tableau_1[numConstraints_1 + 2][j] < 0)
                    if (tableau_1[numConstraints_1 + 2][j] > minValue) {
                        minValue = tableau_1[numConstraints_1 + 2][j];
                        pivotColumnIndex = j;
                    }
            }
        }
        return pivotColumnIndex;
    }

    private static double[][] updateTableau(int pivotRowIndex, int pivotColumnIndex, int choose_method) {
        // Деление опорной строки на опорный элемент
        double[][] table_update;
        int updateVariables;
        int updateConstraints;
        if (choose_method == 1) {
            updateVariables = numVariables;
            updateConstraints = numConstraints;
            table_update = tableau;
        } else {
            updateVariables = numVariables_1;
            updateConstraints = numConstraints_1;
            table_update = tableau_1;
        }

        double pivotElement = table_update[pivotRowIndex][pivotColumnIndex];
        for (int j = 0; j <= updateVariables + updateConstraints; j++)
            table_update[pivotRowIndex][j] /= pivotElement;

        // Обновление остальных строк таблицы
        for (int i = 1; i <= updateConstraints; i++)
            if (i != pivotRowIndex) {
                double ratio = table_update[i][pivotColumnIndex];
                for (int j = 0; j <= updateVariables + updateConstraints; j++)
                    table_update[i][j] -= ratio * table_update[pivotRowIndex][j] / table_update[pivotRowIndex][pivotColumnIndex];
            }
        return table_update;
    }

    private static void printTableau(int choose_method) {
        System.out.println("\nТекущая симплекс-таблица:");
        String[] basis;
        if (choose_method == 1) {
            basis = new String[numVariables + numConstraints + 2];
            for (int i = 0; i < numVariables + numConstraints + 2; i++)
                basis[i] = String.format("%10s", "A" + (i + 1));
            basis[numVariables + numConstraints] = String.format("%10s", "A" + 0);
            basis[numVariables + numConstraints + 1] = String.format("%10s", "Тетта");
        } else {
            basis = new String[numVariables_1 + numConstraints_1 + 1];
            for (int i = 0; i < numVariables_1 + numConstraints_1 + 1; i++)
                basis[i] = String.format("%10s", "A" + (i + 1));
            basis[numVariables_1 + numConstraints_1] = String.format("%10s", "A" + 0);
        }
        for (String vector : basis)
            System.out.print("|" + vector + "|");
        System.out.println();

        //Вывод симплекс-таблицы в зависимости от метода
        double[][] output_table;
        if (choose_method == 1)
            output_table = tableau;
        else
            output_table = tableau_1;

        int k = 0;
        for (double[] row : output_table) {
            if (k <= 1) {
                for (int i = 0; i < row.length; i++)
                    System.out.print("------------");
                System.out.println();
            }
            for (double value : row) {
                System.out.print("|");
                System.out.printf("%10.2f", value);
                System.out.print("|");
            }
            System.out.println();
            k++;
        }
    }

    private static void printSolution(boolean end, int choose_method) {
        if (end)
            System.out.println("Оптимальное решение:");
        else
            System.out.print("Базисное решение: (");

        double[][] table_solution;
        int solutionVariables;
        int solutionConstraints;
        if (choose_method == 1) {
            solutionVariables = numVariables;
            solutionConstraints = numConstraints;
            table_solution = tableau;
        } else {
            solutionVariables = numVariables_1;
            solutionConstraints = numConstraints_1;
            table_solution = tableau_1;
        }

        for (int j = 0; j < solutionVariables + solutionConstraints; j++) {
            boolean isBasicVariable = true;
            int basicRowIndex = -1;

            for (int i = 1; i <= solutionConstraints; i++) {
                if (table_solution[i][j] == 1 && basicRowIndex == -1)
                    basicRowIndex = i;
                else if (table_solution[i][j] != 0) {
                    isBasicVariable = false;
                    break;
                }
            }

            if (end) {
                if (isBasicVariable && basicRowIndex != -1)
                    System.out.printf("x%d: %.2f\n", j + 1, table_solution[basicRowIndex][solutionVariables + solutionConstraints]);
                else
                    System.out.printf("x%d: 0\n", j + 1);
            } else {
                if (isBasicVariable && basicRowIndex != -1)
                    System.out.print(table_solution[basicRowIndex][solutionVariables + solutionConstraints]);
                else
                    System.out.print(0);
                if (j < solutionVariables + solutionConstraints - 1)
                    System.out.print("; ");
            }
        }
        if (choose_method == 1) {
            if (end)
                System.out.printf("Значение целевой функции: %.2f\n", table_solution[solutionConstraints + 1][solutionVariables + solutionConstraints]);
            else {
                System.out.print(")");
                System.out.println();
            }
        } else {
            if (end) {
                System.out.printf("Значение целевой функции G_max: %.2f\n", table_solution[solutionConstraints + 1][solutionVariables + solutionConstraints]);
                System.out.printf("Значение целевой функции G_min: %.2f\n", -table_solution[solutionConstraints + 1][solutionVariables + solutionConstraints]);
            } else {
                System.out.print(")");
                System.out.println();
            }
        }
    }
}