package com.example.apiempleados.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "proyectos")
public class Proyecto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotBlank
    private String nombre;
    private LocalDate fechaInicio;
    @Column(length = 2000)
    private String descripcion;
    private String estado;
    private BigDecimal presupuesto;

    @CreationTimestamp
    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    // PDFs adjuntos al proyecto — cascade ALL: al borrar el proyecto se borran las entidades de BBDD
    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FicheroProyecto> ficheros = new ArrayList<>();

    @ManyToMany
    @JoinTable(   //Opcional para personalizar la tabla intermedia
            name = "proyectos_empleados", // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "proyecto_id"),
            inverseJoinColumns = @JoinColumn(name = "empleado_id")
    )
    private List<Empleado> empleados;

}
