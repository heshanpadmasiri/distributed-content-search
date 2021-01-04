package com.distributed.p2pFileTransfer;

import com.google.gson.Gson;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

@RestController
public class EndPointController {

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
        // TODO: Get upload directory from config
        //String filepath = "/home/kalana/distributed/content/uploads/"+ fileName;

        System.out.println("Attempting to download " + fileName);
        File file = Storage.getFile(fileName);

        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        if (mimeType == null) {
            //unknown mimetype so set the mimetype to application/octet-stream
            mimeType = "application/octet-stream";
        }
        res.setContentType(mimeType);
        res.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
        res.setContentLength((int) file.length());

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        String hexHash = Storage.getFileHash(fileName);
        res.setHeader("Hash", hexHash);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);

    }

    /**
     * Search for the requested file in self storage
     *
     * @param res      Response
     * @param fileName Name of the file
     * @return ResponseEntity Filenames that inclue the search query
     */
    @RequestMapping("/search/{name:.+}")
    public ResponseEntity<String> searchFile(HttpServletResponse res, @PathVariable("name") String fileName) {
        List<String> matchingNames = Storage.searchForFile(fileName);
        String json = new Gson().toJson(matchingNames);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }
}
