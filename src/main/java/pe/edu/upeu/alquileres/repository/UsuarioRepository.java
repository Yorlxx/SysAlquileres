package pe.edu.upeu.alquileres.repository;

import pe.edu.upeu.alquileres.model.Usuario;
import pe.edu.upeu.alquileres.repository.helper.SqlHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UsuarioRepository extends SqlHelper<Usuario> {

    /** Busca un usuario activo con las credenciales indicadas. */
    public Optional<Usuario> login(String usuario, String clave) {
        return executeQueryOne(
                "SELECT * FROM usuario WHERE usuario = ? AND clave = ? AND estado = 'Activo'",
                usuario, clave);
    }

    @Override
    protected Usuario mapRow(ResultSet rs) throws SQLException {
        return Usuario.builder()
                .idUsuario(rs.getLong("idusuario"))
                .usuario(rs.getString("usuario"))
                .clave(rs.getString("clave"))
                .nombre(rs.getString("nombre"))
                .rol(rs.getString("rol"))
                .estado(rs.getString("estado"))
                .build();
    }
}
