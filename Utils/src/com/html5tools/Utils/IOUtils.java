package com.html5tools.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class IOUtils {
	public static boolean createDirectory(String path) {
		if(path == null)
			return false;
		File files = new File(path);
		if (!files.exists()) {
			if (!files.mkdirs()) {
				System.out.println("Failed to create multiple directories!");
				return false;
			}
		}
		return true;
	}

	public static boolean directoryExists(String path) {
		if(path == null)
			return false;
		File folder = new File(path);
		return folder.exists();
	}

	public static List<String> listFoldersInFolder(String path, boolean recursive) {
		List<String> filesInFolder = new ArrayList<String>();
		if(path == null)
			return filesInFolder;
		
		File folder = new File(path);
		for (final File fileEntry : folder.listFiles()) {
			String fileName = fileEntry.getName();
			if (fileEntry.isDirectory()) {
				filesInFolder.add(fileName);
				if(recursive)
					filesInFolder.addAll(listFoldersInFolder(fileName, recursive));
			}
		}
		return filesInFolder;
	}
	
	public static List<String> listFilesInFolder(String path, boolean recursive) {
		List<String> filesInFolder = new ArrayList<String>();
		if(path == null)
			return filesInFolder;
		
		File folder = new File(path);
		for (final File fileEntry : folder.listFiles()) {
			String fileName = fileEntry.getName();
			if (fileEntry.isDirectory() && recursive) {
				filesInFolder.addAll(listFilesInFolder(fileName, recursive));
			} else {
				filesInFolder.add(fileName);
			}
		}
		return filesInFolder;
	}

	public static void saveFile(String fileName, String data) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			writer.print(data);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	public static void deleteFile(String fileName) throws IOException{
		Files.delete(new File(fileName).toPath());
	}
	
	public static String readFile(String path) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(path));

		String line;
		if ((line = br.readLine()) != null)
			sb.append(line);
		while ((line = br.readLine()) != null) {
			sb.append(System.getProperty("line.separator"));
			sb.append(line);
		}
		br.close();

		return sb.toString();
	}
}