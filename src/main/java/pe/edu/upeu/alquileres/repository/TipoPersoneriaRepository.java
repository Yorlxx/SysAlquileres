package pe.edu.upeu.alquileres.repository;

import pe.edu.upeu.alquileres.model.TipoPersoneria;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TipoPersoneriaRepository {

    public List<TipoPersoneria> findAll() {
        return new Mapper().query("SELECT * FROM tipo_personeria ORDER BY idtipopersoneria");
    }

    public Optional<TipoPersoneria> findById(Integer id) {
        return new Mapper().query("SELECT * FROM tipo_personeria WHERE idtipopersoneria = ?", id)
                .stream().findFirst();
    }

    /** Pequeño mapper separado para no heredar el CRUD de entidades. */
    private static final class Mapper extends pe.edu.upeu.alquileres.repository.helper.SqlHelper<TipoPersoneria> {
        List<TipoPersoneria> query(String sql, Object... params) {
            return executeQuery(sql, params);
        }

        @Override
        protected TipoPersoneria mapRow(ResultSet rs) throws SQLException {
            return TipoPersoneria.builder()
                    .idTipoPersoneria(rs.getInt("idtipopersoneria"))
                    .nombre(rs.getString("nombre"))
                    .build();
        }
    }
}
