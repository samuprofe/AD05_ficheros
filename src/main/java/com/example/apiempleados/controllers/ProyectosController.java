package com.example.apiempleados.controllers;

import com.example.apiempleados.dto.ProyectoConNumeroEmpleadosDTO;
import com.example.apiempleados.entities.FicheroProyecto;
import com.example.apiempleados.entities.Proyecto;
import com.example.apiempleados.repositories.FicheroProyectoRepository;
import com.example.apiempleados.services.ProyectoService;
import com.example.apiempleados.services.StorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ProyectosController {

    @Autowired
    private ProyectoService proyectoService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private FicheroProyectoRepository ficheroProyectoRepository;

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

    // POST /proyectos/{id}/ficheros
    // En Postman: Body → form-data → key="files" (tipo File, permite seleccionar varios)
    @PostMapping("/proyectos/{id}/ficheros")
    public ResponseEntity<List<FicheroProyecto>> subirFicheros(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> ficheros) {
        return proyectoService.findById(id).map(proyecto -> {
            List<FicheroProyecto> guardados = ficheros.stream().map(f -> {
                String nombreGuardado = storageService.guardar(f);
                FicheroProyecto fp = new FicheroProyecto();
                fp.setNombreFichero(nombreGuardado);
                fp.setNombreOriginal(f.getOriginalFilename());
                fp.setProyecto(proyecto);
                return ficheroProyectoRepository.save(fp);
            }).collect(Collectors.toList());
            return ResponseEntity.status(201).body(guardados);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /proyectos/{id}/ficheros
    @GetMapping("/proyectos/{id}/ficheros")
    public ResponseEntity<List<FicheroProyecto>> getFicheros(@PathVariable Long id) {
        return proyectoService.findById(id)
                .map(p -> ResponseEntity.ok(p.getFicheros()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /proyectos/{id}/ficheros/{ficheroId}
    // Content-Disposition: inline → el navegador abre el PDF en el visor integrado
    @GetMapping("/proyectos/{id}/ficheros/{ficheroId}")
    public ResponseEntity<Resource> descargarFichero(
            @PathVariable Long id,
            @PathVariable Long ficheroId) {
        return ficheroProyectoRepository.findById(ficheroId)
                .filter(f -> f.getProyecto().getId() == id)
                .map(f -> {
                    Resource recurso = storageService.cargar(f.getNombreFichero());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "inline; filename=\"" + f.getNombreOriginal() + "\"")
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(recurso);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /proyectos/{id}/ficheros/{ficheroId}
    @DeleteMapping("/proyectos/{id}/ficheros/{ficheroId}")
    public ResponseEntity<Void> deleteFichero(
            @PathVariable Long id,
            @PathVariable Long ficheroId) {
        return ficheroProyectoRepository.findById(ficheroId)
                .filter(f -> f.getProyecto().getId() == id)
                .map(f -> {
                    storageService.borrar(f.getNombreFichero());
                    ficheroProyectoRepository.delete(f);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
