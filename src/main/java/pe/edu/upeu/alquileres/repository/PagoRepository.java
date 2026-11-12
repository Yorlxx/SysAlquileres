package pe.edu.upeu.alquileres.repository;

import pe.edu.upeu.alquileres.enums.EstadoContrato;
import pe.edu.upeu.alquileres.enums.EstadoDepartamento;
import pe.edu.upeu.alquileres.enums.MetodoPago;
import pe.edu.upeu.alquileres.model.Contrato;
import pe.edu.upeu.alquileres.model.Departamento;
import pe.edu.upeu.alquileres.model.Inquilino;
import pe.edu.upeu.alquileres.model.Pago;
import pe.edu.upeu.alquileres.model.TipoPersoneria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PagoRepository extends AbstractJpaRepository<Pago, Long> {

    private static final String SELECT_JOIN =
            "SELECT p.idpago, p.fecha_pago, p.periodo, p.monto, p.metodo, p.idcontrato, p.descuento, p.igv, " +
            "c.fecha_inicio, c.fecha_fin, c.monto_mensual, c.estado AS c_estado, c.fecha_contrato, c.cantidad_persona, " +
            "i.idinquilino AS i_id, i.dni, i.nombre AS i_nombre, i.apellido, i.telefono, i.direccion AS i_direccion, " +
            "i.idtipopersoneria, i.rep_legal, i.ruc, i.direccion_fiscal, t.nombre AS tipo_nombre, " +
            "d.iddepartamento AS d_id, d.numero AS d_numero, d.piso AS d_piso, d.direccion AS d_direccion, " +
            "d.precio_mensual AS d_precio, d.estado AS d_estado " +
            "FROM pago p " +
            "JOIN contrato c ON p.idcontrato = c.idcontrato " +
            "JOIN inquilino i ON c.idinquilino = i.idinquilino " +
            "LEFT JOIN tipo_personeria t ON i.idtipopersoneria = t.idtipopersoneria " +
            "JOIN departamento d ON c.iddepartamento = d.iddepartamento ";

    @Override
    protected String getTableName() {
        return "pago";
    }

    @Override
    protected String getPkColumn() {
        return "idpago";
    }

    @Override
    public List<Pago> findAll() {
        return executeQuery(SELECT_JOIN + "ORDER BY p.fecha_pago DESC, p.idpago DESC");
    }

    @Override
    public Optional<Pago> findById(Long id) {
        return executeQueryOne(SELECT_JOIN + "WHERE p.idpago = ?", id);
    }

    public List<Pago> findByContrato(Long idContrato) {
        return executeQuery(SELECT_JOIN + "WHERE p.idcontrato = ? ORDER BY p.periodo DESC", idContrato);
    }

    public long countByContrato(Long idContrato) {
        return executeCount("SELECT COUNT(*) FROM pago WHERE idcontrato = ?", idContrato);
    }

    public long count() {
        return executeCount("SELECT COUNT(*) FROM pago");
    }

    public BigDecimal sumTotal() {
        return executeDecimal("SELECT COALESCE(SUM(monto), 0) FROM pago");
    }

    public BigDecimal sumBetween(LocalDate desde, LocalDate hasta) {
        return executeDecimal("SELECT COALESCE(SUM(monto), 0) FROM pago WHERE fecha_pago BETWEEN ? AND ?", desde, hasta);
    }

    @Override
    protected Pago insert(Connection conn, Pago e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO pago (fecha_pago, periodo, monto, metodo, idcontrato, descuento, igv) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                e.getFechaPago(), e.getPeriodo(), e.getMonto(), e.getMetodo().getDescripcion(),
                e.getContrato().getIdContrato(), e.getDescuento(), e.getIgv());
        e.setIdPago(id);
        return e;
    }

    @Override
    protected Pago updateRow(Connection conn, Pago e) throws SQLException {
        executeUpdate(conn,
                "UPDATE pago SET fecha_pago = ?, periodo = ?, monto = ?, metodo = ?, idcontrato = ?, " +
                        "descuento = ?, igv = ? WHERE idpago = ?",
                e.getFechaPago(), e.getPeriodo(), e.getMonto(), e.getMetodo().getDescripcion(),
                e.getContrato().getIdContrato(), e.getDescuento(), e.getIgv(), e.getIdPago());
        return e;
    }

    @Override
    protected Pago mapRow(ResultSet rs) throws SQLException {
        Integer idTipo = rs.getInt("idtipopersoneria");
        if (rs.wasNull()) idTipo = null;

        Inquilino inquilino = Inquilino.builder()
                .idInquilino(rs.getLong("i_id"))
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
                .idDepartamento(rs.getLong("d_id"))
                .numero(rs.getString("d_numero"))
                .piso(rs.getInt("d_piso"))
                .direccion(rs.getString("d_direccion"))
                .precioMensual(rs.getBigDecimal("d_precio"))
                .estado(EstadoDepartamento.desde(rs.getString("d_estado")))
                .build();

        Contrato contrato = Contrato.builder()
                .idContrato(rs.getLong("idcontrato"))
                .fechaInicio(rs.getDate("fecha_inicio").toLocalDate())
                .fechaFin(rs.getDate("fecha_fin").toLocalDate())
                .montoMensual(rs.getBigDecimal("monto_mensual"))
                .estado(EstadoContrato.desde(rs.getString("c_estado")))
                .inquilino(inquilino)
                .departamento(departamento)
                .fechaContrato(rs.getTimestamp("fecha_contrato").toLocalDateTime())
                .cantidadPersona(rs.getInt("cantidad_persona"))
                .build();

        return Pago.builder()
                .idPago(rs.getLong("idpago"))
                .fechaPago(rs.getDate("fecha_pago").toLocalDate())
                .periodo(rs.getDate("periodo").toLocalDate())
                .monto(rs.getBigDecimal("monto"))
                .metodo(MetodoPago.desde(rs.getString("metodo")))
                .contrato(contrato)
                .descuento(rs.getBigDecimal("descuento"))
                .igv(rs.getBigDecimal("igv"))
                .build();
    }
}
