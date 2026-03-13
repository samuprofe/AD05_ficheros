package com.example.apiempleados.controllers;

import com.example.apiempleados.entities.Empleado;
import com.example.apiempleados.repositories.EmpleadoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class EmpleadosController {

    @Autowired
    EmpleadoRepository empleadoRepository;

    @GetMapping("/empleados")
    public ResponseEntity<Page<Empleado>> findAllEmpleados(@PageableDefault(page = 0, size = 5, sort = "apellidos", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(empleadoRepository.findAll(pageable));
    }

    @GetMapping("/empleados/{id}")
    public ResponseEntity<Empleado> findEmpleado(@PathVariable Long id){
        return empleadoRepository.findById(id)
                .map(empleado -> ResponseEntity.ok(empleado))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/empleados/{id}")
    public ResponseEntity<Object> deleteEmpleado(@PathVariable Long id){
        return empleadoRepository.findById(id)
                .map(empleado -> {
                    //Borramos las relaciones con los proyectos
                    empleado.getProyectos().forEach(proyecto -> {proyecto.getEmpleados().remove(empleado);});
                    empleado.getProyectos().clear();

                    empleadoRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() ->{
                    return  ResponseEntity.notFound().build();
                });

//         Optional<Empleado> empleadOptl = empleadoRepository.findById(id);
//         if (empleadOptl.isPresent()){

//             empleadoRepository.deleteById(id);
//             return ResponseEntity.noContent().build();
//         }
//         else{
//             return  ResponseEntity.notFound().build();
//         }

    }

    @PostMapping("/empleados")
    public ResponseEntity<?> createEmpleado(@Valid @RequestBody Empleado empleado){
        //Una mejora posible sería retornar la URI del recurso creado.
        //Lo simplificamos por el momento devolviendo el código 201(created) y el empleado creado en el cuerpo
        try {
            return ResponseEntity.status(301).body(empleadoRepository.save(empleado));
        }catch (DataIntegrityViolationException e){
            return ResponseEntity.badRequest().body("La dirección de email ya existe en la base de datos");
        }
    }

    @PutMapping("/empleados/{id}")
    public ResponseEntity<Empleado> updateEmpleado(@Valid @RequestBody Empleado empleadoNuevo, @PathVariable Long id){
        Optional<Empleado> empleado = empleadoRepository.findById(id);
        if(empleado.isPresent()){
            empleado.get().setApellidos(empleadoNuevo.getApellidos());
            empleado.get().setEmail(empleadoNuevo.getEmail());
            empleado.get().setNombre(empleadoNuevo.getNombre());
            empleado.get().setFechaNacimiento(empleadoNuevo.getFechaNacimiento());
            empleadoRepository.save(empleado.get());
            return ResponseEntity.ok(empleado.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/empleados/export")
    public ResponseEntity<byte[]> exportEmpleados() {
        List<Empleado> empleados = empleadoRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("id,email,nombre,apellidos,fechaNacimiento\n");
        for (Empleado empleado : empleados) {
            csv.append(csvField(String.valueOf(empleado.getId()))).append(",")
               .append(csvField(empleado.getEmail())).append(",")
               .append(csvField(empleado.getNombre())).append(",")
               .append(csvField(empleado.getApellidos())).append(",")
               .append(csvField(empleado.getFechaNacimiento() != null ? empleado.getFechaNacimiento().toString() : ""))
               .append("\n");
        }

        byte[] data = csv.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"empleados.csv\"");
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @PostMapping("/empleados/import")
    public ResponseEntity<?> importEmpleados(@RequestParam("file") MultipartFile file) {
        try {
            List<Empleado> importados = new ArrayList<>();
            List<String> advertencias = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

                String linea;
                boolean primeraLinea = true;
                int numeroLinea = 0;
                while ((linea = reader.readLine()) != null) {
                    numeroLinea++;
                    if (primeraLinea) {
                        primeraLinea = false; // Saltamos la cabecera
                        continue;
                    }
                    linea = linea.trim();
                    if (linea.isEmpty()) continue;

                    String[] campos = linea.split(",", -1);
                    if (campos.length < 4) {
                        advertencias.add("Línea " + numeroLinea + " ignorada: número de campos insuficiente");
                        continue;
                    }

                    try {
                        Empleado empleado = new Empleado();
                        // campos[0] es el id; lo ignoramos para que la BD genere uno nuevo
                        empleado.setEmail(unquoteCsvField(campos[1]));
                        empleado.setNombre(unquoteCsvField(campos[2]));
                        empleado.setApellidos(unquoteCsvField(campos[3]));
                        if (campos.length > 4 && !unquoteCsvField(campos[4]).isEmpty()) {
                            empleado.setFechaNacimiento(LocalDate.parse(unquoteCsvField(campos[4])));
                        }
                        importados.add(empleado);
                    } catch (Exception e) {
                        advertencias.add("Línea " + numeroLinea + " ignorada: " + e.getMessage());
                    }
                }
            }

            List<Empleado> guardados = empleadoRepository.saveAll(importados);
            if (advertencias.isEmpty()) {
                return ResponseEntity.ok(guardados);
            }
            return ResponseEntity.ok(java.util.Map.of("importados", guardados, "advertencias", advertencias));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Error: algún email del fichero ya existe en la base de datos");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al importar el fichero: " + e.getMessage());
        }
    }

    private String csvField(String value) {
        if (value == null) return "\"\"";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private String unquoteCsvField(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).replace("\"\"", "\"");
        }
        return trimmed;
    }
