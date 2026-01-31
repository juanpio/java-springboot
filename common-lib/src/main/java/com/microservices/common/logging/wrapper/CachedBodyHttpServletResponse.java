package com.microservices.common.logging.wrapper;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Wrapper to cache response body so it can be logged.
 */
public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {
    
    private final ByteArrayOutputStream cachedBody = new ByteArrayOutputStream();
    private ServletOutputStream outputStream;
    private PrintWriter writer;
    
    public CachedBodyHttpServletResponse(HttpServletResponse response) {
        super(response);
    }
    
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new CachedBodyServletOutputStream(
                getResponse().getOutputStream(), 
                cachedBody
            );
        }
        return outputStream;
    }
    
    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(
                new OutputStreamWriter(getOutputStream(), getCharacterEncoding())
            );
        }
        return writer;
    }
    
    public byte[] getCachedBody() {
        return cachedBody.toByteArray();
    }
    
    private static class CachedBodyServletOutputStream extends ServletOutputStream {
        
        private final ServletOutputStream outputStream;
        private final ByteArrayOutputStream cachedBody;
        
        public CachedBodyServletOutputStream(ServletOutputStream outputStream, 
                                            ByteArrayOutputStream cachedBody) {
            this.outputStream = outputStream;
            this.cachedBody = cachedBody;
        }
        
        @Override
        public boolean isReady() {
            return outputStream.isReady();
        }
        
        @Override
        public void setWriteListener(WriteListener listener) {
            outputStream.setWriteListener(listener);
        }
        
        @Override
        public void write(int b) throws IOException {
            cachedBody.write(b);
            outputStream.write(b);
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            cachedBody.write(b, off, len);
            outputStream.write(b, off, len);
        }
    }
}
