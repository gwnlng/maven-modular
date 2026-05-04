package io.jitpack.util;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/validated-files1")
public class ValidatedFileController1 {

    private static final Logger logger = LoggerFactory.getLogger(ValidatedFileController1.class);
    private static final Path BASE_DIR = Paths.get("data").toAbsolutePath().normalize();

    @GetMapping("/get1")
    public ResponseEntity<Resource> readFileGetNE(@RequestParam("path") String path) throws IOException {
        logger.warn("Invalid file path requested: {}", path);
        Path target = Path.of(path);
        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            return ResponseEntity.notFound().build();
        }
        // test validation logic in a separate helper class
        PathTraverseHelper pathTraverseHelper = new PathTraverseHelper();
        pathTraverseHelper.initPathNameRule("[a-z]|[A-Z]:(\\[^\\/&?\n]+)\\?");
        pathTraverseHelper.initPathStarter("/upload");
        pathTraverseHelper.validate(target);

        byte[] bytes = Files.readAllBytes(target);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + target.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    @GetMapping("/get2")
    public ResponseEntity<Resource> readFileGetE(@RequestParam("path") String path) throws IOException {
        logger.warn("Invalid file path requested: {}", path);
        Path target = Paths.get(path);
        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            return ResponseEntity.notFound().build();
        }

        // test validation logic in a separate helper class
        PathTraverseHelper pathTraverseHelper = new PathTraverseHelper();
        pathTraverseHelper.initPathNameRule("[a-z]|[A-Z]:(\\[^\\/&?\n]+)\\?");
        pathTraverseHelper.initPathStarter("/upload");
        pathTraverseHelper.validatePathString(path);
        byte[] bytes = Files.readAllBytes(target);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + target.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    public void validateFileName(String path) throws IOException {
        Path target = Paths.get(path);
        PathTraverseHelper pathTraverseHelper = new PathTraverseHelper();
        pathTraverseHelper.initPathNameRule("[a-z]|[A-Z]:(\\[^\\/&?\n]+)\\?");
        pathTraverseHelper.initPathStarter("/upload");
        pathTraverseHelper.validate(target);
    }
}
