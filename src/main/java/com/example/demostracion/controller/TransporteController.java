package com.example.demostracion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TransporteController {

    @GetMapping("/transporte")
    public String transporteMenu(Model model) {
        return "gerente/transporte/transporte";
    }

    @GetMapping("/transporte/seguimiento")
    public String seguimientoEnvios(Model model) {
        return "gerente/transporte/seguimiento";
    }

    @GetMapping("/transporte/programacion")
    public String programarTransporte(Model model) {
        return "gerente/transporte/programacion";
    }

    @GetMapping("/transporte/documentos")
    public String documentos(Model model) {
        return "gerente/transporte/documentosTransporte";
    }

    @GetMapping("/transporte/detalle/{id}")
    public String detalleEnvio(Model model) {
        return "gerente/transporte/detalleTransporte";
    }

    // 🔥 MÉTODO POST QUE NECESITAS
    @PostMapping("/transporte/programar")
    public String guardarProgramacion(
            @RequestParam Long envioId,
            @RequestParam String camion,
            @RequestParam String conductor,
            @RequestParam String fecha,
            RedirectAttributes redirectAttributes
    ) {

        // TODO → Guardar en base de datos si lo deseas

        redirectAttributes.addFlashAttribute("mensaje", "✔ Programación realizada con éxito");

        // Redirige para recargar la página y limpiar el formulario
        return "redirect:/transporte/programacion";
    }
}
