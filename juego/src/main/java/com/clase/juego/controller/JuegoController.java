package com.clase.juego.controller; // Define el paquete de la capa de controladores

import com.clase.juego.model.JuegoForm;        // Importa el modelo para capturar datos del formulario
import com.clase.juego.model.JuegoSesion;      // Importa el modelo que mantiene el estado del juego
import com.clase.juego.service.JuegoService;   // Importa el servicio con la lógica de adivinación
import jakarta.validation.Valid;               // Habilita la validación de los datos entrantes
import org.springframework.beans.factory.annotation.Autowired; // Inyección de dependencias
import org.springframework.stereotype.Controller; // Indica que es un controlador web (MVC)
import org.springframework.ui.Model;            // Se usa para enviar datos a la vista (Thymeleaf)
import org.springframework.validation.BindingResult; // Almacena los resultados de la validación
import org.springframework.web.bind.annotation.*;    // Anotaciones para manejar rutas HTTP

@Controller // Registra esta clase en el contenedor de Spring como un controlador
@RequestMapping("/juego") // Todas las rutas de este archivo comienzan con el prefijo /juego
public class JuegoController {

    @Autowired // Spring conecta automáticamente el servicio JuegoService aquí
    private JuegoService juegoService;

    // Maneja peticiones GET a "/juego". Carga la vista inicial.
    @GetMapping
    public String mostrar(Model model) {
        // Pasa la sesión de juego (actual o nula) a la vista
        model.addAttribute("sesion", juegoService.getSesion());
        // Crea un objeto formulario vacío para que Thymeleaf pueda renderizar el input
        model.addAttribute("form", new JuegoForm());
        return "juego"; // Retorna el nombre de la plantilla HTML: juego.html
    }

    // Ruta para iniciar una nueva partida. Se cambió a GET para simplificar pruebas sin CSRF.
    @GetMapping("/nuevo")
    public String nuevaPartida() {
        juegoService.nuevaPartida(); // Llama a la lógica para generar un nuevo número secreto
        return "redirect:/juego"; // Redirige a la página principal para mostrar el juego limpio
    }

    // Maneja el envío del formulario cuando el usuario intenta adivinar un número
    @PostMapping("/adivinar")
    public String adivinar(
            @Valid @ModelAttribute("form") JuegoForm form, // Valida el número según las reglas de JuegoForm
            BindingResult errores, // Contenedor que guarda si el número fue inválido (ej: fuera de rango)
            Model model) {

        // Si el formulario tiene errores (ej: campo vacío o número > 100)
        if (errores.hasErrors()) {
            // Recargamos la sesión para no perder el historial en la vista
            model.addAttribute("sesion", juegoService.getSesion());
            return "juego"; // Regresa a la página mostrando los mensajes de error de validación
        }

        // Ejecuta la lógica de adivinación y obtiene la pista (GANO, FRIO, etc.)
        String pista = juegoService.adivinar(form.getNumero());
        
        // Enviamos los resultados a la vista
        model.addAttribute("pista", pista);        // El resultado del intento (mensaje/emoji)
        model.addAttribute("intento", form.getNumero()); // El número que el usuario ingresó
        model.addAttribute("sesion", juegoService.getSesion()); // El estado actualizado (intentos, historial)
        model.addAttribute("form", new JuegoForm()); // Limpia el input enviando un formulario nuevo
        
        return "juego"; // Recarga la página juego.html con los nuevos datos
    }
}