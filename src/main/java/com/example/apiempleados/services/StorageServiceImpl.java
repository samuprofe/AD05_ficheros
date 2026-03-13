package com.example.apiempleados.services;

import com.example.apiempleados.exception.StorageException;
import com.example.apiempleados.exception.StorageNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path ubicacion;

    // @Value inyecta el valor de la propiedad en el parámetro del constructor
    // La sintaxis ${nombre.propiedad} busca en application.properties
    public StorageServiceImpl(@Value("${app.storage.location}") String ruta) {
        this.ubicacion = Paths.get(ruta);
    }

    // 1. Spring crea el objeto StorageServiceImpl
    // 2. Spring inyecta @Value("${app.storage.location}") → this.ubicacion
    // 3. Spring llama a inicializar()  ← @PostConstruct
    // 4. La aplicación empieza a atender peticiones HTTP
    @Override
    @PostConstruct
    public void inicializar() {
        try {
            // createDirectories crea la carpeta; si ya existe no lanza excepción
            Files.createDirectories(ubicacion);
        } catch (IOException e) {
            throw new StorageException("No se pudo crear la carpeta de almacenamiento", e);
        }
    }

    @Override
    public String guardar(MultipartFile fichero) {
        if (fichero.isEmpty()) {
            throw new StorageException("No se puede guardar un fichero vacío");
        }
        // Usamos UUID para evitar colisiones de nombres
        String nombreOriginal = fichero.getOriginalFilename();
        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        String nombreFinal = UUID.randomUUID().toString() + extension;
        try {
            // Combina la ruta con el archivo para obtener el Path destino
            Path destino = ubicacion.resolve(nombreFinal);
            Files.copy(fichero.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return nombreFinal;
        } catch (IOException e) {
            throw new StorageException("Error al guardar el fichero: " + nombreOriginal, e);
        }
    }

    @Override
    public Resource cargar(String nombreFichero) {
        try {
            // Combinamos carpeta y nombre de archivo y normalizamos por seguridad
            Path fichero = ubicacion.resolve(nombreFichero).normalize();
            // toUri convierte el Path a una ruta completa para poder crear Resource
            Resource recurso = new UrlResource(fichero.toUri());
            if (!recurso.exists() || !recurso.isReadable()) {
                throw new StorageNotFoundException("Fichero no encontrado: " + nombreFichero);
            }
            return recurso;
        } catch (MalformedURLException e) {
            throw new StorageNotFoundException("Ruta de fichero inválida: " + nombreFichero, e);
        }
    }

    @Override
    public void borrar(String nombreFichero) {
        try {
            // Combina carpeta y nombre de archivo
            Path fichero = ubicacion.resolve(nombreFichero).normalize();
            Files.deleteIfExists(fichero);
        } catch (IOException e) {
            throw new StorageException("Error al borrar el fichero: " + nombreFichero, e);
        }
    }

    @Override
    public List<String> listarFicheros() {
        try {
            return Files.list(ubicacion)
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new StorageException("Error al listar ficheros", e);
        }
    }
}
