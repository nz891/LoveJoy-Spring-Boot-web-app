package com.love.joy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService
{
    private final Path uploadDir;

    //constructor sets the upload directory and makes sure it exists
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir)
    {
        //creating folder if its not there
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try { Files.createDirectories(this.uploadDir); } catch (IOException ignored) {}
    }

    //to store uploaded files safely on the server
    public String storeFile(MultipartFile file) throws IOException
    {
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = "";
        int i = original.lastIndexOf('.');
        if (i >= 0) ext = original.substring(i);
        String filename = UUID.randomUUID().toString() + ext;

        //check on mime make sure it's actually an image
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png")))
        {
            throw new IOException("Only PNG/JPEG images are allowed");
        }

        //size limit
        if (file.getSize() > 5 * 1024 * 1024)
            throw new IOException("File is too large :( ");

        // create a full path for where the file will be saved
        Path target = uploadDir.resolve(filename);

        //copy uploaded file into my uploads folder
        try (InputStream in = file.getInputStream())
        {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return filename;
    }

    public Path load(String filename) { return uploadDir.resolve(filename).normalize(); }
}
