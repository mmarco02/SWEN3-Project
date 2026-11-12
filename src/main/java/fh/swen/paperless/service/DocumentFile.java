package fh.swen.paperless.service;

import fh.swen.paperless.domain.Document;

import java.io.InputStream;

/**
 * A document together with the content of its stored file.
 */
public record DocumentFile(Document document, InputStream content) {
}
