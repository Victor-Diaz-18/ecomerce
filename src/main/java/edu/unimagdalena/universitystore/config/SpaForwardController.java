package edu.unimagdalena.universitystore.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Cuando el frontend va empaquetado en el jar, las rutas de React Router (/products, /orders...)
 * no existen en el servidor; se reenvían a index.html para que React las resuelva.
 * Se excluyen la API, Swagger y Actuator, y cualquier ruta con punto (archivos estáticos).
 */
@Controller
public class SpaForwardController {

    @GetMapping("/{path:^(?!api|swagger-ui|v3|actuator)[^.]*}")
    public String forward() {
        return "forward:/index.html";
    }
}
