package cgiserver.exec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ScriptExecutor {

	private final File EXEC_DIR;

	public ScriptExecutor(File execDir) {
		this.EXEC_DIR = execDir;
	}

	public void execute(File scriptToRun, String[] params, InputStream in, OutputStream out) {
		try {
			String name = scriptToRun.getName();

			Process process = createProcess(scriptToRun, params);

			redirectProcessPipes(process, in, out, name);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void redirectProcessPipes(Process process, InputStream in, OutputStream out, String name)
			throws IOException {
		InputStream tmpIn = process.getInputStream();
		OutputStream tmpOut = process.getOutputStream();

		do {
			handleRedirects(process, in, out, tmpIn, tmpOut);
		} while (process.isAlive());

		handleRedirects(process, in, out, tmpIn, tmpOut);
		
		out.close();
		in.close();
	}

	private void handleRedirects(Process process, InputStream in, OutputStream out, InputStream tmpIn,
			OutputStream tmpOut) throws IOException {
		byte[] buffer = new byte[128];
		int read;
		while (in.available() > 0) {
			read = in.read(buffer);
			tmpOut.write(buffer, 0, read);
		}
		while (tmpIn.available() > 0) {
			read = tmpIn.read(buffer);
			out.write(buffer, 0, read);
		}
		while (process.getErrorStream().available() > 0) {
			read = process.getErrorStream().read(buffer);
			System.err.write(buffer, 0, read);
		}
	}

	private Process createProcess(File scriptToRun, String[] params) throws IOException {
		Process process = Runtime.getRuntime().exec(scriptToRun.getAbsolutePath(), params, EXEC_DIR);
		return process;
	}

}