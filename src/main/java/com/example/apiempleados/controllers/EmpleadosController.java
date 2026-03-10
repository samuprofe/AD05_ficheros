package com.example.apiempleados.controllers;

import com.example.apiempleados.entities.Empleado;
import com.example.apiempleados.services.EmpleadoService;
import com.example.apiempleados.services.StorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class EmpleadosController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private StorageService storageService;

    @GetMapping("/empleados")
    public ResponseEntity<Page<Empleado>> findAllEmpleados(@PageableDefault(page = 0, size = 5, sort = "apellidos", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(empleadoService.findAll(pageable));
    }

    @GetMapping("/empleados/{id}")
    public ResponseEntity<Empleado> findEmpleado(@PathVariable Long id) {
        return empleadoService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/empleados")
    public ResponseEntity<?> createEmpleado(@Valid @RequestBody Empleado empleado) {
        try {
            return ResponseEntity.status(201).body(empleadoService.create(empleado));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("La dirección de email ya existe en la base de datos");
        }
    }

    @PutMapping("/empleados/{id}")
    public ResponseEntity<Empleado> updateEmpleado(@Valid @RequestBody Empleado empleadoNuevo, @PathVariable Long id) {
        return empleadoService.update(id, empleadoNuevo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/empleados/{id}")
    public ResponseEntity<Object> deleteEmpleado(@PathVariable Long id) {
        if (empleadoService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // POST /empleados/{id}/foto
    // En Postman: Body → form-data → key="file" (tipo File)
    @PostMapping("/empleados/{id}/foto")
    public ResponseEntity<Empleado> subirFoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile foto) {
        return empleadoService.findById(id).map(empleado -> {
            // Si ya tenía foto, borrar la anterior del disco
            if (empleado.getRutaFoto() != null) {
                storageService.borrar(empleado.getRutaFoto());
            }
            String nombreGuardado = storageService.guardar(foto);
            // Actualizamos el nombre del fichero en la BD
            return ResponseEntity.ok(empleadoService.actualizarFoto(id, nombreGuardado).get());
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /empleados/{id}/foto
    // Content-Disposition: inline → el navegador muestra la imagen directamente
    @GetMapping("/empleados/{id}/foto")
    public ResponseEntity<Resource> getFoto(@PathVariable Long id) {
        return empleadoService.findById(id)
                .filter(e -> e.getRutaFoto() != null)
                .map(e -> {
                    Resource recurso = storageService.cargar(e.getRutaFoto());
                    // Detectamos el MediaType a partir del nombre del fichero
                    MediaType mediaType = MediaTypeFactory.getMediaType(recurso)
                            .orElse(MediaType.APPLICATION_OCTET_STREAM);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "inline; filename=\"" + recurso.getFilename() + "\"")
                            .contentType(mediaType)
                            .body(recurso);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /empleados/{id}/foto
    @DeleteMapping("/empleados/{id}/foto")
    public ResponseEntity<Void> deleteFoto(@PathVariable Long id) {
        return empleadoService.findById(id)
                .filter(e -> e.getRutaFoto() != null)
                .map(e -> {
                    storageService.borrar(e.getRutaFoto());
                    empleadoService.actualizarFoto(id, null);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
