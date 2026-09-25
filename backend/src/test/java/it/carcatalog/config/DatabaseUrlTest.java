package it.carcatalog.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseUrlTest {

    @Test
    void convertiUrlRender() {
        var p = DatabaseUrl.parse("postgresql://user:p%40ss@dpg-abc-a:5432/cardb");
        assertEquals("jdbc:postgresql://dpg-abc-a:5432/cardb", p.jdbcUrl());
        assertEquals("user", p.username());
        assertEquals("p@ss", p.password());
    }

    @Test
    void portaDiDefaultEQuery() {
        var p = DatabaseUrl.parse("postgres://u:pw@host.render.com/db?sslmode=require");
        assertEquals("jdbc:postgresql://host.render.com:5432/db?sslmode=require", p.jdbcUrl());
    }

    @Test
    void jdbcPassaInvariato() {
        var p = DatabaseUrl.parse("jdbc:postgresql://localhost:5432/x");
        assertEquals("jdbc:postgresql://localhost:5432/x", p.jdbcUrl());
        assertNull(p.username());
    }

    @Test
    void toStringNonMostraPassword() {
        assertFalse(DatabaseUrl.parse("postgresql://u:segreta@h/db").toString().contains("segreta"));
    }
}
