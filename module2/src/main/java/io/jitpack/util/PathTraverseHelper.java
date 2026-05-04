package io.jitpack.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PathTraverseHelper {

    private Pattern pathNameRule;
    private String pathStarter;

    public void initPathNameRule(String regex) {
        if (regex == null || regex.isBlank()) {
            this.pathNameRule = null;
            return;
        }

        try {
            this.pathNameRule = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid path name rule pattern", e);
        }
    }

    public void initPathStarter(String starter) {
        if (starter == null) {
            this.pathStarter = null;
            return;
        }

        String normalizedStarter = starter.trim();
        if (normalizedStarter.isEmpty()) {
            this.pathStarter = null;
            return;
        }

        this.pathStarter = normalizedStarter;
    }

    public void validate(Path path) throws IOException {
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

    public void validatePathString(String pathString) throws IOException {
        if (pathString == null || pathString.isBlank()) {
            throw new IOException("Path is null or blank");
        }

        Path path = Paths.get(pathString);
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
