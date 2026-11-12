package pe.edu.upeu.alquileres.repository;

import pe.edu.upeu.alquileres.model.Inquilino;
import pe.edu.upeu.alquileres.model.TipoPersoneria;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class InquilinoRepository extends AbstractJpaRepository<Inquilino, Long> {

    private static final String SELECT_JOIN =
            "SELECT i.idinquilino, i.dni, i.nombre, i.apellido, i.telefono, i.direccion, " +
            "i.idtipopersoneria, i.rep_legal, i.ruc, i.direccion_fiscal, t.nombre AS tipo_nombre " +
            "FROM inquilino i LEFT JOIN tipo_personeria t ON i.idtipopersoneria = t.idtipopersoneria ";

    @Override
    protected String getTableName() {
        return "inquilino";
    }

    @Override
    protected String getPkColumn() {
        return "idinquilino";
    }

    @Override
    public List<Inquilino> findAll() {
        return executeQuery(SELECT_JOIN + "ORDER BY i.apellido, i.nombre");
    }

    @Override
    public Optional<Inquilino> findById(Long id) {
        return executeQueryOne(SELECT_JOIN + "WHERE i.idinquilino = ?", id);
    }

    public Optional<Inquilino> findByDni(String dni) {
        return executeQueryOne(SELECT_JOIN + "WHERE i.dni = ?", dni);
    }

    public long countByTipo(int idTipoPersoneria) {
        return executeCount("SELECT COUNT(*) FROM inquilino WHERE idtipopersoneria = ?", idTipoPersoneria);
    }

    @Override
    protected Inquilino insert(Connection conn, Inquilino e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO inquilino (dni, nombre, apellido, telefono, direccion, idtipopersoneria, rep_legal, ruc, direccion_fiscal) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                e.getDni(), e.getNombre(), e.getApellido(), e.getTelefono(), e.getDireccion(),
                e.getTipoPersoneria() == null ? null : e.getTipoPersoneria().getIdTipoPersoneria(),
                e.getRepLegal(), e.getRuc(), e.getDireccionFiscal());
        e.setIdInquilino(id);
        return e;
    }

    @Override
    protected Inquilino updateRow(Connection conn, Inquilino e) throws SQLException {
        executeUpdate(conn,
                "UPDATE inquilino SET dni = ?, nombre = ?, apellido = ?, telefono = ?, direccion = ?, " +
                        "idtipopersoneria = ?, rep_legal = ?, ruc = ?, direccion_fiscal = ? WHERE idinquilino = ?",
                e.getDni(), e.getNombre(), e.getApellido(), e.getTelefono(), e.getDireccion(),
                e.getTipoPersoneria() == null ? null : e.getTipoPersoneria().getIdTipoPersoneria(),
                e.getRepLegal(), e.getRuc(), e.getDireccionFiscal(), e.getIdInquilino());
        return e;
    }

    @Override
    protected Inquilino mapRow(ResultSet rs) throws SQLException {
        Integer idTipo = rs.getInt("idtipopersoneria");
        if (rs.wasNull()) idTipo = null;
        return Inquilino.builder()
                .idInquilino(rs.getLong("idinquilino"))
                .dni(rs.getString("dni"))
                .nombre(rs.getString("nombre"))
                .apellido(rs.getString("apellido"))
                .telefono(rs.getString("telefono"))
                .direccion(rs.getString("direccion"))
                .tipoPersoneria(idTipo == null ? null : TipoPersoneria.builder()
                        .idTipoPersoneria(idTipo)
                        .nombre(rs.getString("tipo_nombre"))
                        .build())
                .repLegal(rs.getString("rep_legal"))
                .ruc(rs.getString("ruc"))
                .direccionFiscal(rs.getString("direccion_fiscal"))
                .build();
    }
}
