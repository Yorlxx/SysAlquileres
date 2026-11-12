package pe.edu.upeu.alquileres.repository;

import pe.edu.upeu.alquileres.enums.EstadoDepartamento;
import pe.edu.upeu.alquileres.model.Departamento;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DepartamentoRepository extends AbstractJpaRepository<Departamento, Long> {

    @Override
    protected String getTableName() {
        return "departamento";
    }

    @Override
    protected String getPkColumn() {
        return "iddepartamento";
    }

    @Override
    public List<Departamento> findAll() {
        return executeQuery("SELECT * FROM departamento ORDER BY piso, numero");
    }

    public Optional<Departamento> findByNumero(String numero) {
        return executeQueryOne("SELECT * FROM departamento WHERE LOWER(numero) = LOWER(?)", numero);
    }

    public List<Departamento> findDisponibles() {
        return executeQuery("SELECT * FROM departamento WHERE estado = 'Disponible' ORDER BY piso, numero");
    }

    public void updateEstado(Long id, EstadoDepartamento estado) {
        executeUpdateStandalone("UPDATE departamento SET estado = ? WHERE iddepartamento = ?",
                estado.getDescripcion(), id);
    }

    public long countByEstado(EstadoDepartamento estado) {
        return executeCount("SELECT COUNT(*) FROM departamento WHERE estado = ?", estado.getDescripcion());
    }

    public BigDecimal sumPrecioOcupados() {
        return executeDecimal("SELECT COALESCE(SUM(precio_mensual), 0) FROM departamento WHERE estado = 'Ocupado'");
    }

    public BigDecimal sumPrecioTotal() {
        return executeDecimal("SELECT COALESCE(SUM(precio_mensual), 0) FROM departamento");
    }

    @Override
    protected Departamento insert(Connection conn, Departamento e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO departamento (numero, piso, direccion, precio_mensual, estado) VALUES (?, ?, ?, ?, ?)",
                e.getNumero(), e.getPiso(), e.getDireccion(), e.getPrecioMensual(),
                e.getEstado().getDescripcion());
        e.setIdDepartamento(id);
        return e;
    }

    @Override
    protected Departamento updateRow(Connection conn, Departamento e) throws SQLException {
        executeUpdate(conn,
                "UPDATE departamento SET numero = ?, piso = ?, direccion = ?, precio_mensual = ?, estado = ? WHERE iddepartamento = ?",
                e.getNumero(), e.getPiso(), e.getDireccion(), e.getPrecioMensual(),
                e.getEstado().getDescripcion(), e.getIdDepartamento());
        return e;
    }

    @Override
    protected Departamento mapRow(ResultSet rs) throws SQLException {
        return Departamento.builder()
                .idDepartamento(rs.getLong("iddepartamento"))
                .numero(rs.getString("numero"))
                .piso(rs.getInt("piso"))
                .direccion(rs.getString("direccion"))
                .precioMensual(rs.getBigDecimal("precio_mensual"))
                .estado(EstadoDepartamento.desde(rs.getString("estado")))
                .build();
    }
}
