package radium.dump;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import radium.dump.impl.NoneCompressor;
import radium.dump.impl.ZIPCompressor;

public interface Compressor {

	public enum Type {

		NONE(NoneCompressor.class), ZIP(ZIPCompressor.class);

		final public static Logger LOGGER = LoggerFactory.getLogger(Type.class);

		private Class<? extends Compressor> compressorClass;

		private Type(Class<? extends Compressor> compressorClass) {
			this.compressorClass = compressorClass;
		}

		public Class<? extends Compressor> getCompressorClass() {
			return compressorClass;
		}

		@SuppressWarnings("unchecked")
		public <T extends Compressor> T newCompressor(String name) {
			try {
				return (T) compressorClass.getConstructors()[0].newInstance(new Object[] {name});
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				LOGGER.error("Unable to instanciate compressor", e);
				return null;
			}
		}

	}
	
	void compress(InputStream inputStream, OutputStream outputStream) throws IOException;
	
}
