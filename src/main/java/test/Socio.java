package test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Scanner;

public class Socio {

    private int idSocio;
    private String nombre;
    private String dni;
    private String telefono;
    private LocalDate fechaAlta;

    public Socio(int idSocio, String nombre, String dni, String telefono, LocalDate fechaAlta) {
        this.idSocio = idSocio;
        this.nombre = nombre;
        this.dni = dni;
        this.telefono = telefono;
        this.fechaAlta = fechaAlta;
    }

    public int getIdSocio() {
        return idSocio;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDni() {
        return dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    @Override
    public String toString() {
        return "ID: " + idSocio +
                "\nNombre: " + nombre +
                "\nDNI: " + dni +
                "\nTeléfono: " + telefono +
                "\nFecha de alta: " + fechaAlta.format(Main.FORMATO_FECHA);
    }

    public static void agregar(Scanner sc, Connection conexion) {
        String dni = Main.leerTexto(sc, "DNI: ");

        try {
            if (buscarPorDni(conexion, dni) != null) {
                System.out.println("Ya existe un socio con ese DNI.");
                return;
            }

            String nombre = Main.leerTexto(sc, "Nombre: ");
            String telefono = Main.leerTexto(sc, "Teléfono: ");
            LocalDate fechaAlta = Main.leerFecha(sc, "Fecha de alta (dd/mm/aaaa): ");

            String sql = "INSERT INTO SOCIO (nombre, DNI, telefono, fecha_alta) VALUES (?, ?, ?, ?)";

            try (PreparedStatement instruccion = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                instruccion.setString(1, nombre);
                instruccion.setString(2, dni);
                instruccion.setString(3, telefono);
                instruccion.setDate(4, Date.valueOf(fechaAlta));

                int resultado = instruccion.executeUpdate();

                if (resultado == 1) {
                    try (ResultSet claves = instruccion.getGeneratedKeys()) {
                        claves.next();
                        System.out.println("Socio agregado correctamente con ID " + claves.getInt(1) + ".");
                    }
                } else {
                    System.out.println("No se pudo agregar el socio.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al agregar el socio: " + e.getMessage());
        }
    }

    public static void listar(Connection conexion) {
        String sql = "SELECT id_socio, nombre, DNI, telefono FROM SOCIO";

        try (PreparedStatement instruccion = conexion.prepareStatement(sql);
             ResultSet conjuntoResultados = instruccion.executeQuery()) {

            System.out.printf("%n%-6s%-20s%-11s%s%n", "ID", "NOMBRE", "DNI", "TELÉFONO");
            System.out.println("---------------------------------------------------------");

            while (conjuntoResultados.next()) {
                System.out.printf("%-6d%-20s%-11s%s%n",
                        conjuntoResultados.getInt("id_socio"),
                        conjuntoResultados.getString("nombre"),
                        conjuntoResultados.getString("DNI"),
                        conjuntoResultados.getString("telefono"));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar los socios: " + e.getMessage());
        }
    }

    public static void buscar(Scanner sc, Connection conexion) {
        String dni = Main.leerTexto(sc, "Ingrese el DNI: ");

        try {
            Socio socio = buscarPorDni(conexion, dni);

            if (socio == null) {
                System.out.println("No se encontró un socio con ese DNI.");
            } else {
                System.out.println("\n" + socio);
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar el socio: " + e.getMessage());
        }
    }

    public static void modificar(Scanner sc, Connection conexion) {
        String dni = Main.leerTexto(sc, "Ingrese el DNI del socio a modificar: ");

        try {
            Socio socio = buscarPorDni(conexion, dni);

            if (socio == null) {
                System.out.println("No se encontró un socio con ese DNI.");
                return;
            }

            System.out.println("\nDatos actuales:\n" + socio);
            System.out.println("\nIngrese los nuevos datos:");

            String nombre = Main.leerTexto(sc, "Nombre: ");
            String telefono = Main.leerTexto(sc, "Teléfono: ");

            String sql = "UPDATE SOCIO SET nombre = ?, telefono = ? WHERE id_socio = ?";

            try (PreparedStatement instruccion = conexion.prepareStatement(sql)) {
                instruccion.setString(1, nombre);
                instruccion.setString(2, telefono);
                instruccion.setInt(3, socio.getIdSocio());

                int resultado = instruccion.executeUpdate();

                if (resultado == 1) {
                    System.out.println("Socio modificado correctamente.");
                } else {
                    System.out.println("No se pudo modificar el socio.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al modificar el socio: " + e.getMessage());
        }
    }

    public static Socio buscarPorDni(Connection conexion, String dni) throws SQLException {
        String sql = "SELECT * FROM SOCIO WHERE DNI = ?";

        try (PreparedStatement instruccion = conexion.prepareStatement(sql)) {
            instruccion.setString(1, dni);

            try (ResultSet conjuntoResultados = instruccion.executeQuery()) {
                if (conjuntoResultados.next()) {
                    return new Socio(conjuntoResultados.getInt("id_socio"),
                            conjuntoResultados.getString("nombre"),
                            conjuntoResultados.getString("DNI"),
                            conjuntoResultados.getString("telefono"),
                            conjuntoResultados.getDate("fecha_alta").toLocalDate());
                }
            }
        }

        return null;
    }
}
