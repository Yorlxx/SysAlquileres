package pe.edu.upeu.alquileres.service.impl;

import pe.edu.upeu.alquileres.exception.BusinessException;
import pe.edu.upeu.alquileres.model.Pago;
import pe.edu.upeu.alquileres.repository.PagoRepository;
import pe.edu.upeu.alquileres.service.IPagoService;
import pe.edu.upeu.alquileres.util.Validador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PagoServiceImp implements IPagoService {

    private final PagoRepository repository;

    public PagoServiceImp(PagoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Pago> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Pago> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Pago save(Pago pago) {
        normalizar(pago);
        Validador.validar(pago);
        pago.setIdPago(null);
        return repository.save(pago);
    }

    @Override
    public Pago update(Long id, Pago pago) {
        repository.findById(id).orElseThrow(() -> new BusinessException("El pago ya no existe."));
        normalizar(pago);
        Validador.validar(pago);
        pago.setIdPago(id);
        return repository.update(pago);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BusinessException("El pago ya no existe.");
        }
        repository.deleteById(id);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public BigDecimal totalRecaudado() {
        return repository.sumTotal();
    }

    @Override
    public BigDecimal totalMesActual() {
        LocalDate inicio = inicioMesActual();
        return repository.sumBetween(inicio, inicio.withDayOfMonth(inicio.lengthOfMonth()));
    }

    @Override
    public BigDecimal promedioPago() {
        long cantidad = count();
        if (cantidad == 0) return BigDecimal.ZERO;
        return totalRecaudado().divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);
    }

    @Override
    public LocalDate inicioMesActual() {
        return LocalDate.now().withDayOfMonth(1);
    }

    private void normalizar(Pago pago) {
        if (pago.getPeriodo() != null) {
            pago.setPeriodo(pago.getPeriodo().withDayOfMonth(1));
        }
        if (pago.getMonto() == null) pago.setMonto(BigDecimal.ZERO);
        if (pago.getDescuento() == null) pago.setDescuento(BigDecimal.ZERO);
        if (pago.getIgv() == null) pago.setIgv(BigDecimal.ZERO);
        if (pago.getDescuento().signum() < 0) {
            throw new BusinessException("El descuento no puede ser negativo.");
        }
        if (pago.getIgv().signum() < 0) {
            throw new BusinessException("El IGV no puede ser negativo.");
        }
    }
}
