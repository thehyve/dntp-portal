package business.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import business.exceptions.FileDeleteError;
import business.exceptions.FileDownloadError;
import business.exceptions.FileNotFound;
import business.exceptions.FileUploadError;
import business.models.File;
import business.models.FileRepository;
import business.models.User;

@Service
public class FileService {

    Log log = LogFactory.getLog(getClass());
    
    FileSystem fileSystem = FileSystems.getDefault();
    
    @Autowired
    FileRepository fileRepository;

    @Value("${dntp.upload-path}")
    String uploadPath;
    
    @PostConstruct
    public void init() throws IOException {
        Path path = fileSystem.getPath(uploadPath).normalize();
        if (!path.toFile().exists()) {
            Files.createDirectory(path);
        }
        log.info("File upload path: " + path.toAbsolutePath());
    }
    
    public static String getBasename(String name) {
        String[] tokens = name.split("\\.(?=[^\\.]+$)");
        if (tokens.length > 0) {
            return tokens[0];
        } else {
            return "";
        }
    }
    
    public static String getExtension(String name) {
        String[] tokens = name.split("\\.(?=[^\\.]+$)");
        if (tokens.length > 1) {
            return "."+tokens[1];
        } else {
            return "";
        }
    }
    
    Map<String, SortedMap<Integer, Path>> uploadChunks = new HashMap<String, SortedMap<Integer,Path>>();
    
    public File uploadPart(User user, String name, File.AttachmentType type, MultipartFile file,
            Integer chunk, Integer chunks, String flowIdentifier) {
        try {
            String identifier = user.getId().toString() + "_" +flowIdentifier;
            
            String contentType = file.getContentType();
            InputStream input = file.getInputStream();

            // Create temporary file for chunk
            Path path = fileSystem.getPath(uploadPath).normalize();
            if (!path.toFile().exists()) {
                Files.createDirectory(path);
            }
            
            String prefix = getBasename(name);
            String suffix = getExtension(name);
            Path f = Files.createTempFile(path, prefix, suffix + "." + chunk + ".chunk").normalize();
            // filter path names that point to places outside the upload path.
            // E.g., to prevent that in cases where clients use '../' in the filename
            // arbitrary locations are reachable.
            if (!Files.isSameFile(path, f.getParent())) {
                // Path f is not in the upload path. Maybe 'name' contains '..'?
                throw new FileUploadError("Invalid file name");
            }
            log.info("Copying file to " + f.toString());
            
            // Copy chunk to temporary file
            Files.copy(input, f, StandardCopyOption.REPLACE_EXISTING);
            
            // Save chunk location in chunk map
            SortedMap<Integer, Path> chunkMap;
            synchronized(uploadChunks) {
                // FIXME: perhaps use a better identifier? Not sure if this one 
                // is unique enough...
                chunkMap = uploadChunks.get(identifier);
                if (chunkMap == null) {
                    chunkMap = new TreeMap<Integer, Path>();
                    uploadChunks.put(identifier, chunkMap);
                }
            }
            chunkMap.put(chunk, f);
            log.info("Chunk " + chunk + " saved to " + f.toString());

            // Assemble complete file if all chunks have been received
            if (chunkMap.size() == chunks.intValue()) {
                Path assembly = Files.createTempFile(path, prefix, suffix).normalize();
                // filter path names that point to places outside the upload path.
                // E.g., to prevent that in cases where clients use '../' in the filename
                // arbitrary locations are reachable.
                if (!Files.isSameFile(path, assembly.getParent())) {
                    // Path assembly is not in the upload path. Maybe 'name' contains '..'?
                    throw new FileUploadError("Invalid file name");
                }
                log.info("Assembling file " + assembly.toString() + " from " + chunks + " chunks...");
                OutputStream out = Files.newOutputStream(assembly, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                
                // Copy chunks to assembly file, delete chunk files
                for (int i = 1; i <= chunks; i++) {
                    //log.info("Copying chunk " + i + "...");
                    Path source = chunkMap.get(new Integer(i));
                    if (source == null) {
                        log.error("Cannot find chunk " + i);
                        throw new FileUploadError("Cannot find chunk " + i);
                    }
                    Files.copy(source, out);
                    Files.delete(source);
                }

                // Save assembled file name to database
                log.info("Saving attachment to database...");
                File attachment = new File();
                attachment.setName(name);
                attachment.setType(type);
                attachment.setMimeType(contentType);
                attachment.setDate(new Date());
                attachment.setUploader(user);
                attachment.setFilename(assembly.getFileName().toString());
                attachment = fileRepository.save(attachment);
                return attachment;
            }
            return null;
        } catch(IOException e) {
            log.error(e);
            throw new FileUploadError(e.getMessage());
        }
    }
    
    public InputStream getInputStream(File attachment) {
        if (attachment == null) {
            throw new FileNotFound();
        }
        try {
            FileSystem fileSystem = FileSystems.getDefault();
            Path path = fileSystem.getPath(uploadPath, attachment.getFilename());
            InputStream input = new FileInputStream(path.toFile());
            return input;
        } catch(IOException e) {
            log.error(e);
            throw new FileDownloadError();
        }
    }
    
    public HttpEntity<InputStreamResource> download(Long id) {
        try {
            File attachment = fileRepository.findOne(id);
            if (attachment == null) {
                throw new FileNotFound();
            }
            FileSystem fileSystem = FileSystems.getDefault();
            Path path = fileSystem.getPath(uploadPath, attachment.getFilename());
            InputStream input = new FileInputStream(path.toFile());
            InputStreamResource resource = new InputStreamResource(input);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf(attachment.getMimeType()));
            headers.set("Content-Disposition",
                    "attachment; filename=" + attachment.getName().replace(" ", "_"));
            HttpEntity<InputStreamResource> response =  new HttpEntity<InputStreamResource>(resource, headers);
            return response;
        } catch(IOException e) {
            log.error(e);
            throw new FileDownloadError();
        }
    }

    public void removeAttachment(File attachment) {
        fileRepository.delete(attachment);
        Path path = fileSystem.getPath(uploadPath, attachment.getFilename());
        try {
            Files.delete(path);
        } catch(IOException e) {
            log.error(e);
            throw new FileDeleteError();
        }
    }

}
