package pe.edu.upeu.alquileres.service;

import pe.edu.upeu.alquileres.model.Pago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IPagoService {
    List<Pago> findAll();
    Optional<Pago> findById(Long id);
    Pago save(Pago pago);
    Pago update(Long id, Pago pago);
    void delete(Long id);
    long count();
    BigDecimal totalRecaudado();
    BigDecimal totalMesActual();
    BigDecimal promedioPago();
    LocalDate inicioMesActual();
}
