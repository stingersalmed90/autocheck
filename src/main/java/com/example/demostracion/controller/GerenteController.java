package com.example.demostracion.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demostracion.model.Conductor;
import com.example.demostracion.model.Inventario;
import com.example.demostracion.model.Notificacion;
import com.example.demostracion.model.Novedad;
import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.ConductorRepository;
import com.example.demostracion.repository.InventarioRepository;
import com.example.demostracion.repository.NotificacionRepository;
import com.example.demostracion.repository.NovedadRepository;
import com.example.demostracion.repository.UsuarioRepository;
import com.example.demostracion.repository.VehiculoRepository;
import com.example.demostracion.service.MensajeService;

@Controller
@RequestMapping("/gerente")
public class GerenteController {

    private final VehiculoRepository vehiculoRepository;
    private final InventarioRepository inventarioRepository;
    private final NotificacionRepository notificacionRepository;
    private final ConductorRepository conductorRepository;
    private final NovedadRepository novedadRepository;
    private final UsuarioRepository usuarioRepository;
    private final MensajeService mensajeService;

    private final String UPLOAD_DIR = "uploads/";

    public GerenteController(VehiculoRepository vehiculoRepository,
                             InventarioRepository inventarioRepository,
                             NotificacionRepository notificacionRepository,
                             ConductorRepository conductorRepository,
                             NovedadRepository novedadRepository,
                             UsuarioRepository usuarioRepository,
                             MensajeService mensajeService) {
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
        this.notificacionRepository = notificacionRepository;
        this.conductorRepository = conductorRepository;
        this.novedadRepository = novedadRepository;
        this.usuarioRepository = usuarioRepository;
        this.mensajeService = mensajeService;
    }

    // ===============================
    // 🏠 Dashboard del gerente
    // ===============================
    @GetMapping
    public String gerenteHome(Model model, Authentication auth) {
        // Agregar información del usuario para el menú de correo
        if (auth != null) {
            usuarioRepository.findByCorreo(auth.getName()).ifPresent(usuario -> {
                model.addAttribute("usuarioId", usuario.getIdUsuario());
                model.addAttribute("usuarioRol", usuario.getRol() != null ? usuario.getRol().getNombre() : "Usuario");
                model.addAttribute("unreadCount", mensajeService.contarNoLeidos(usuario.getIdUsuario()));
            });
        }
        return "gerente/gerente";
    }

    // ===============================
    // 🚘 Vehículos
    // ===============================
    @GetMapping("/vehiculos")
    public String listarVehiculos(Model model) {
        model.addAttribute("vehiculos", vehiculoRepository.findAll());
        return "gerente/vehiculos/listar";
    }

    @GetMapping("/vehiculos/crear")
    public String crearVehiculoForm(Model model) {
        model.addAttribute("vehiculo", new Vehiculo());
        model.addAttribute("inventarios", inventarioRepository.findAll());
        return "gerente/vehiculos/form";
    }

    @PostMapping("/vehiculos/crear")
    public String guardarVehiculo(@ModelAttribute Vehiculo vehiculo) {
        vehiculoRepository.save(vehiculo);
        return "redirect:/gerente/vehiculos";
    }

    @GetMapping("/vehiculos/editar/{id}")
    public String editarVehiculo(@PathVariable Long id, Model model) {
        Vehiculo vehiculo = vehiculoRepository.findById(id).orElseThrow();
        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("inventarios", inventarioRepository.findAll());
        return "gerente/vehiculos/form";
    }

    @PostMapping("/vehiculos/editar/{id}")
    public String actualizarVehiculo(@PathVariable Long id, @ModelAttribute Vehiculo vehiculo) {
        vehiculo.setIdVehiculo(id);
        vehiculoRepository.save(vehiculo);
        return "redirect:/gerente/vehiculos";
    }

    @PostMapping("/vehiculos/eliminar/{id}")
    public String eliminarVehiculo(@PathVariable Long id) {
        vehiculoRepository.deleteById(id);
        return "redirect:/gerente/vehiculos";
    }

    // ===============================
    // 📦 Inventario
    // ===============================
    @GetMapping("/inventario")
    public String listarInventario(Model model) {
        model.addAttribute("inventarios", inventarioRepository.findByActivoTrue());
        return "gerente/gerente-inventario";
    }

    @GetMapping("/inventario/inactivos")
    public String listarInventarioInactivo(Model model) {
        model.addAttribute("inventarios", inventarioRepository.findByActivoFalse());
        return "gerente/gerente-inventario";
    }

    @GetMapping("/inventario/crear")
    public String crearInventarioForm(Model model) {
        model.addAttribute("inventario", new Inventario());
        return "gerente/inventario/form";
    }

    @PostMapping("/inventario/crear")
    public String guardarInventario(@ModelAttribute Inventario inventario) {
        inventarioRepository.save(inventario);
        return "redirect:/gerente/inventario";
    }

    @GetMapping("/inventario/editar/{id}")
    public String editarInventario(@PathVariable Long id, Model model) {
        Inventario inventario = inventarioRepository.findById(id).orElseThrow();
        model.addAttribute("inventario", inventario);
        return "gerente/inventario/form";
    }

    @PostMapping("/inventario/editar/{id}")
    public String actualizarInventario(@PathVariable Long id, @ModelAttribute Inventario inventario) {
        inventario.setIdInventario(id);
        inventarioRepository.save(inventario);
        return "redirect:/gerente/inventario";
    }

