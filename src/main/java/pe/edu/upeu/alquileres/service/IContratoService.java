package pe.edu.upeu.alquileres.service;

import pe.edu.upeu.alquileres.model.Contrato;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IContratoService {
    List<Contrato> findAll();
    Optional<Contrato> findById(Long id);
    Contrato save(Contrato contrato);
    Contrato update(Long id, Contrato contrato);
    void delete(Long id);
    void finalizar(Long id, String motivo);
    List<Contrato> findVigentes();
    List<Contrato> findProximosVencer(int dias);
    long countVigentes();
    long countFinalizados();
    BigDecimal rentaMensualVigente();
    long countPorVencer(int dias);
}
