package test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try (Connection conexion = Conexion.conectar()) {

            if (conexion == null) {
                System.out.println("No se puede continuar sin conexión a la base de datos.");
                return;
            }   

            int opcion;

            do {
                System.out.println("\n====================================");
                System.out.println("       SISTEMA DE BIBLIOTECA");
                System.out.println("====================================\n");
                System.out.println("1. Gestionar libros");
                System.out.println("2. Gestionar socios");
                System.out.println("3. Gestionar ejemplares");
                System.out.println("4. Gestionar préstamos");
                System.out.println("0. Salir");
                System.out.print("\nSeleccione una opción: ");
                opcion = leerOpcion(sc);

                switch (opcion) {
                    case 1:
                        menuLibros(sc, conexion);
                        break;
                    case 2:
                        menuSocios(sc, conexion);
                        break;
                    case 3:
                        menuEjemplares(sc, conexion);
                        break;
                    case 4:
                        menuPrestamos(sc, conexion);
                        break;
                    case 0:
                        System.out.println("Saliendo del sistema...");
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
            } while (opcion != 0);

        } catch (SQLException e) {
            System.out.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    private static void menuLibros(Scanner sc, Connection conexion) {
        int opcion;

        do {
            System.out.println("\n------- GESTIÓN DE LIBROS -------");
            System.out.println("1. Agregar libro");
            System.out.println("2. Listar libros");
            System.out.println("3. Buscar libro");
            System.out.println("4. Modificar libro");
            System.out.println("5. Eliminar libro");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");
            opcion = leerOpcion(sc);

            switch (opcion) {
                case 1:
                    Libro.agregar(sc, conexion);
                    break;
                case 2:
                    Libro.listar(conexion);
                    break;
                case 3:
                    Libro.buscar(sc, conexion);
                    break;
                case 4:
                    Libro.modificar(sc, conexion);
                    break;
                case 5:
                    Libro.eliminar(sc, conexion);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private static void menuSocios(Scanner sc, Connection conexion) {
        int opcion;

        do {
            System.out.println("\n------- GESTIÓN DE SOCIOS -------");
            System.out.println("1. Agregar socio");
            System.out.println("2. Listar socios");
            System.out.println("3. Buscar socio");
            System.out.println("4. Modificar socio");
            System.out.println("5. Eliminar socio");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");
            opcion = leerOpcion(sc);

            switch (opcion) {
                case 1:
                    Socio.agregar(sc, conexion);
                    break;
                case 2:
                    Socio.listar(conexion);
                    break;
                case 3:
                    Socio.buscar(sc, conexion);
                    break;
                case 4:
                    Socio.modificar(sc, conexion);
                    break;
                case 5:
                    Socio.eliminar(sc, conexion);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private static void menuEjemplares(Scanner sc, Connection conexion) {
        int opcion;

        do {
            System.out.println("\n------- GESTIÓN DE EJEMPLARES -------");
            System.out.println("1. Agregar ejemplar");
            System.out.println("2. Listar ejemplares de un libro");
            System.out.println("3. Modificar estado");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");
            opcion = leerOpcion(sc);

            switch (opcion) {
                case 1:
                    Ejemplar.agregar(sc, conexion);
                    break;
                case 2:
                case 3:
                    System.out.println("(pendiente)");
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private static void menuPrestamos(Scanner sc, Connection conexion) {
        int opcion;

        do {
            System.out.println("\n------- GESTIÓN DE PRÉSTAMOS -------");
            System.out.println("1. Registrar préstamo");
            System.out.println("2. Registrar devolución");
            System.out.println("3. Listar préstamos");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");
            opcion = leerOpcion(sc);

            switch (opcion) {
                case 1:
                case 2:
                case 3:
                    System.out.println("(pendiente)");
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    static String leerTexto(Scanner sc, String mensaje) {
        String texto;

        do {
            System.out.print(mensaje);
            texto = sc.nextLine().trim();
            if (texto.isBlank()) {
                System.out.println("El dato no puede estar vacío.");
            }
        } while (texto.isBlank());

        return texto;
    }

    static int leerEntero(Scanner sc, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                int numero = sc.nextInt();
                sc.nextLine();
                return numero;
            } catch (InputMismatchException e) {
                sc.nextLine();
                System.out.println("Debe ingresar un número.");
            }
        }
    }

    static LocalDate leerFecha(Scanner sc, String mensaje) {
        while (true) {
            String texto = leerTexto(sc, mensaje);
            try {
                return LocalDate.parse(texto, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("Fecha inválida. Use el formato dd/mm/aaaa.");
            }
        }
    }

    private static int leerOpcion(Scanner sc) {
        try {
            int opcion = sc.nextInt();
            sc.nextLine();
            return opcion;
        } catch (InputMismatchException e) {
            sc.nextLine();
            return -1;
        }
    }
}
