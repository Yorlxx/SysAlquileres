package pe.edu.upeu.alquileres.config;

import pe.edu.upeu.alquileres.controller.ContratoController;
import pe.edu.upeu.alquileres.controller.DepartamentoController;
import pe.edu.upeu.alquileres.controller.InquilinoController;
import pe.edu.upeu.alquileres.controller.LoginController;
import pe.edu.upeu.alquileres.controller.MainController;
import pe.edu.upeu.alquileres.controller.PagoController;
import pe.edu.upeu.alquileres.repository.ContratoRepository;
import pe.edu.upeu.alquileres.repository.DepartamentoRepository;
import pe.edu.upeu.alquileres.repository.FinalizacionContratoRepository;
import pe.edu.upeu.alquileres.repository.InquilinoRepository;
import pe.edu.upeu.alquileres.repository.PagoRepository;
import pe.edu.upeu.alquileres.repository.TipoPersoneriaRepository;
import pe.edu.upeu.alquileres.repository.UsuarioRepository;
import pe.edu.upeu.alquileres.service.IContratoService;
import pe.edu.upeu.alquileres.service.IDepartamentoService;
import pe.edu.upeu.alquileres.service.IInquilinoService;
import pe.edu.upeu.alquileres.service.IPagoService;
import pe.edu.upeu.alquileres.service.IUsuarioService;
import pe.edu.upeu.alquileres.service.impl.ContratoServiceImp;
import pe.edu.upeu.alquileres.service.impl.DepartamentoServiceImp;
import pe.edu.upeu.alquileres.service.impl.InquilinoServiceImp;
import pe.edu.upeu.alquileres.service.impl.PagoServiceImp;
import pe.edu.upeu.alquileres.service.impl.UsuarioServiceImp;

import java.util.HashMap;
import java.util.Map;

/**
 * Contenedor de dependencias de la aplicación (patrón Singleton).
 * Arma el grafo: repositorios → servicios → controladores.
 */
public final class AppContext {

    private static AppContext instance;

    private final Map<Class<?>, Object> contenedor = new HashMap<>();

    private AppContext() {
        DatabaseConfig.init();
        registrarRepositorios();
        registrarServicios();
        registrarControladores();
    }

    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    private void registrarRepositorios() {
        registrar(UsuarioRepository.class, new UsuarioRepository());
        registrar(DepartamentoRepository.class, new DepartamentoRepository());
        registrar(InquilinoRepository.class, new InquilinoRepository());
        registrar(TipoPersoneriaRepository.class, new TipoPersoneriaRepository());
        registrar(ContratoRepository.class, new ContratoRepository());
        registrar(PagoRepository.class, new PagoRepository());
        registrar(FinalizacionContratoRepository.class, new FinalizacionContratoRepository());
    }

    private void registrarServicios() {
        registrar(IUsuarioService.class, new UsuarioServiceImp(
                getBean(UsuarioRepository.class)));

        registrar(IDepartamentoService.class, new DepartamentoServiceImp(
                getBean(DepartamentoRepository.class),
                getBean(ContratoRepository.class)));

        registrar(IInquilinoService.class, new InquilinoServiceImp(
                getBean(InquilinoRepository.class),
                getBean(ContratoRepository.class),
                getBean(TipoPersoneriaRepository.class)));

        registrar(IContratoService.class, new ContratoServiceImp(
                getBean(ContratoRepository.class),
                getBean(DepartamentoRepository.class),
                getBean(PagoRepository.class),
                getBean(FinalizacionContratoRepository.class)));

        registrar(IPagoService.class, new PagoServiceImp(
                getBean(PagoRepository.class)));
    }

    private void registrarControladores() {
        registrar(LoginController.class, new LoginController(
                getBean(IUsuarioService.class)));
        registrar(MainController.class, new MainController());
        registrar(DepartamentoController.class, new DepartamentoController(
                getBean(IDepartamentoService.class)));
        registrar(InquilinoController.class, new InquilinoController(
                getBean(IInquilinoService.class)));
        registrar(ContratoController.class, new ContratoController(
                getBean(IContratoService.class),
                getBean(IInquilinoService.class),
                getBean(IDepartamentoService.class)));
        registrar(PagoController.class, new PagoController(
                getBean(IPagoService.class),
                getBean(IContratoService.class)));
    }

    private void registrar(Class<?> tipo, Object bean) {
        contenedor.put(tipo, bean);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> tipo) {
        Object bean = contenedor.get(tipo);
        if (bean == null) {
            bean = contenedor.values().stream()
                    .filter(b -> tipo.isAssignableFrom(b.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Bean no encontrado: " + tipo.getName()));
        }
        return (T) bean;
    }
}
