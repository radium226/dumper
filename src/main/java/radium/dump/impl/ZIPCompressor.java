package radium.dump.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import radium.dump.Compressor;

public class ZIPCompressor implements Compressor {

	final public static int BUFFER_SIZE = 1024;
	
	private String name;
	
	public ZIPCompressor(String name) {
		super();
		
		this.name = name;
	}

	@Override
	public void compress(InputStream inputStream, OutputStream outputStream) throws IOException {
		ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
		zipOutputStream.putNextEntry(new ZipEntry(this.name));
		byte[] buffer  = new byte[BUFFER_SIZE];
		while (true) {
			int readByteCount = inputStream.read(buffer);
			if (readByteCount <= 0) break;
			zipOutputStream.write(buffer, 0, readByteCount);
			zipOutputStream.flush();
		}
		zipOutputStream.closeEntry();
		zipOutputStream.flush();
		zipOutputStream.close();
	}
	
}
