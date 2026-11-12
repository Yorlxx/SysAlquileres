package pe.edu.upeu.alquileres.service.impl;

import pe.edu.upeu.alquileres.exception.BusinessException;
import pe.edu.upeu.alquileres.model.Inquilino;
import pe.edu.upeu.alquileres.model.TipoPersoneria;
import pe.edu.upeu.alquileres.repository.ContratoRepository;
import pe.edu.upeu.alquileres.repository.InquilinoRepository;
import pe.edu.upeu.alquileres.repository.TipoPersoneriaRepository;
import pe.edu.upeu.alquileres.service.IInquilinoService;
import pe.edu.upeu.alquileres.util.Validador;

import java.util.List;
import java.util.Optional;

public class InquilinoServiceImp implements IInquilinoService {

    private final InquilinoRepository repository;
    private final ContratoRepository contratoRepository;
    private final TipoPersoneriaRepository tipoPersoneriaRepository;

    public InquilinoServiceImp(InquilinoRepository repository,
                               ContratoRepository contratoRepository,
                               TipoPersoneriaRepository tipoPersoneriaRepository) {
        this.repository = repository;
        this.contratoRepository = contratoRepository;
        this.tipoPersoneriaRepository = tipoPersoneriaRepository;
    }

    @Override
    public List<Inquilino> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Inquilino> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Inquilino save(Inquilino inquilino) {
        normalizar(inquilino);
        Validador.validar(inquilino);
        validarDniUnico(inquilino);
        inquilino.setIdInquilino(null);
        return repository.save(inquilino);
    }

    @Override
    public Inquilino update(Long id, Inquilino inquilino) {
        repository.findById(id).orElseThrow(() -> new BusinessException("El inquilino ya no existe."));
        normalizar(inquilino);
        Validador.validar(inquilino);
        validarDniUnico(inquilino);
        inquilino.setIdInquilino(id);
        return repository.update(inquilino);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BusinessException("El inquilino ya no existe.");
        }
        if (contratoRepository.existsByInquilino(id)) {
            throw new BusinessException("No se puede eliminar el inquilino porque tiene contratos registrados.");
        }
        repository.deleteById(id);
    }

    @Override
    public List<TipoPersoneria> listarTiposPersoneria() {
        return tipoPersoneriaRepository.findAll();
    }

    @Override
    public long count() {
        return repository.findAll().size();
    }

    @Override
    public long countNaturales() {
        return repository.findAll().stream()
                .filter(i -> i.getTipoPersoneria() != null && "Natural".equalsIgnoreCase(i.getTipoPersoneria().getNombre()))
                .count();
    }

    @Override
    public long countJuridicas() {
        return repository.findAll().stream()
                .filter(i -> i.getTipoPersoneria() != null && "Jurídica".equalsIgnoreCase(i.getTipoPersoneria().getNombre()))
                .count();
    }

    private void normalizar(Inquilino i) {
        i.setDni(Validador.texto(i.getDni()));
        i.setNombre(Validador.texto(i.getNombre()));
        i.setApellido(Validador.texto(i.getApellido()));
        i.setTelefono(Validador.texto(i.getTelefono()));
        i.setDireccion(Validador.texto(i.getDireccion()));
        i.setRepLegal(Validador.texto(i.getRepLegal()));
        i.setRuc(Validador.texto(i.getRuc()));
        i.setDireccionFiscal(Validador.texto(i.getDireccionFiscal()));

        if (i.getTipoPersoneria() != null && "Natural".equalsIgnoreCase(i.getTipoPersoneria().getNombre())) {
            i.setRepLegal(null);
            i.setRuc(null);
            i.setDireccionFiscal(null);
        }
    }

    private void validarDniUnico(Inquilino i) {
        repository.findByDni(i.getDni()).ifPresent(otro -> {
            if (!otro.getIdInquilino().equals(i.getIdInquilino())) {
                throw new BusinessException("Ya existe un inquilino con el DNI " + i.getDni() + ".");
            }
        });
    }
}
