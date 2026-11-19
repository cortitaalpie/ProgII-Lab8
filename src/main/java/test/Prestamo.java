package test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Scanner;

public class Prestamo {

    public static void registrar(Scanner sc, Connection conexion) {
        try {
            int idSocio = Main.leerEntero(sc, "ID del socio: ");
            Socio socio = Socio.buscarPorId(conexion, idSocio);
            if (socio == null) {
                System.out.println("No se encontró un socio con ese ID.");
                return;
            }

            String isbn = Main.leerTexto(sc, "ISBN: ");
            Libro libro = Libro.buscarPorIsbn(conexion, isbn);
            if (libro == null) {
                System.out.println("No se encontró un libro con ese ISBN.");
                return;
            }

            int nroEjemplar = Main.leerEntero(sc, "Número de ejemplar: ");
            Ejemplar ejemplar = Ejemplar.buscar(conexion, isbn, nroEjemplar);
            if (ejemplar == null) {
                System.out.println("No se encontró ese ejemplar.");
                return;
            }
            if (!ejemplar.getEstado().equals(Ejemplar.DISPONIBLE)) {
                System.out.println("El ejemplar no está disponible (estado: " + ejemplar.getEstado() + ").");
                return;
            }

            LocalDate fechaPrestamo = Main.leerFecha(sc, "Fecha del préstamo (dd/mm/aaaa): ");
            LocalDate fechaPrevista = Main.leerFecha(sc, "Fecha prevista de devolución (dd/mm/aaaa): ");
            if (fechaPrevista.isBefore(fechaPrestamo)) {
                System.out.println("La fecha prevista de devolución no puede ser anterior a la del préstamo.");
                return;
            }

            String sqlPrestamo = "INSERT INTO PRESTAMO (id_socio, ISBN, nro_ejemplar, fecha_prestamo, fecha_devolucion_prevista) VALUES (?, ?, ?, ?, ?)";
            String sqlEjemplar = "UPDATE EJEMPLAR SET estado = ? WHERE ISBN = ? AND nro_ejemplar = ?";

            // transacción: el INSERT y el UPDATE se guardan juntos o no se guarda ninguno
            conexion.setAutoCommit(false);

            try (PreparedStatement insertarPrestamo = conexion.prepareStatement(sqlPrestamo, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement actualizarEjemplar = conexion.prepareStatement(sqlEjemplar)) {

                insertarPrestamo.setInt(1, idSocio);
                insertarPrestamo.setString(2, isbn);
                insertarPrestamo.setInt(3, nroEjemplar);
                insertarPrestamo.setDate(4, Date.valueOf(fechaPrestamo));
                insertarPrestamo.setDate(5, Date.valueOf(fechaPrevista));
                int filasPrestamo = insertarPrestamo.executeUpdate();

                actualizarEjemplar.setString(1, Ejemplar.PRESTADO);
                actualizarEjemplar.setString(2, isbn);
                actualizarEjemplar.setInt(3, nroEjemplar);
                int filasEjemplar = actualizarEjemplar.executeUpdate();

                if (filasPrestamo == 1 && filasEjemplar == 1) {
                    conexion.commit();
                    try (ResultSet claves = insertarPrestamo.getGeneratedKeys()) {
                        claves.next();
                        System.out.println("Préstamo registrado correctamente con ID " + claves.getInt(1) + ".");
                    }
                } else {
                    conexion.rollback();
                    System.out.println("No se pudo registrar el préstamo. No se guardó ningún cambio.");
                }
            } catch (SQLException e) {
                conexion.rollback();
                System.out.println("Error al registrar el préstamo. No se guardó ningún cambio: " + e.getMessage());
            } finally {
                conexion.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error al registrar el préstamo: " + e.getMessage());
        }
    }

    public static void listar(Connection conexion) {
        String sql = "SELECT p.id_prestamo, s.nombre, l.titulo, p.fecha_prestamo " +
                "FROM PRESTAMO p " +
                "JOIN SOCIO s ON p.id_socio = s.id_socio " +
                "JOIN LIBRO l ON p.ISBN = l.ISBN " +
                "ORDER BY p.id_prestamo";

        try (PreparedStatement instruccion = conexion.prepareStatement(sql);
             ResultSet conjuntoResultados = instruccion.executeQuery()) {

            System.out.printf("%n%-5s%-20s%-30s%s%n", "ID", "SOCIO", "LIBRO", "F. PRÉSTAMO");
            System.out.println("-----------------------------------------------------------------------");

            while (conjuntoResultados.next()) {
                System.out.printf("%-5d%-20s%-30s%s%n",
                        conjuntoResultados.getInt("id_prestamo"),
                        conjuntoResultados.getString("nombre"),
                        conjuntoResultados.getString("titulo"),
                        conjuntoResultados.getDate("fecha_prestamo").toLocalDate().format(Main.FORMATO_FECHA));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar los préstamos: " + e.getMessage());
        }
    }
}
