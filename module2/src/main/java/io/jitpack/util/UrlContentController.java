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
import io.jitpack.helper.UrlHelper;

@RestController
@RequestMapping("/get-link-content")
public class UrlContentController {

    private static final Logger logger = LoggerFactory.getLogger(UrlContentController.class);

    @GetMapping("/fetch")
    public ResponseEntity<Resource> fetchUrlContent(@RequestParam("url") String url) {
        logger.warn("Fetching content from URL: {}", url);
        url = UrlHelper.validateSafeUrl(url);
        String content = UrlFetcher.requireSafe(url);
        byte[] bytes = content.getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"content.txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }
}
