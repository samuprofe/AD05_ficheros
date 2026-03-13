package com.example.apiempleados.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {
    void inicializar();                          // Crear la carpeta si no existe
    String guardar(MultipartFile fichero);       // Guarda y devuelve el nombre final (UUID)
    Resource cargar(String nombreFichero);       // Devuelve el fichero como Resource
    void borrar(String nombreFichero);           // Borra el fichero del disco
    List<String> listarFicheros();               // Lista todos los nombres de fichero
}