    @PostMapping("/inventario/cambiar-estado/{id}")
    public String cambiarEstadoInventario(@PathVariable Long id) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado"));
        inventario.setActivo(!inventario.isActivo()); // Cambia el estado
        inventarioRepository.save(inventario);
        return "redirect:/gerente/inventario";
    }



    // ===============================
    // 🔔 Notificaciones
    // ===============================
    @GetMapping("/notificaciones")
    public String listarNotificaciones(
            @RequestParam(required = false) Boolean leida,
            @RequestParam(required = false) Long conductorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {

        List<Notificacion> notificaciones = notificacionRepository.findAll();

        if (leida != null) {
            notificaciones = notificaciones.stream()
                    .filter(n -> n.isLeida() == leida)
                    .toList();
        }

        if (conductorId != null) {
            notificaciones = notificaciones.stream()
                    .filter(n -> n.getConductor() != null && n.getConductor().getIdConductor().equals(conductorId))
                    .toList();
        }

        if (desde != null) {
            notificaciones = notificaciones.stream()
                    .filter(n -> n.getFecha() != null && !n.getFecha().toLocalDate().isBefore(desde))
                    .toList();
        }

        if (hasta != null) {
            notificaciones = notificaciones.stream()
                    .filter(n -> n.getFecha() != null && !n.getFecha().toLocalDate().isAfter(hasta))
                    .toList();
        }

        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("conductores", conductorRepository.findAll());
        return "gerente/notificacion/lista";
    }

    @GetMapping("/notificaciones/nuevo")
    public String crearNotificacionForm(Model model) {
        model.addAttribute("notificacion", new Notificacion());
        model.addAttribute("conductores", conductorRepository.findAll());
        return "gerente/notificacion/form";
    }

    @PostMapping("/notificaciones/guardar")
    public String guardarNotificacion(@ModelAttribute Notificacion notificacion) {
        if (notificacion.getConductor() != null && notificacion.getConductor().getIdConductor() != null) {
            Conductor conductor = conductorRepository.findById(notificacion.getConductor().getIdConductor())
                    .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado"));
            notificacion.setConductor(conductor);
        }
        notificacionRepository.save(notificacion);
        return "redirect:/gerente/notificaciones";
    }

    @GetMapping("/notificaciones/editar/{id}")
    public String editarNotificacion(@PathVariable Long id, Model model) {
        Notificacion notificacion = notificacionRepository.findById(id).orElseThrow();
        model.addAttribute("notificacion", notificacion);
        model.addAttribute("conductores", conductorRepository.findAll());
        return "gerente/notificacion/form";
    }

    @PostMapping("/notificaciones/editar/{id}")
    public String actualizarNotificacion(@PathVariable Long id, @ModelAttribute Notificacion notificacion) {
        notificacion.setIdNotificacion(id);

        if (notificacion.getConductor() != null && notificacion.getConductor().getIdConductor() != null) {
            Conductor conductor = conductorRepository.findById(notificacion.getConductor().getIdConductor())
                    .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado"));
            notificacion.setConductor(conductor);
        }

        notificacionRepository.save(notificacion);
        return "redirect:/gerente/notificaciones";
    }

    @PostMapping("/notificaciones/eliminar/{id}")
    public String eliminarNotificacion(@PathVariable Long id) {
        notificacionRepository.deleteById(id);
        return "redirect:/gerente/notificaciones";
    }

    // ===============================
    // 📢 Novedades (CRUD completo)
    // ===============================
    @GetMapping("/novedades")
    public String listarNovedades(Model model) {
        model.addAttribute("novedades", novedadRepository.findAll());
        return "gerente/novedades/listar";
    }

    @GetMapping("/novedades/crear")
    public String crearNovedadForm(Model model) {
        model.addAttribute("novedad", new Novedad());
        return "gerente/novedades/form";
    }

    @PostMapping("/novedades/guardar")
    public String guardarNovedad(@ModelAttribute Novedad novedad, @RequestParam("file") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            novedad.setEvidencia(fileName);
        }
        novedadRepository.save(novedad);
        return "redirect:/gerente/novedades";
    }

    @GetMapping("/novedades/editar/{id}")
    public String editarNovedadForm(@PathVariable Long id, Model model) {
        Novedad novedad = novedadRepository.findById(id).orElseThrow();
        model.addAttribute("novedad", novedad);
        return "gerente/novedades/form";
    }

    @PostMapping("/novedades/actualizar/{id}")
    public String actualizarNovedad(@PathVariable Long id, @ModelAttribute Novedad novedad, @RequestParam("file") MultipartFile file) throws IOException {
        Novedad existente = novedadRepository.findById(id).orElseThrow();

        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            existente.setEvidencia(fileName);
        }

        existente.setTipoNovedad(novedad.getTipoNovedad());
        existente.setDescripcion(novedad.getDescripcion());
        existente.setEstado(novedad.getEstado());
        existente.setUsuario(novedad.getUsuario());

        novedadRepository.save(existente);
        return "redirect:/gerente/novedades";
    }

    @PostMapping("/novedades/eliminar/{id}")
    public String eliminarNovedad(@PathVariable Long id) {
        novedadRepository.deleteById(id);
        return "redirect:/gerente/novedades";
    }
}
