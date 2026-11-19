package test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Ejemplar {

    static final String DISPONIBLE = "Disponible";
    static final String PRESTADO = "Prestado";

    private String isbn;
    private int nroEjemplar;
    private String estado;

    public Ejemplar(String isbn, int nroEjemplar, String estado) {
        this.isbn = isbn;
        this.nroEjemplar = nroEjemplar;
        this.estado = estado;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getNroEjemplar() {
        return nroEjemplar;
    }

    public String getEstado() {
        return estado;
    }

    public static void agregar(Scanner sc, Connection conexion) {
        String isbn = Main.leerTexto(sc, "ISBN del libro: ");

        try {
            Libro libro = Libro.buscarPorIsbn(conexion, isbn);

            if (libro == null) {
                System.out.println("No se encontró un libro con ese ISBN.");
                return;
            }

            int nroEjemplar = Main.leerEntero(sc, "Número de ejemplar: ");

            if (nroEjemplar <= 0) {
                System.out.println("El número de ejemplar debe ser mayor a 0.");
                return;
            }

            if (buscar(conexion, isbn, nroEjemplar) != null) {
                System.out.println("Ya existe el ejemplar " + nroEjemplar + " de \"" + libro.getTitulo() + "\".");
                return;
            }

            String sql = "INSERT INTO EJEMPLAR (ISBN, nro_ejemplar, estado) VALUES (?, ?, ?)";

            try (PreparedStatement instruccion = conexion.prepareStatement(sql)) {
                instruccion.setString(1, isbn);
                instruccion.setInt(2, nroEjemplar);
                instruccion.setString(3, DISPONIBLE);

                int resultado = instruccion.executeUpdate();

                if (resultado == 1) {
                    System.out.println("Ejemplar agregado correctamente.");
                } else {
                    System.out.println("No se pudo agregar el ejemplar.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al agregar el ejemplar: " + e.getMessage());
        }
    }

    public static void listarPorLibro(Scanner sc, Connection conexion) {
        String isbn = Main.leerTexto(sc, "ISBN del libro: ");

        try {
            Libro libro = Libro.buscarPorIsbn(conexion, isbn);

            if (libro == null) {
                System.out.println("No se encontró un libro con ese ISBN.");
                return;
            }

            String sql = "SELECT nro_ejemplar, estado FROM EJEMPLAR WHERE ISBN = ?";

            try (PreparedStatement instruccion = conexion.prepareStatement(sql)) {
                instruccion.setString(1, isbn);

                try (ResultSet conjuntoResultados = instruccion.executeQuery()) {
                    System.out.println("\nLibro: " + libro.getTitulo() + "\n");
                    System.out.printf("%-13s%s%n", "Ejemplar", "Estado");
                    System.out.println("-------------------------");

                    boolean hayEjemplares = false;

                    while (conjuntoResultados.next()) {
                        hayEjemplares = true;
                        System.out.printf("%-13d%s%n",
                                conjuntoResultados.getInt("nro_ejemplar"),
                                conjuntoResultados.getString("estado"));
                    }

                    if (!hayEjemplares) {
                        System.out.println("El libro no tiene ejemplares registrados.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar los ejemplares: " + e.getMessage());
        }
    }

    public static Ejemplar buscar(Connection conexion, String isbn, int nroEjemplar) throws SQLException {
        String sql = "SELECT * FROM EJEMPLAR WHERE ISBN = ? AND nro_ejemplar = ?";

        try (PreparedStatement instruccion = conexion.prepareStatement(sql)) {
            instruccion.setString(1, isbn);
            instruccion.setInt(2, nroEjemplar);

            try (ResultSet conjuntoResultados = instruccion.executeQuery()) {
                if (conjuntoResultados.next()) {
                    return new Ejemplar(conjuntoResultados.getString("ISBN"),
                            conjuntoResultados.getInt("nro_ejemplar"),
                            conjuntoResultados.getString("estado"));
                }
            }
        }

        return null;
    }
}
