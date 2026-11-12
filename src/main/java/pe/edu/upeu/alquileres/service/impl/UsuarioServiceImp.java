package pe.edu.upeu.alquileres.service.impl;

import pe.edu.upeu.alquileres.exception.BusinessException;
import pe.edu.upeu.alquileres.model.Usuario;
import pe.edu.upeu.alquileres.repository.UsuarioRepository;
import pe.edu.upeu.alquileres.service.IUsuarioService;

import java.util.Optional;

public class UsuarioServiceImp implements IUsuarioService {

    private final UsuarioRepository repository;

    public UsuarioServiceImp(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Usuario> login(String usuario, String clave) {
        if (usuario == null || usuario.isBlank()) {
            throw new BusinessException("Ingresa el nombre de usuario.");
        }
        if (clave == null || clave.isBlank()) {
            throw new BusinessException("Ingresa la clave.");
        }
        return repository.login(usuario.trim(), clave.trim());
    }
}
