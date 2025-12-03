package com.preetinest.impl;


import com.preetinest.entity.Image;
import com.preetinest.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    public Image saveImage(String fileName, String url) {
        Image image = new Image();
        image.setFileName(fileName);
        image.setUrl(url);
        return imageRepository.save(image);
    }
}