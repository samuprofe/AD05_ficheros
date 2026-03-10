package com.example.apiempleados.services;

import com.example.apiempleados.entities.Departamento;
import com.example.apiempleados.entities.Empleado;
import com.example.apiempleados.repositories.DepartamentoRepository;
import com.example.apiempleados.repositories.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DepartamentoService {

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    public Page<Departamento> findAll(Pageable pageable) {
        return departamentoRepository.findAll(pageable);
    }

    public Optional<Departamento> findById(Long id) {
        return departamentoRepository.findById(id);
    }

    public Departamento create(Departamento departamento) {
        return departamentoRepository.save(departamento);
    }

    public Optional<Departamento> update(Long id, Departamento departamentoNuevo) {
        return departamentoRepository.findById(id).map(departamento -> {
            departamento.setNombre(departamentoNuevo.getNombre());
            return departamentoRepository.save(departamento);
        });
    }

    public boolean delete(Long id) {
        return departamentoRepository.findById(id).map(departamento -> {
            departamento.getEmpleados().forEach(empleado -> empleado.setDepartamento(null));
            departamentoRepository.delete(departamento);
            return true;
        }).orElse(false);
    }

    public Optional<Departamento> addEmpleado(Long departamentoId, Long empleadoId) {
        Optional<Departamento> departamentoOpt = departamentoRepository.findById(departamentoId);
        Optional<Empleado> empleadoOpt = empleadoRepository.findById(empleadoId);

        if (departamentoOpt.isPresent() && empleadoOpt.isPresent()) {
            Departamento departamento = departamentoOpt.get();
            Empleado empleado = empleadoOpt.get();

            empleado.setDepartamento(departamento);
            departamento.getEmpleados().add(empleado);

            empleadoRepository.save(empleado);
            return Optional.of(departamentoRepository.save(departamento));
        }

        return Optional.empty();
    }

    public Optional<Departamento> removeEmpleado(Long departamentoId, Long empleadoId) {
        Optional<Departamento> departamentoOpt = departamentoRepository.findById(departamentoId);
        Optional<Empleado> empleadoOpt = empleadoRepository.findById(empleadoId);

        if (departamentoOpt.isPresent() && empleadoOpt.isPresent()) {
            Departamento departamento = departamentoOpt.get();
            Empleado empleado = empleadoOpt.get();

            if (empleado.getDepartamento() != null && empleado.getDepartamento().getId().equals(departamentoId)) {
                empleado.setDepartamento(null);
                departamento.getEmpleados().remove(empleado);

                empleadoRepository.save(empleado);
                return Optional.of(departamentoRepository.save(departamento));
            }
        }

        return Optional.empty();
    }
}
