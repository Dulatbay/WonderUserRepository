package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.exceptions.StorageException;
import kz.wonder.wonderuserrepository.services.FileService;
import kz.wonder.wonderuserrepository.validators.ValidFile;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.UPLOADED_FOLDER;


@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private final Path rootLocation;

    public FileServiceImpl() {
        this.rootLocation = Paths.get(UPLOADED_FOLDER);
    }

    @Override
    public String save(@ValidFile MultipartFile file) {
        try {
            if (file.getOriginalFilename() == null || file.isEmpty()) {
                throw new StorageException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "Failed to store empty file.");
            }

            String filename = UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());


            Path destinationFile = this.rootLocation.resolve(
                            Paths.get(filename))
                    .normalize().toAbsolutePath();


            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new StorageException(HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Cannot store file outside current directory.");
            }

            @Cleanup
            InputStream inputStream = file.getInputStream();
            Files.copy(inputStream, destinationFile,
                    StandardCopyOption.REPLACE_EXISTING);


            return filename;
        } catch (IOException e) {
            log.error("IOException: ", e);
            throw new StorageException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "Failed to store file.");
        }
    }

    private Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String fileName) {
        try {
            Path file = load(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageException(HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Could not read file: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new StorageException(HttpStatus.NOT_FOUND.getReasonPhrase(),
                    "Could not read file: " + fileName);
        }
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            log.info("Create dir: {}", rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage.\nIOException: ", e);
        }
    }

    @Override
    public void deleteByName(String filename) {
        try {

            Path filePath = rootLocation.resolve(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", filename);
            } else {
                log.warn("File not found: {}", filename);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }
}
