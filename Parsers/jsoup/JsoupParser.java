import java.io.File;
import java.io.PrintStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JsoupParser {

	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println("Two parameters are required");
			return;
		}
		
		String inputType = args[0];
		String inputValue = args[1];
		
		try {
			Document doc = null;
			switch (inputType) {
			case "-f":
				doc = Jsoup.parse(new File(inputValue), "utf-8");
				break;
			case "-s":
				doc = Jsoup.parse(inputValue);
				break;
			default:
				System.out.println("Invalid option");
				return;
			}
			String output = Html5libSerializer.dom2string(doc);
			PrintStream out = new PrintStream(System.out, true, "UTF-8");
			out.println(output);
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}