using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using AngleSharp;
using AngleSharp.Parser.Html;

namespace AngleSharpParser
{
    class AngleSharpParser
    {
        static void Main(string[] args)
        {
            if (args.Length != 2)
            {
                Console.WriteLine("Two parameters are required " + args.Length);
                return;
            }

            AngleSharp.Dom.Html.IHtmlDocument document = null;
            String html = null;
            String url = null;

            switch (args[0])
            {
                case "-f":
                    html = readFile(args[1]);
                    if (html == null)
                        return;
                    break;
                case "-s":
                    html = args[1];
                    break;
                case "-u":

                    url = args[1];

                    break;
                default:
                    Console.WriteLine("Invalid option");
                    return;
            }
            try
            {
                if (url == null && html != null)
                    document = new HtmlParser(html).Parse();
                else if (url != null && html == null)
                    document = new HtmlParser(url).Parse();
                else
                {
                    Console.WriteLine("There was an error while parsing");
                    return;
                }
            }
            catch (Exception e)
            {
                if (e.Source != null)
                    Console.WriteLine("IOException source: {0}", e.Source);
                throw;

            }
            String output = Html5libSerializer.dom2string(document);
            //Console.WriteLine(document.DocumentElement.OuterHtml);
            Console.OutputEncoding = Encoding.UTF8;
            Console.WriteLine(output);
            //Console.ReadKey();
        }

        private static string readFile(String path)
        {
            String text = null;
            try
            {
                text = System.IO.File.ReadAllText(path);
            }
            catch (Exception e)
            {
                if (e.Source != null)
                    Console.WriteLine("IOException source: {0}", e.Source);
                throw;
            }
            return text;
        }
    }
}
