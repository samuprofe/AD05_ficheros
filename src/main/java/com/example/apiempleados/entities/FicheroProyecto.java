package com.example.apiempleados.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ficheros_proyecto")
public class FicheroProyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreFichero;    // nombre UUID guardado en disco
    private String nombreOriginal;   // nombre original del fichero subido

    @JsonIgnore  // evita el bucle infinito al serializar
    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;
}
