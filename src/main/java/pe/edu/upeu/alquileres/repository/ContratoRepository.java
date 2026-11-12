package pe.edu.upeu.alquileres.repository;

import pe.edu.upeu.alquileres.enums.EstadoContrato;
import pe.edu.upeu.alquileres.enums.EstadoDepartamento;
import pe.edu.upeu.alquileres.model.Contrato;
import pe.edu.upeu.alquileres.model.Departamento;
import pe.edu.upeu.alquileres.model.Inquilino;
import pe.edu.upeu.alquileres.model.TipoPersoneria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ContratoRepository extends AbstractJpaRepository<Contrato, Long> {

    private static final String SELECT_JOIN =
            "SELECT c.idcontrato, c.fecha_inicio, c.fecha_fin, c.monto_mensual, c.estado, " +
            "c.idinquilino, c.iddepartamento, c.fecha_contrato, c.cantidad_persona, " +
            "i.dni, i.nombre AS i_nombre, i.apellido, i.telefono, i.direccion AS i_direccion, " +
            "i.idtipopersoneria, i.rep_legal, i.ruc, i.direccion_fiscal, t.nombre AS tipo_nombre, " +
            "d.numero AS d_numero, d.piso AS d_piso, d.direccion AS d_direccion, " +
            "d.precio_mensual AS d_precio, d.estado AS d_estado " +
            "FROM contrato c " +
            "JOIN inquilino i ON c.idinquilino = i.idinquilino " +
            "LEFT JOIN tipo_personeria t ON i.idtipopersoneria = t.idtipopersoneria " +
            "JOIN departamento d ON c.iddepartamento = d.iddepartamento ";

    @Override
    protected String getTableName() {
        return "contrato";
    }

    @Override
    protected String getPkColumn() {
        return "idcontrato";
    }

    @Override
    public List<Contrato> findAll() {
        return executeQuery(SELECT_JOIN + "ORDER BY c.idcontrato DESC");
    }

    @Override
    public Optional<Contrato> findById(Long id) {
        return executeQueryOne(SELECT_JOIN + "WHERE c.idcontrato = ?", id);
    }

    public List<Contrato> findVigentes() {
        return executeQuery(SELECT_JOIN + "WHERE c.estado = 'Vigente' ORDER BY c.fecha_fin");
    }

    public List<Contrato> findByDepartamento(Long idDepartamento) {
        return executeQuery(SELECT_JOIN + "WHERE c.iddepartamento = ? ORDER BY c.idcontrato DESC", idDepartamento);
    }

    public boolean existsByDepartamento(Long idDepartamento) {
        return executeExists("SELECT 1 FROM contrato WHERE iddepartamento = ?", idDepartamento);
    }

    public boolean existsByInquilino(Long idInquilino) {
        return executeExists("SELECT 1 FROM contrato WHERE idinquilino = ?", idInquilino);
    }

    public boolean existsVigenteByDepartamento(Long idDepartamento) {
        return executeExists("SELECT 1 FROM contrato WHERE iddepartamento = ? AND estado = 'Vigente'", idDepartamento);
    }

    public Optional<Contrato> findVigenteSuperpuesto(Long idDepartamento, LocalDate inicio,
                                                     LocalDate fin, Long excluirId) {
        String sql = SELECT_JOIN +
                "WHERE c.iddepartamento = ? AND c.estado = 'Vigente' " +
                "AND c.fecha_inicio <= ? AND c.fecha_fin >= ?";
        if (excluirId != null) {
            sql += " AND c.idcontrato <> ?";
            return executeQueryOne(sql, idDepartamento, fin, inicio, excluirId);
        }
        return executeQueryOne(sql, idDepartamento, fin, inicio);
    }

    public long countByEstado(EstadoContrato estado) {
        return executeCount("SELECT COUNT(*) FROM contrato WHERE estado = ?", estado.getDescripcion());
    }

    public BigDecimal sumMontoVigente() {
        return executeDecimal("SELECT COALESCE(SUM(monto_mensual), 0) FROM contrato WHERE estado = 'Vigente'");
    }

    public long countPorVencer(LocalDate desde, LocalDate hasta) {
        return executeCount("SELECT COUNT(*) FROM contrato WHERE estado = 'Vigente' AND fecha_fin BETWEEN ? AND ?",
                desde, hasta);
    }

    public void updateEstado(Long id, EstadoContrato estado) {
        executeUpdateStandalone("UPDATE contrato SET estado = ? WHERE idcontrato = ?",
                estado.getDescripcion(), id);
    }

    /**
     * Finaliza un contrato, registra el motivo y libera el departamento en una sola transacción.
     */
    public void finalizar(Long idContrato, Long idDepartamento, String motivo) {
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try {
                executeUpdate(conn, "UPDATE contrato SET estado = 'Finalizado' WHERE idcontrato = ?", idContrato);
                executeInsert(conn,
                        "INSERT INTO finalizacion_contrato (idcontrato, fecha, motivo) VALUES (?, ?, ?)",
                        idContrato, LocalDateTime.now(), motivo);
                executeUpdate(conn,
                        "UPDATE departamento SET estado = 'Disponible' WHERE iddepartamento = ? " +
                                "AND NOT EXISTS (SELECT 1 FROM contrato WHERE iddepartamento = ? AND estado = 'Vigente')",
                        idDepartamento, idDepartamento);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al finalizar el contrato: " + e.getMessage(), e);
        }
    }

    @Override
    protected Contrato insert(Connection conn, Contrato e) throws SQLException {
        LocalDateTime fechaContrato = e.getFechaContrato() == null ? LocalDateTime.now() : e.getFechaContrato();
        long id = executeInsertGetKey(conn,
                "INSERT INTO contrato (fecha_inicio, fecha_fin, monto_mensual, estado, idinquilino, " +
                        "iddepartamento, fecha_contrato, cantidad_persona) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                e.getFechaInicio(), e.getFechaFin(), e.getMontoMensual(), e.getEstado().getDescripcion(),
                e.getInquilino().getIdInquilino(), e.getDepartamento().getIdDepartamento(),
                fechaContrato, e.getCantidadPersona());
        e.setIdContrato(id);
        e.setFechaContrato(fechaContrato);
        return e;
    }

    @Override
    protected Contrato updateRow(Connection conn, Contrato e) throws SQLException {
        executeUpdate(conn,
                "UPDATE contrato SET fecha_inicio = ?, fecha_fin = ?, monto_mensual = ?, estado = ?, " +
                        "idinquilino = ?, iddepartamento = ?, fecha_contrato = ?, cantidad_persona = ? " +
                        "WHERE idcontrato = ?",
                e.getFechaInicio(), e.getFechaFin(), e.getMontoMensual(), e.getEstado().getDescripcion(),
                e.getInquilino().getIdInquilino(), e.getDepartamento().getIdDepartamento(),
                e.getFechaContrato() == null ? LocalDateTime.now() : e.getFechaContrato(),
                e.getCantidadPersona(), e.getIdContrato());
        return e;
    }

    @Override
    protected Contrato mapRow(ResultSet rs) throws SQLException {
        Integer idTipo = rs.getInt("idtipopersoneria");
        if (rs.wasNull()) idTipo = null;

        Inquilino inquilino = Inquilino.builder()
                .idInquilino(rs.getLong("idinquilino"))
                .dni(rs.getString("dni"))
                .nombre(rs.getString("i_nombre"))
                .apellido(rs.getString("apellido"))
                .telefono(rs.getString("telefono"))
                .direccion(rs.getString("i_direccion"))
                .tipoPersoneria(idTipo == null ? null : TipoPersoneria.builder()
                        .idTipoPersoneria(idTipo)
                        .nombre(rs.getString("tipo_nombre"))
                        .build())
                .repLegal(rs.getString("rep_legal"))
                .ruc(rs.getString("ruc"))
                .direccionFiscal(rs.getString("direccion_fiscal"))
                .build();

        Departamento departamento = Departamento.builder()
                .idDepartamento(rs.getLong("iddepartamento"))
                .numero(rs.getString("d_numero"))
                .piso(rs.getInt("d_piso"))
                .direccion(rs.getString("d_direccion"))
                .precioMensual(rs.getBigDecimal("d_precio"))
                .estado(EstadoDepartamento.desde(rs.getString("d_estado")))
                .build();

        return Contrato.builder()
                .idContrato(rs.getLong("idcontrato"))
                .fechaInicio(rs.getDate("fecha_inicio").toLocalDate())
                .fechaFin(rs.getDate("fecha_fin").toLocalDate())
                .montoMensual(rs.getBigDecimal("monto_mensual"))
                .estado(EstadoContrato.desde(rs.getString("estado")))
                .inquilino(inquilino)
                .departamento(departamento)
                .fechaContrato(rs.getTimestamp("fecha_contrato").toLocalDateTime())
                .cantidadPersona(rs.getInt("cantidad_persona"))
                .build();
    }
}
