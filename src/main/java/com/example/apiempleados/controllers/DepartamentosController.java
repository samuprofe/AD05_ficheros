package com.example.apiempleados.controllers;

import com.example.apiempleados.entities.Departamento;
import com.example.apiempleados.services.DepartamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class DepartamentosController {

    @Autowired
    private DepartamentoService departamentoService;

    @GetMapping("/departamentos")
    public ResponseEntity<Page<Departamento>> findAllDepartamentos(@PageableDefault(page = 0, size = 5, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(departamentoService.findAll(pageable));
    }

    @GetMapping("/departamentos/{id}")
    public ResponseEntity<Departamento> findDepartamento(@PathVariable Long id) {
        return departamentoService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/departamentos")
    public ResponseEntity<Departamento> createDepartamento(@RequestBody Departamento departamento) {
        return ResponseEntity.status(201).body(departamentoService.create(departamento));
    }

    @PutMapping("/departamentos/{id}")
    public ResponseEntity<Departamento> updateDepartamento(@RequestBody Departamento departamentoNuevo, @PathVariable Long id) {
        return departamentoService.update(id, departamentoNuevo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/departamentos/{id}")
    public ResponseEntity<Object> deleteDepartamento(@PathVariable Long id) {
        if (departamentoService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/departamentos/{departamentoId}/empleados/{empleadoId}")
    public ResponseEntity<Departamento> addEmpleadoToDepartamento(@PathVariable Long departamentoId, @PathVariable Long empleadoId) {
        return departamentoService.addEmpleado(departamentoId, empleadoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/departamentos/{departamentoId}/empleados/{empleadoId}")
    public ResponseEntity<Departamento> removeEmpleadoFromDepartamento(@PathVariable Long departamentoId, @PathVariable Long empleadoId) {
        return departamentoService.removeEmpleado(departamentoId, empleadoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
