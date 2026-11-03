package com.sloth.OnlyStudent.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sloth.OnlyStudent.shared.errors.InvalidImageException;

@Service
public class CloudinaryImageService {

    private final Cloudinary cloudinary;
    private final boolean configured;

    public CloudinaryImageService(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("cloud_name", cloudName);
        configuration.put("api_key", apiKey);
        configuration.put("api_secret", apiSecret);
        this.cloudinary = new Cloudinary(configuration);
        this.configured = !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
    }

    public String upload(MultipartFile image, Long classroomId, String imageType) {
        if (image == null || image.isEmpty()) {
            throw new InvalidImageException("A imagem é obrigatória.");
        }
        if (!configured) {
            throw new InvalidImageException("O serviço de imagens não está configurado.");
        }
        if (image.getContentType() == null || !image.getContentType().toLowerCase().startsWith("image/")) {
            throw new InvalidImageException("O arquivo deve ser uma imagem.");
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.asMap(
                    "folder", "onlystudent/classrooms/" + classroomId,
                    "public_id", imageType,
                    "resource_type", "image",
                    "overwrite", true,
                    "invalidate", true));
            Object secureUrl = result.get("secure_url");
            if (secureUrl == null || secureUrl.toString().isBlank()) {
                throw new InvalidImageException("O Cloudinary não retornou uma URL pública para a imagem.");
            }
            return secureUrl.toString();
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof InvalidImageException invalidImageException) {
                throw invalidImageException;
            }
            throw new InvalidImageException("Não foi possível enviar a imagem.", exception);
        }
    }
}
