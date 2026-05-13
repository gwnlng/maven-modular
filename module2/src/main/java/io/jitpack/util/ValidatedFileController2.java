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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@RestController
@RequestMapping("/validated-files2")
public class ValidatedFileController2 {

    private static final Logger logger = LoggerFactory.getLogger(ValidatedFileController2.class);
    private static final Path BASE_DIR = Paths.get("data").toAbsolutePath().normalize();

    @GetMapping("/get3")
    public ResponseEntity<Resource> readFileGetE(@RequestParam("path") String path) throws IOException {
        logger.warn("Invalid file path requested: {}", path);
        Path target = Paths.get(path);
        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            return ResponseEntity.notFound().build();
        }

        // test validation logic within class
        this.validateFilePath(target);
        byte[] bytes = Files.readAllBytes(target);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + target.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    public void validateFilePath(Path path) throws IOException, PatternSyntaxException {
        Pattern pathNameRule = Pattern.compile("[a-z]|[A-Z]:(\\[^\\/&?\n]+)\\?");
        String pathStarter = "/upload";
        if (path == null || path.toString().isBlank()) {
            throw new IOException("Path is null or blank");
        }

        if (path.toString().contains("\0")) {
            throw new IOException("Path contains null byte");
        }

        if (pathNameRule != null && !pathNameRule.matcher(path.toString()).matches()) {
            throw new IOException("Path does not match name rule");
        }

        Path requestedPath = path.normalize();
        if (containsPathTraversal(requestedPath)) {
            throw new IOException("Path contains path traversal");
        }

        if (pathStarter != null && !startsWithStarter(requestedPath, pathStarter)) {
            throw new IOException("Path does not start with required prefix");
        }

        if (!Files.exists(requestedPath) || !Files.isRegularFile(requestedPath)) {
            throw new IOException("Path does not exist or is not a regular file");
        }
    }

    private boolean containsPathTraversal(Path normalizedPath) {
        for (Path part : normalizedPath) {
            if ("..".equals(part.toString())) {
                return true;
            }
        }
        return false;
    }

    private boolean startsWithStarter(Path requestedPath, String starter) {
        Path starterPath = Paths.get(starter).normalize();

        if (requestedPath.isAbsolute() != starterPath.isAbsolute()) {
            return false;
        }

        return requestedPath.startsWith(starterPath);
    }
}
