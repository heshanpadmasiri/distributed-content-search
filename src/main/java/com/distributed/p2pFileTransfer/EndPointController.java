package com.distributed.p2pFileTransfer;

import com.google.gson.Gson;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class EndPointController {

    private Storage fileStorage;
    private Logger logger;

    @PostConstruct
    public void initialize() {
        fileStorage = new Storage(Configuration.getCacheDir(), Configuration.getLocalDir(), Configuration.getCacheSize(), this.getClass().getName());
        logger = Logger.getLogger(FileHandler.class.getName());
    }

    /**
     * Serve the requested File
     *
     * @param res      response
     * @param fileName Name of the file
     * @return ResponseEntity with File
     * @throws IOException
     */
    @RequestMapping("/file/{name:.+}")
    public ResponseEntity<Resource> downloadFile(HttpServletResponse res, @PathVariable("name") String fileName) throws IOException {

        //Re-convert the underscores in the file to spaces
        String originalFileName = fileName.replaceAll("_", " ");
        logger.log(Level.INFO, "Attempting to download " + originalFileName);
        Boolean fileExists = fileStorage.checkFileExists(originalFileName);

        if (fileExists) {
            String mimeType = "application/octet-stream";
            res.setContentType(mimeType);
            File file = fileStorage.generateRandomFile(originalFileName);
            res.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
            res.setContentLength((int) file.length());

            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            String hexHash = fileStorage.getFileHash(file);
            res.setHeader("Hash", hexHash);

            logger.log(Level.INFO, "Serving file from the server");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping("/search/{name:.+}")
    public ResponseEntity<String> searchFile(HttpServletResponse res, @PathVariable("name") String fileName) {
        String originalFileName = fileName.replaceAll("_", "_");
        List<String> matchingNames = fileStorage.searchForFile(originalFileName);
        String json = new Gson().toJson(matchingNames);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }
}
