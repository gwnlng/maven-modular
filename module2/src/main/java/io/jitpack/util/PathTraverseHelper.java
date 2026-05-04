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

    public boolean validate(String path) throws IOException {
        if (path == null || path.isBlank()) {
            return false;
        }

        if (path.contains("\0")) {
            return false;
        }

        if (pathNameRule != null && !pathNameRule.matcher(path).matches()) {
            return false;
        }

        Path requestedPath = Paths.get(path).normalize();
        if (containsPathTraversal(requestedPath)) {
            return false;
        }

        if (pathStarter != null && !startsWithStarter(requestedPath, pathStarter)) {
            return false;
        }

        return Files.exists(requestedPath) && Files.isRegularFile(requestedPath);
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
