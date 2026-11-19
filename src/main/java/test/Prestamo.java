package test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Prestamo {

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
