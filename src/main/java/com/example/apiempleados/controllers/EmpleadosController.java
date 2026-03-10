package com.example.apiempleados.controllers;

import com.example.apiempleados.entities.Empleado;
import com.example.apiempleados.services.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class EmpleadosController {

    @Autowired
    private EmpleadoService empleadoService;

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
}
