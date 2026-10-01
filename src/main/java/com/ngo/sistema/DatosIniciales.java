package com.ngo.sistema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * Prepara la base de datos embebida (H2) al arrancar, para que el sistema funcione sin PostgreSQL:
 * 1) crea las tablas si todavía no existen (db/h2/schema.sql, idempotente);
 * 2) si la base está vacía (primer arranque), carga los datos de ejemplo (db/h2/datos.sql).
 * Los arranques siguientes no tocan los datos existentes.
 */
@Component
public class DatosIniciales implements InitializingBean {

    private final DataSource dataSource;

    public DatosIniciales(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new ResourceDatabasePopulator(new ClassPathResource("db/h2/schema.sql")).execute(dataSource);

        if (!hayDatos()) {
            try (Connection con = dataSource.getConnection()) {
                con.setAutoCommit(false);
                try {
                    new ResourceDatabasePopulator(new ClassPathResource("db/h2/datos.sql")).populate(con);
                    con.commit();
                } catch (Exception e) {
                    con.rollback();
                    throw e;
                }
            }
        }
    }

    private boolean hayDatos() throws Exception {
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM rol")) {
            rs.next();
            return rs.getLong(1) > 0;
        }
    }
}
