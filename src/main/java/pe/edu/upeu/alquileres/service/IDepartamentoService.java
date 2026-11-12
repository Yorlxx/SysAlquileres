package pe.edu.upeu.alquileres.service;

import pe.edu.upeu.alquileres.model.Departamento;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IDepartamentoService {
    List<Departamento> findAll();
    Optional<Departamento> findById(Long id);
    Departamento save(Departamento departamento);
    Departamento update(Long id, Departamento departamento);
    void delete(Long id);
    List<Departamento> findDisponibles();
    long count();
    long countDisponibles();
    long countOcupados();
    BigDecimal rentaMensualOcupada();
}
