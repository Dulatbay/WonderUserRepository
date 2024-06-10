package kz.wonder.filemanager.client.api;

import kz.wonder.filemanager.client.configuration.ClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "${file-api.name:file-api}", url = "${file-api.url:https://file-manager-of5r5e4d7a-lm.a.run.app}", configuration = ClientConfiguration.class)
public interface FileManagerApi {
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/{directory}/upload/files",
            produces = "*/*",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<String> uploadFiles(@PathVariable("directory") String directory, @RequestPart("files") List<MultipartFile> files);
}
