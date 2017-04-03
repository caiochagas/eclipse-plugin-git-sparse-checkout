package resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {

	public static boolean isValidGitProject(File file) {
		return new File(file.getAbsolutePath() + "/.git").isDirectory();
	}

	public static File getFile(String path) throws IOException {
		File file = new File(path);
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}

	public static String getFileContent(File file) throws IOException {
		return new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
	}

	public static String getInputAsString(InputStream is) {
		try (java.util.Scanner s = new java.util.Scanner(is)) {
			return s.useDelimiter("\\A").hasNext() ? s.next() : "";
		}
	}

	public static void setConfigSparseCheckout(boolean value, File projectDir) {
		try {
			Runtime.getRuntime().exec("git config core.sparsecheckout true", null, projectDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
