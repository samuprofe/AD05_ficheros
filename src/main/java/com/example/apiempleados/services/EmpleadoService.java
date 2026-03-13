package com.example.apiempleados.services;

import com.example.apiempleados.entities.Empleado;
import com.example.apiempleados.repositories.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private StorageService storageService;

    public Page<Empleado> findAll(Pageable pageable) {
        return empleadoRepository.findAll(pageable);
    }

    public Optional<Empleado> findById(Long id) {
        return empleadoRepository.findById(id);
    }

    public Empleado create(Empleado empleado) {
        try {
            return empleadoRepository.save(empleado);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("La dirección de email ya existe en la base de datos");
        }
    }

    public Optional<Empleado> update(Long id, Empleado empleadoNuevo) {
        return empleadoRepository.findById(id).map(empleado -> {
            empleado.setNombre(empleadoNuevo.getNombre());
            empleado.setApellidos(empleadoNuevo.getApellidos());
            empleado.setEmail(empleadoNuevo.getEmail());
            empleado.setFechaNacimiento(empleadoNuevo.getFechaNacimiento());
            return empleadoRepository.save(empleado);
        });
    }

    public boolean delete(Long id) {
        return empleadoRepository.findById(id).map(empleado -> {
            // Borrar la foto del disco si existe
            if (empleado.getRutaFoto() != null) {
                storageService.borrar(empleado.getRutaFoto());
            }
            empleado.getProyectos().forEach(proyecto -> proyecto.getEmpleados().remove(empleado));
            empleado.getProyectos().clear();
            empleadoRepository.delete(empleado);
            return true;
        }).orElse(false);
    }

    // Actualiza el campo rutaFoto del empleado (usado por el controlador tras guardar/borrar la foto)
    public Optional<Empleado> actualizarFoto(Long id, String rutaFoto) {
        return empleadoRepository.findById(id).map(empleado -> {
            empleado.setRutaFoto(rutaFoto);
            return empleadoRepository.save(empleado);
        });
    }
}
