package com.example.apiempleados.controllers;

import com.example.apiempleados.dto.ProyectoConNumeroEmpleadosDTO;
import com.example.apiempleados.entities.Proyecto;
import com.example.apiempleados.services.ProyectoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ProyectosController {

    @Autowired
    private ProyectoService proyectoService;

    @GetMapping("/proyectos")
    public ResponseEntity<Page<Proyecto>> findAllProyectos(@PageableDefault(page = 0, size = 5, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(proyectoService.findAll(pageable));
    }

    @GetMapping("/proyectos/{id}")
    public ResponseEntity<Proyecto> findProyecto(@PathVariable Long id) {
        return proyectoService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/proyectos")
    public ResponseEntity<Proyecto> createProyecto(@Valid @RequestBody Proyecto proyecto) {
        return ResponseEntity.status(201).body(proyectoService.create(proyecto));
    }

    @PutMapping("/proyectos/{id}")
    public ResponseEntity<Proyecto> updateProyecto(@Valid @RequestBody Proyecto proyectoNuevo, @PathVariable Long id) {
        return proyectoService.update(id, proyectoNuevo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/proyectos/{id}")
    public ResponseEntity<Object> deleteProyecto(@PathVariable Long id) {
        if (proyectoService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/proyectos/{proyectoId}/empleados/{empleadoId}")
    public ResponseEntity<Proyecto> addEmpleadoToProyecto(@PathVariable Long proyectoId, @PathVariable Long empleadoId) {
        return proyectoService.addEmpleado(proyectoId, empleadoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/proyectos/{proyectoId}/empleados/{empleadoId}")
    public ResponseEntity<Proyecto> removeEmpleadoFromProyecto(@PathVariable Long proyectoId, @PathVariable Long empleadoId) {
        return proyectoService.removeEmpleado(proyectoId, empleadoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/proyectos/resumen")
    public ResponseEntity<List<ProyectoConNumeroEmpleadosDTO>> obtenerProyectosResumen() {
        return ResponseEntity.ok(proyectoService.getResumen());
    }
}
