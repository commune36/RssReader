package jp.commune.rssreader;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class RssController {

    @GetMapping("/viewer")
    public String view(@RequestParam(name = "url", required = false, defaultValue = "https://sports.yahoo.co.jp/sports/column/rss") String url, Model model) {
        String nextPage = "";

        try {
            // RSSサイトからxml文字列を取得します。
            String rss = new RestTemplate().getForObject(url, String.class);

            // xml文字列をDocument形式に変換します。
            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docbuilder = dbfactory.newDocumentBuilder();
            Document document = docbuilder.parse(new ByteArrayInputStream(rss.getBytes("UTF-8")));

            // xmlからChannel情報を取得します。
            Channel channel = this.getChannel(document);

            // チャンネル情報を取得できた場合、viewerサイトを表示します。
            if (channel != null) {
                model.addAttribute("channel", channel);
                nextPage = "viewer";
            } else {
                nextPage = "error";
            }

        } catch (Exception e) {
            nextPage = "error";
        }

        return nextPage;
    }

    private Channel getChannel(Document document) throws ParseException {
        Channel channel = null;

        Element root = document.getDocumentElement();
        NodeList rootChildren = root.getChildNodes();

        for (int i = 0; i < rootChildren.getLength(); i++) {
            Node node = rootChildren.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                if (element.getNodeName().equals("channel")) {
                    channel = new Channel();
                    List<Item> itemList = new ArrayList<>();

                    NodeList channelChildren = node.getChildNodes();

                    for (int j = 0; j < channelChildren.getLength(); j++) {
                        Node channelNode = channelChildren.item(j);

                        if (channelNode.getNodeType() == Node.ELEMENT_NODE) {
                            String channelNodeName = channelNode.getNodeName();

                            switch (channelNodeName) {
                                case "title":
                                    channel.setTitle(channelNode.getTextContent());
                                    break;

                                case "link":
                                    channel.setLink(channelNode.getTextContent());
                                    break;

                                case "description":
                                    channel.setDescription(channelNode.getTextContent());
                                    break;

                                case "lastBuildDate":
                                    channel.setLastBuildDate(channelNode.getTextContent());
                                    break;

                                case "language":
                                    channel.setLanguage(channelNode.getTextContent());
                                    break;

                                case "copyright":
                                    channel.setCopyright(channelNode.getTextContent());
                                    break;

                                case "item":
                                    Item item = new Item();

                                    NodeList itemChildren = channelNode.getChildNodes();

                                    for (int k = 0; k < itemChildren.getLength(); k++) {
                                        Node itemNode = itemChildren.item(k);

                                        if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                                            Element itemElement = (Element) itemNode;
                                            String itemNodeName = itemNode.getNodeName();

                                            switch (itemNodeName) {
                                                case "title":
                                                    item.setTitle(itemNode.getTextContent());
                                                    break;

                                                case "link":
                                                    item.setLink(itemNode.getTextContent());
                                                    break;

                                                case "pubDate":
                                                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
                                                    Date pubDate = sdf.parse(itemNode.getTextContent());
                                                    item.setPubDate(pubDate);

                                                    Date nowDate = new Date();

                                                    break;

                                                default:
                                                    break;

                                            }
                                        }
                                    }
                                    itemList.add(item);
                                    break;

                                default:
                                    break;

                            }
                        }
                    }
                    channel.setItemList(itemList);

                }
            }
        }

        return channel;
    }
}
