package radium;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.SECONDS;

public class TryMultiThreadedIO {

	public static void main(String[] arguments) throws Throwable {
		final PipedOutputStream pipedOutputStream = new PipedOutputStream();
		final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
		
		
		Thread writerThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				//Writer writer = new OutputStreamWriter(pipedOutputStream);
				PrintStream printStream = new PrintStream(pipedOutputStream);
				for (long i = 0; i < 1000000000L; i++) {
					printStream.print(i);
					printStream.println(" - Hey! ");
					printStream.flush();
				}
				
				printStream.close();
			}
			
		});
		
		Thread readerThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					InputStreamReader inputStreamReader = new InputStreamReader(pipedInputStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
					long lineCount = 0;
					while (true) {
						String line = bufferedReader.readLine();
						if (line == null) {
							break;
						}
						lineCount++;
						if (lineCount % 100000L == 0L) {
							System.out.print(".");
						}
						if (lineCount % 10000000L == 0L) {
							System.out.println();
						}
					}
					bufferedReader.close();
					inputStreamReader.close();
					System.out.println();
					System.out.println("There was " + lineCount + " lines");
					
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
			}
			
		});
		
		writerThread.start();
		readerThread.start();
		
		writerThread.join();
		readerThread.join();
		
		pipedOutputStream.close();
		pipedInputStream.close();
		
	}
	
	public static void sleep(int duration, TimeUnit unit) {
		try {
			Thread.sleep(unit.toMillis(duration));
		} catch (InterruptedException e) {
			
		}
	}
	
}
