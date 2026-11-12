package fh.swen.paperless.storage;

import java.io.InputStream;

/**
 * Stores the uploaded document files. Implemented by {@link MinioFileStorage}.
 */
public interface FileStorage {

    void store(String key, InputStream content, long size, String contentType);

    InputStream load(String key);

    void delete(String key);
}
