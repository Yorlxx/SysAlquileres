package pe.edu.upeu.alquileres.service;

import pe.edu.upeu.alquileres.model.Usuario;

import java.util.Optional;

public interface IUsuarioService {

    /** Devuelve el usuario si las credenciales son correctas. */
    Optional<Usuario> login(String usuario, String clave);
}
