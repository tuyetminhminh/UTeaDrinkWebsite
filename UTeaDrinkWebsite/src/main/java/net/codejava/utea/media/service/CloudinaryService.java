package net.codejava.utea.media.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    Map<String, Object> upload(MultipartFile file) throws IOException;

    Map<String, Object> upload(MultipartFile file, String folder, String publicId) throws IOException;

    void delete(String publicId) throws IOException;
}
