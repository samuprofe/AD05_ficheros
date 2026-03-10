package com.example.apiempleados.services;

import com.example.apiempleados.dto.ProyectoConNumeroEmpleadosDTO;
import com.example.apiempleados.entities.Empleado;
import com.example.apiempleados.entities.Proyecto;
import com.example.apiempleados.repositories.EmpleadoRepository;
import com.example.apiempleados.repositories.ProyectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProyectoService {

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    public Page<Proyecto> findAll(Pageable pageable) {
        return proyectoRepository.findAll(pageable);
    }

    public Optional<Proyecto> findById(Long id) {
        return proyectoRepository.findById(id);
    }

    public Proyecto create(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    public Optional<Proyecto> update(Long id, Proyecto proyectoNuevo) {
        return proyectoRepository.findById(id).map(proyecto -> {
            proyecto.setNombre(proyectoNuevo.getNombre());
            proyecto.setFechaInicio(proyectoNuevo.getFechaInicio());
            proyecto.setDescripcion(proyectoNuevo.getDescripcion());
            proyecto.setEstado(proyectoNuevo.getEstado());
            proyecto.setPresupuesto(proyectoNuevo.getPresupuesto());
            return proyectoRepository.save(proyecto);
        });
    }

    public boolean delete(Long id) {
        return proyectoRepository.findById(id).map(proyecto -> {
            proyecto.getEmpleados().forEach(empleado -> empleado.getProyectos().remove(proyecto));
            proyecto.getEmpleados().clear();
            proyectoRepository.delete(proyecto);
            return true;
        }).orElse(false);
    }

    public Optional<Proyecto> addEmpleado(Long proyectoId, Long empleadoId) {
        Optional<Proyecto> proyectoOpt = proyectoRepository.findById(proyectoId);
        Optional<Empleado> empleadoOpt = empleadoRepository.findById(empleadoId);

        if (proyectoOpt.isPresent() && empleadoOpt.isPresent()) {
            Proyecto proyecto = proyectoOpt.get();
            Empleado empleado = empleadoOpt.get();

            if (!proyecto.getEmpleados().contains(empleado)) {
                proyecto.getEmpleados().add(empleado);
            }

            return Optional.of(proyectoRepository.save(proyecto));
        }

        return Optional.empty();
    }

    public Optional<Proyecto> removeEmpleado(Long proyectoId, Long empleadoId) {
        Optional<Proyecto> proyectoOpt = proyectoRepository.findById(proyectoId);
        Optional<Empleado> empleadoOpt = empleadoRepository.findById(empleadoId);

        if (proyectoOpt.isPresent() && empleadoOpt.isPresent()) {
            Proyecto proyecto = proyectoOpt.get();
            Empleado empleado = empleadoOpt.get();

            if (proyecto.getEmpleados().contains(empleado)) {
                proyecto.getEmpleados().remove(empleado);
                return Optional.of(proyectoRepository.save(proyecto));
            }
        }

        return Optional.empty();
    }

    public List<ProyectoConNumeroEmpleadosDTO> getResumen() {
        return proyectoRepository.obtenerProyectosConNumeroDeEmpleadosDto();
    }
}
