package test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Libro {

    private String isbn;
    private String titulo;
    private String autor;
    private String editorial;
    private int anioPublicacion;

    public Libro(String isbn, String titulo, String autor, String editorial, int anioPublicacion) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getEditorial() {
        return editorial;
    }

    public int getAnioPublicacion() {
        return anioPublicacion;
    }

    @Override
    public String toString() {
        return "ISBN: " + isbn +
                "\nTítulo: " + titulo +
                "\nAutor: " + autor +
                "\nEditorial: " + editorial +
                "\nAño de publicación: " + anioPublicacion;
    }

    public static void agregar(Scanner sc, Connection conexion) {
        String isbn = Main.leerTexto(sc, "ISBN: ");

        try {
            if (buscarPorIsbn(conexion, isbn) != null) {
                System.out.println("Ya existe un libro con ese ISBN.");
                return;
            }

            String titulo = Main.leerTexto(sc, "Título: ");
            String autor = Main.leerTexto(sc, "Autor: ");
            String editorial = Main.leerTexto(sc, "Editorial: ");
            int anio = Main.leerEntero(sc, "Año de publicación: ");

            String sql = "INSERT INTO LIBRO (ISBN, titulo, autor, editorial, anio_publicacion) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement instruccion = conexion.prepareStatement(sql)) {
                instruccion.setString(1, isbn);
                instruccion.setString(2, titulo);
                instruccion.setString(3, autor);
                instruccion.setString(4, editorial);
                instruccion.setInt(5, anio);

                int resultado = instruccion.executeUpdate();

                if (resultado == 1) {
                    System.out.println("Libro agregado correctamente.");
                } else {
                    System.out.println("No se pudo agregar el libro.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al agregar el libro: " + e.getMessage());
        }
    }

    public static void listar(Connection conexion) {
        String sql = "SELECT ISBN, titulo, autor FROM LIBRO";

        try (PreparedStatement instruccion = conexion.prepareStatement(sql);
             ResultSet conjuntoResultados = instruccion.executeQuery()) {

            System.out.printf("%n%-18s%-30s%s%n", "ISBN", "TÍTULO", "AUTOR");
            System.out.println("----------------------------------------------------------------------");

            while (conjuntoResultados.next()) {
                System.out.printf("%-18s%-30s%s%n",
                        conjuntoResultados.getString("ISBN"),
                        conjuntoResultados.getString("titulo"),
                        conjuntoResultados.getString("autor"));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar los libros: " + e.getMessage());
        }
    }

    public static void buscar(Scanner sc, Connection conexion) {
        String isbn = Main.leerTexto(sc, "Ingrese el ISBN: ");

        try {
            Libro libro = buscarPorIsbn(conexion, isbn);

            if (libro == null) {
                System.out.println("No se encontró un libro con ese ISBN.");
            } else {
                System.out.println("\n" + libro);
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar el libro: " + e.getMessage());
        }
    }

    public static void modificar(Scanner sc, Connection conexion) {
        String isbn = Main.leerTexto(sc, "Ingrese el ISBN del libro a modificar: ");

        try {
            Libro libro = buscarPorIsbn(conexion, isbn);

            if (libro == null) {
                System.out.println("No se encontró un libro con ese ISBN.");
                return;
            }

            System.out.println("\nDatos actuales:\n" + libro);
            System.out.println("\nIngrese los nuevos datos:");

            String titulo = Main.leerTexto(sc, "Título: ");
            String autor = Main.leerTexto(sc, "Autor: ");
            String editorial = Main.leerTexto(sc, "Editorial: ");
            int anio = Main.leerEntero(sc, "Año de publicación: ");

            String sql = "UPDATE LIBRO SET titulo = ?, autor = ?, editorial = ?, anio_publicacion = ? WHERE ISBN = ?";

            try (PreparedStatement instruccion = conexion.prepareStatement(sql)) {
                instruccion.setString(1, titulo);
                instruccion.setString(2, autor);
                instruccion.setString(3, editorial);
                instruccion.setInt(4, anio);
                instruccion.setString(5, isbn);

                int resultado = instruccion.executeUpdate();

                if (resultado == 1) {
                    System.out.println("Libro modificado correctamente.");
                } else {
                    System.out.println("No se pudo modificar el libro.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al modificar el libro: " + e.getMessage());
        }
    }

    public static void eliminar(Scanner sc, Connection conexion) {
        String isbn = Main.leerTexto(sc, "Ingrese el ISBN del libro a eliminar: ");

        try {
            Libro libro = buscarPorIsbn(conexion, isbn);

            if (libro == null) {
                System.out.println("No se encontró un libro con ese ISBN.");
                return;
            }

            if (contar(conexion, "SELECT COUNT(*) FROM PRESTAMO WHERE ISBN = ?", isbn) > 0) {
                System.out.println("No se puede eliminar \"" + libro.getTitulo() + "\" porque tiene préstamos registrados.");
                return;
            }

            int ejemplares = contar(conexion, "SELECT COUNT(*) FROM EJEMPLAR WHERE ISBN = ?", isbn);
            if (ejemplares > 0) {
                System.out.println("Atención: también se eliminarán sus " + ejemplares + " ejemplar(es).");
            }

            String respuesta = Main.leerTexto(sc, "¿Está seguro de eliminar el libro \"" + libro.getTitulo() + "\"? (S/N): ");

            if (!respuesta.equalsIgnoreCase("S")) {
                System.out.println("Operación cancelada.");
                return;
            }

            String sql = "DELETE FROM LIBRO WHERE ISBN = ?";

            try (PreparedStatement instruccion = conexion.prepareStatement(sql)) {
                instruccion.setString(1, isbn);

                int resultado = instruccion.executeUpdate();

                if (resultado == 1) {
                    System.out.println("Libro eliminado correctamente.");
                } else {
                    System.out.println("No se pudo eliminar el libro.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar el libro: " + e.getMessage());
        }
    }

    private static int contar(Connection conexion, String sql, String isbn) throws SQLException {
        try (PreparedStatement instruccion = conexion.prepareStatement(sql)) {
            instruccion.setString(1, isbn);

            try (ResultSet conjuntoResultados = instruccion.executeQuery()) {
                conjuntoResultados.next();
                return conjuntoResultados.getInt(1);
            }
        }
    }

    public static Libro buscarPorIsbn(Connection conexion, String isbn) throws SQLException {
        String sql = "SELECT * FROM LIBRO WHERE ISBN = ?";

        try (PreparedStatement instruccion = conexion.prepareStatement(sql)) {
            instruccion.setString(1, isbn);

            try (ResultSet conjuntoResultados = instruccion.executeQuery()) {
                if (conjuntoResultados.next()) {
                    return new Libro(conjuntoResultados.getString("ISBN"),
                            conjuntoResultados.getString("titulo"),
                            conjuntoResultados.getString("autor"),
                            conjuntoResultados.getString("editorial"),
                            conjuntoResultados.getInt("anio_publicacion"));
                }
            }
        }

        return null;
    }
}
