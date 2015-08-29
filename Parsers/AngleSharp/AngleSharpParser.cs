using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using AngleSharp;
using AngleSharp.Parser.Html;
using AngleSharp.Dom.Html;
using System.IO;

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

            string inputType = args[0];
            string inputValue = args[1];

            try
            {
                IHtmlDocument document = null;
                HtmlParserOptions parserOptions = new HtmlParserOptions();
                parserOptions.IsScripting = true;

                switch (inputType)
                {
                    case "-s":
                        document = new HtmlParser(parserOptions).Parse(inputValue);
                        break;
                    case "-f":
                        using (FileStream fs = new FileStream(
                            inputValue, FileMode.Open, FileAccess.Read))
                        {
                            document = new HtmlParser(parserOptions).Parse(fs);
                        }
                        break;
                    default:
                        Console.WriteLine("Invalid option");
                        return;
                }

                String output = Html5libSerializer.dom2string(document);
                //Console.WriteLine(document.DocumentElement.OuterHtml);
                Console.OutputEncoding = Encoding.UTF8;
                Console.WriteLine(output);
                //Console.ReadKey();
            }
            catch (Exception e)
            {
                if (e.Source != null)
                    Console.WriteLine("IOException source: {0}", e.Source);
            }
        }
    }
}
