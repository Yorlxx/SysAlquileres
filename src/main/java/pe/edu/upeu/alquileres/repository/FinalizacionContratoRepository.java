package pe.edu.upeu.alquileres.repository;

import pe.edu.upeu.alquileres.model.Contrato;
import pe.edu.upeu.alquileres.model.FinalizacionContrato;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FinalizacionContratoRepository extends AbstractJpaRepository<FinalizacionContrato, Long> {

    @Override
    protected String getTableName() {
        return "finalizacion_contrato";
    }

    @Override
    protected String getPkColumn() {
        return "id_finalizacion";
    }

    @Override
    public List<FinalizacionContrato> findAll() {
        return executeQuery("SELECT * FROM finalizacion_contrato ORDER BY fecha DESC");
    }

    public boolean existsByContrato(Long idContrato) {
        return executeExists("SELECT 1 FROM finalizacion_contrato WHERE idcontrato = ?", idContrato);
    }

    @Override
    protected FinalizacionContrato insert(Connection conn, FinalizacionContrato e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO finalizacion_contrato (idcontrato, fecha, motivo) VALUES (?, ?, ?)",
                e.getContrato().getIdContrato(), e.getFecha(), e.getMotivo());
        e.setIdFinalizacion(id);
        return e;
    }

    @Override
    protected FinalizacionContrato updateRow(Connection conn, FinalizacionContrato e) {
        return e;
    }

    @Override
    protected FinalizacionContrato mapRow(ResultSet rs) throws SQLException {
        return FinalizacionContrato.builder()
                .idFinalizacion(rs.getLong("id_finalizacion"))
                .contrato(Contrato.builder().idContrato(rs.getLong("idcontrato")).build())
                .fecha(rs.getTimestamp("fecha").toLocalDateTime())
                .motivo(rs.getString("motivo"))
                .build();
    }
}
