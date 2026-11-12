package pe.edu.upeu.alquileres.service.impl;

import pe.edu.upeu.alquileres.enums.EstadoContrato;
import pe.edu.upeu.alquileres.enums.EstadoDepartamento;
import pe.edu.upeu.alquileres.exception.BusinessException;
import pe.edu.upeu.alquileres.model.Contrato;
import pe.edu.upeu.alquileres.repository.ContratoRepository;
import pe.edu.upeu.alquileres.repository.DepartamentoRepository;
import pe.edu.upeu.alquileres.repository.FinalizacionContratoRepository;
import pe.edu.upeu.alquileres.repository.PagoRepository;
import pe.edu.upeu.alquileres.service.IContratoService;
import pe.edu.upeu.alquileres.util.Validador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ContratoServiceImp implements IContratoService {

    private final ContratoRepository repository;
    private final DepartamentoRepository departamentoRepository;
    private final PagoRepository pagoRepository;
    private final FinalizacionContratoRepository finalizacionRepository;

    public ContratoServiceImp(ContratoRepository repository,
                              DepartamentoRepository departamentoRepository,
                              PagoRepository pagoRepository,
                              FinalizacionContratoRepository finalizacionRepository) {
        this.repository = repository;
        this.departamentoRepository = departamentoRepository;
        this.pagoRepository = pagoRepository;
        this.finalizacionRepository = finalizacionRepository;
    }

    @Override
    public List<Contrato> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Contrato> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Contrato save(Contrato contrato) {
        validarContrato(contrato, null);
        contrato.setIdContrato(null);
        contrato.setEstado(EstadoContrato.VIGENTE);
        if (contrato.getFechaContrato() == null) {
            contrato.setFechaContrato(LocalDateTime.now());
        }
        if (contrato.getCantidadPersona() == null) {
            contrato.setCantidadPersona(1);
        }
        Contrato guardado = repository.save(contrato);
        departamentoRepository.updateEstado(contrato.getDepartamento().getIdDepartamento(), EstadoDepartamento.OCUPADO);
        return guardado;
    }

    @Override
    public Contrato update(Long id, Contrato contrato) {
        Contrato actual = repository.findById(id)
                .orElseThrow(() -> new BusinessException("El contrato ya no existe."));
        if (actual.getEstado() == EstadoContrato.FINALIZADO) {
            throw new BusinessException("No se puede modificar un contrato finalizado.");
        }
        validarContrato(contrato, id);
        Long idDepartamentoAnterior = actual.getDepartamento().getIdDepartamento();

        contrato.setIdContrato(id);
        contrato.setEstado(EstadoContrato.VIGENTE);
        contrato.setFechaContrato(actual.getFechaContrato());
        if (contrato.getCantidadPersona() == null) contrato.setCantidadPersona(1);

        Contrato actualizado = repository.update(contrato);
        departamentoRepository.updateEstado(contrato.getDepartamento().getIdDepartamento(), EstadoDepartamento.OCUPADO);

        if (!idDepartamentoAnterior.equals(contrato.getDepartamento().getIdDepartamento())
                && !repository.existsVigenteByDepartamento(idDepartamentoAnterior)) {
            departamentoRepository.updateEstado(idDepartamentoAnterior, EstadoDepartamento.DISPONIBLE);
        }
        return actualizado;
    }

    @Override
    public void delete(Long id) {
        Contrato contrato = repository.findById(id)
                .orElseThrow(() -> new BusinessException("El contrato ya no existe."));
        if (contrato.getEstado() == EstadoContrato.FINALIZADO
                || finalizacionRepository.existsByContrato(id)) {
            throw new BusinessException("No se puede eliminar un contrato finalizado; forma parte del historial.");
        }
        if (pagoRepository.countByContrato(id) > 0) {
            throw new BusinessException("No se puede eliminar el contrato porque tiene pagos registrados.");
        }
        Long idDepartamento = contrato.getDepartamento().getIdDepartamento();
        repository.deleteById(id);
        if (!repository.existsVigenteByDepartamento(idDepartamento)) {
            departamentoRepository.updateEstado(idDepartamento, EstadoDepartamento.DISPONIBLE);
        }
    }

    @Override
    public void finalizar(Long id, String motivo) {
        Contrato contrato = repository.findById(id)
                .orElseThrow(() -> new BusinessException("El contrato ya no existe."));
        if (contrato.getEstado() == EstadoContrato.FINALIZADO) {
            throw new BusinessException("El contrato ya está finalizado.");
        }
        String motivoLimpio = Validador.texto(motivo);
        if (motivoLimpio.isEmpty()) motivoLimpio = "Fin de plazo";
        if (motivoLimpio.length() > 20) {
            throw new BusinessException("El motivo admite como máximo 20 caracteres.");
        }
        repository.finalizar(id, contrato.getDepartamento().getIdDepartamento(), motivoLimpio);
    }

    @Override
    public List<Contrato> findVigentes() {
        return repository.findVigentes();
    }

    @Override
    public List<Contrato> findProximosVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);
        return repository.findVigentes().stream()
                .filter(c -> c.getFechaFin() != null
                        && !c.getFechaFin().isBefore(hoy)
                        && !c.getFechaFin().isAfter(limite))
                .toList();
    }

    @Override
    public long countVigentes() {
        return repository.countByEstado(EstadoContrato.VIGENTE);
    }

    @Override
    public long countFinalizados() {
        return repository.countByEstado(EstadoContrato.FINALIZADO);
    }

    @Override
    public BigDecimal rentaMensualVigente() {
        return repository.sumMontoVigente();
    }

    @Override
    public long countPorVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        return repository.countPorVencer(hoy, hoy.plusDays(dias));
    }

    private void validarContrato(Contrato contrato, Long excluirId) {
        if (contrato.getFechaInicio() != null && contrato.getFechaFin() != null
                && !contrato.getFechaFin().isAfter(contrato.getFechaInicio())) {
            throw new BusinessException("La fecha de fin debe ser posterior a la fecha de inicio.");
        }
        if (contrato.getCantidadPersona() == null || contrato.getCantidadPersona() < 1) {
            contrato.setCantidadPersona(1);
        }
        Validador.validar(contrato);

        Long idDepartamento = contrato.getDepartamento().getIdDepartamento();
        repository.findVigenteSuperpuesto(idDepartamento, contrato.getFechaInicio(),
                        contrato.getFechaFin(), excluirId)
                .ifPresent(c -> {
                    throw new BusinessException("El departamento ya tiene un contrato vigente que se cruza con esas fechas.");
                });
    }
}
