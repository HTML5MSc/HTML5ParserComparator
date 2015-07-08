using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using AngleSharp.Dom;

namespace AngleSharpParser
{
    class Html5libSerializer
    {
        private readonly static String HTML = "http://www.w3.org/1999/xhtml";
        private readonly static String MATHML = "http://www.w3.org/1998/Math/MathML";
        private readonly static String SVG = "http://www.w3.org/2000/svg";
        private readonly static String XLink = "http://www.w3.org/1999/xlink";
        private readonly static String XML = "http://www.w3.org/XML/1998/namespace";
        private readonly static String XMLNS = "http://www.w3.org/2000/xmlns/";

        public static String dom2string(INode node)
        {
            String str = "";
            int ancestors = 0;
            if (!node.HasChildNodes)
                return "| ";
            INode parent = node;
            INode current = node.FirstChild;
            INode next = null;

            for (; ; )
            {
                str += "\n| " + indent(ancestors + templateAncestors(current, 0));

                switch (current.NodeType)
                {
                    case NodeType.Comment:
                        IComment c = (IComment)current;
                        str += "<!-- " + c.TextContent + " -->";
                        break;
                    case NodeType.DocumentType:
                        IDocumentType docType = (IDocumentType)current;
                        String dname = docType.Name;
                        String publicid = docType.PublicIdentifier;
                        String systemid = docType.SystemIdentifier;

                        if (!String.IsNullOrEmpty(publicid) && String.IsNullOrEmpty(systemid))
                        {
                            publicid = " \"" + publicid + "\"";
                            systemid = " \"\"";
                        }
                        else if (String.IsNullOrEmpty(publicid) && !String.IsNullOrEmpty(systemid))
                        {
                            publicid = " \"\"";
                            systemid = " \"" + systemid + "\"";
                        }
                        else if (!String.IsNullOrEmpty(publicid) && !String.IsNullOrEmpty(systemid))
                        {
                            publicid = " \"" + publicid + "\"";
                            systemid = " \"" + systemid + "\"";
                        }

                        str += "<!DOCTYPE " + dname + publicid + systemid + '>';
                        break;
                    case NodeType.Entity:
                        str += "<![CDATA[ " + current.NodeName + " ]]>";
                        break;
                    case NodeType.Text:
                        IText t = (IText)current;
                        str += '"' + t.Text + '"';
                        break;
                    case NodeType.Element:
                        IElement element = (IElement)current;
                        str += "<";

                        if (element.NamespaceUri != null)
                        {
                            if (element.NamespaceUri.Equals(SVG))
                                str += "svg ";
                            else if (element.NamespaceUri.Equals(MATHML))
                                str += "math ";
                        }

                        str += current.NodeName.ToLower();
                        str += '>';
                        if (element.Attributes.Count() > 0)
                        {
                            foreach (var att in element.Attributes.OrderBy(x => x.Name).ToList())
                            {
                                String key;
                                String value = att.Value;

                                if (att.NamespaceUri == null)
                                    key = att.Name;
                                else if (att.NamespaceUri.Equals(XML))
                                    key = "xml " + att.LocalName;
                                else if (att.NamespaceUri.Equals(XMLNS))
                                    key = "xmlns " + att.LocalName;
                                else if (att.NamespaceUri.Equals(XLink))
                                    key = "xlink " + att.LocalName;
                                else
                                    key = att.Name;


                                str += "\n| " + indent(1 + ancestors + templateAncestors(element, 0)) + key;
                                str += "=\"" + value + "\"";
                            }
                        }
                        /*
                         * Template elements have 'content' child 
                         * This is the required format of html5lib
                         */
                        if (element.NodeName.Equals("template") && element.NamespaceUri != null && element.NamespaceUri.Equals(HTML))
                        {
                            str += "\n| " + indent(1 + ancestors + templateAncestors(current, 0)) + "content";
                        }
                        break;


                    default:
                        break;
                }

                next = current.FirstChild;
                if (null != next)
                {
                    parent = current;
                    current = next;
                    ancestors++;
                    continue;
                }

                for (; ; )
                {
                    next = current.NextSibling;
                    if (next != null)
                    {
                        current = next;
                        break;
                    }
                    current = current.Parent;
                    parent = parent.Parent;
                    ancestors--;
                    if (current == node)
                    {
                        return str.Substring(1);
                    }
                }
            }
        }

        private static String indent(int ancestors)
        {
            String str = "";
            if (ancestors > 0)
            {
                while (0 <= --ancestors)
                    str += "  ";
            }
            return str;
        }

        private static int templateAncestors(INode current, int ancestors)
        {
            if (current.Parent != null)
            {
                current = current.Parent;
                if (current.NodeName.Equals("template") && ((IElement)current).NamespaceUri != null
                    && ((IElement)current).NamespaceUri.Equals(HTML))
                    ancestors++;
                return templateAncestors(current, ancestors);
            }
            else
                return ancestors;

        }
    }
}
