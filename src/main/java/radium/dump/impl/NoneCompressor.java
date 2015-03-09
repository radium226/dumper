package radium.dump.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import radium.dump.Compressor;

public class NoneCompressor implements Compressor {
	
	final public static int BUFFER_SIZE = 1024;
	
	public NoneCompressor(String name) {
		super();
	}
	
	@Override
	public void compress(InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] buffer  = new byte[BUFFER_SIZE];
		while (true) {
			int readByteCount = inputStream.read(buffer);
			if (readByteCount <= 0) break;
			outputStream.write(buffer, 0, readByteCount);
			outputStream.flush();
		}
	}

}
