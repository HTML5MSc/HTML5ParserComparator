import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JsoupParser {

	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println("Two parameters are required");
			return;
		}

		Document doc = null;
		String html = null;
		URL url = null;

		switch (args[0]) {
		case "-f":
			html = readFile(args[1]);
			if(html == null)
				return;
			break;
		case "-s":
			html = args[1];
			break;
		case "-u":
			try {
				url = new URL(args[1]);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		default:
			System.out.println("Invalid option");
			return;
		}
		try {
			if (url == null && html != null)
				doc = Jsoup.parse(html);
			else if (url != null && html == null)
				doc = Jsoup.parse(url, 1000);
			else {
				System.out.println("There was an error while parsing");
				return;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String output = Html5libSerializer.dom2string(doc);
		try{
			PrintStream out = new PrintStream(System.out, true, "UTF-8");
			out.println(output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String readFile(String path) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path));
			String line;
			if((line = br.readLine()) != null)
				sb.append(line);
			while ((line = br.readLine()) != null) {
				sb.append(System.getProperty("line.separator"));
				sb.append(line);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}
}