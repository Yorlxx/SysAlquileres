package pe.edu.upeu.alquileres.service;

import pe.edu.upeu.alquileres.model.Inquilino;
import pe.edu.upeu.alquileres.model.TipoPersoneria;

import java.util.List;
import java.util.Optional;

public interface IInquilinoService {
    List<Inquilino> findAll();
    Optional<Inquilino> findById(Long id);
    Inquilino save(Inquilino inquilino);
    Inquilino update(Long id, Inquilino inquilino);
    void delete(Long id);
    List<TipoPersoneria> listarTiposPersoneria();
    long count();
    long countNaturales();
    long countJuridicas();
}
