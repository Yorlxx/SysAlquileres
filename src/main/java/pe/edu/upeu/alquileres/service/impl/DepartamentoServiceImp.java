package pe.edu.upeu.alquileres.service.impl;

import pe.edu.upeu.alquileres.enums.EstadoDepartamento;
import pe.edu.upeu.alquileres.exception.BusinessException;
import pe.edu.upeu.alquileres.model.Departamento;
import pe.edu.upeu.alquileres.repository.ContratoRepository;
import pe.edu.upeu.alquileres.repository.DepartamentoRepository;
import pe.edu.upeu.alquileres.service.IDepartamentoService;
import pe.edu.upeu.alquileres.util.Validador;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class DepartamentoServiceImp implements IDepartamentoService {

    private final DepartamentoRepository repository;
    private final ContratoRepository contratoRepository;

    public DepartamentoServiceImp(DepartamentoRepository repository, ContratoRepository contratoRepository) {
        this.repository = repository;
        this.contratoRepository = contratoRepository;
    }

    @Override
    public List<Departamento> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Departamento> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Departamento save(Departamento departamento) {
        normalizar(departamento);
        Validador.validar(departamento);
        validarNumeroUnico(departamento);
        departamento.setIdDepartamento(null);
        departamento.setEstado(EstadoDepartamento.DISPONIBLE);
        return repository.save(departamento);
    }

    @Override
    public Departamento update(Long id, Departamento departamento) {
        Departamento actual = repository.findById(id)
                .orElseThrow(() -> new BusinessException("El departamento ya no existe."));
        normalizar(departamento);
        Validador.validar(departamento);
        validarNumeroUnico(departamento);
        departamento.setIdDepartamento(id);
        departamento.setEstado(actual.getEstado());
        return repository.update(departamento);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BusinessException("El departamento ya no existe.");
        }
        if (contratoRepository.existsByDepartamento(id)) {
            throw new BusinessException("No se puede eliminar el departamento porque tiene contratos registrados.");
        }
        repository.deleteById(id);
    }

    @Override
    public List<Departamento> findDisponibles() {
        return repository.findDisponibles();
    }

    @Override
    public long count() {
        return repository.findAll().size();
    }

    @Override
    public long countDisponibles() {
        return repository.countByEstado(EstadoDepartamento.DISPONIBLE);
    }

    @Override
    public long countOcupados() {
        return repository.countByEstado(EstadoDepartamento.OCUPADO);
    }

    @Override
    public BigDecimal rentaMensualOcupada() {
        return repository.sumPrecioOcupados();
    }

    private void normalizar(Departamento d) {
        d.setNumero(Validador.texto(d.getNumero()));
        d.setDireccion(Validador.texto(d.getDireccion()));
        if (d.getPrecioMensual() == null) d.setPrecioMensual(BigDecimal.ZERO);
    }

    private void validarNumeroUnico(Departamento d) {
        repository.findByNumero(d.getNumero()).ifPresent(otro -> {
            if (!otro.getIdDepartamento().equals(d.getIdDepartamento())) {
                throw new BusinessException("Ya existe un departamento con el número " + d.getNumero() + ".");
            }
        });
    }
}
