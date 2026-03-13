package com.example.apiempleados.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "empleados")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String nombre;
    @NotBlank
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String rutaFoto;  // Nombre UUID del fichero de imagen en el servidor

    //Relación con Departamento
    @JsonIgnore //Para que Jackson no serielice esta propiedad a JSON y no se forme un bucle infinito
    @ManyToOne
    private Departamento departamento;

    //Relación con Proyecto
    @JsonIgnore
    @ManyToMany(mappedBy = "empleados")
    private List<Proyecto> proyectos;

}
