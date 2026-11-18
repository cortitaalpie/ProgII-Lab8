package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    static final String CONTROLADOR = "com.mysql.cj.jdbc.Driver";
    static final String URL_BASEDATOS = "jdbc:mysql://localhost:3306/Biblioteca_DB";
    static final String USER = "root";
    static final String PASS = "";

    public static Connection conectar() {
        Connection conexion = null;

        try {
            Class.forName(CONTROLADOR);
            conexion = DriverManager.getConnection(URL_BASEDATOS, USER, PASS);
            System.out.println("Conexión exitosa a la base de datos Biblioteca_DB.");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: no se encontró el driver JDBC. Revise la dependencia del driver en el pom.xml.");
        } catch (SQLException e) {
            System.out.println("Error al conectar con la base de datos: " + e.getMessage());
        }

        return conexion;
    }
}
