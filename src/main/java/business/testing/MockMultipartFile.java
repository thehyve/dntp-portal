package business.testing;

import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MockMultipartFile implements MultipartFile {

    private final URL resource;

    public MockMultipartFile(URL resource) {
        this.resource = resource;
    }

    @Override
    public String getName() {
        return resource.getFile();
    }

    @Override
    public String getOriginalFilename() {
        return resource.getFile();
    }

    @Override
    public String getContentType() {
        return MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBytes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return resource.openStream();
    }

    @Override
    public void transferTo(java.io.File dest) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }
}
