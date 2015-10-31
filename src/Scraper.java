import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * class Scraper is responsible for scraping steam web page
 */
public class Scraper {
    //HashMap where key is the appid and the value is the game information
    public static HashMap<String, Game> steamStore = new HashMap<String, Game>();

    public static void main(String[] args) throws IOException {
        /*information to be put into database*/
        Elements appIdElems, gameNameElems, photoUrlElems, tagsElems, releaseDateElems, operatingSystemElems,
                ratingElems, originalPriceElems, salePriceElems;

        ArrayList<String> appIdList = new ArrayList<String>();
        ArrayList<String> photoUrlList = new ArrayList<String>();
        ArrayList<String> ratingsList = new ArrayList<String>();
        ArrayList<String> releaseDateList = new ArrayList<String>();
        ArrayList<String> priceList = new ArrayList<String>();

        String baseWebPage = "http://store.steampowered.com/search/?sort_by=&sort_order=0&page=";
        /*storing HTTP response from steam website*/
        Document doc = Jsoup.connect("http://store.steampowered.com/search/").get();

        /*loops through every page on steam store to gather data*/
        for (int i = 1; i <= 1; i++) //get rid of hardcoding later. change to real number
        {
            System.out.println("in loop");
            String link = baseWebPage + String.valueOf(i);
            doc = connectToWebpage(link);

            appIdList = getAppId(doc);
            appIdList.removeAll(Arrays.asList("", null));
            //System.out.println(appIdList.toString());
            photoUrlList = getPhotoUrl(doc, "");
            //System.out.println(photoUrlList.toString());
            ratingsList = getRatings(doc, "");
            //System.out.println(ratingsList.toString());
            releaseDateList = getReleaseDates(doc, "");
            //System.out.println(releaseDateList.toString());

            //System.out.println(appIdList.toString());
            setGameNames(doc, appIdList);
            //System.out.println(appIdList.toString());
            setPrice(doc, appIdList);

            /*creates Game objects for all game information on page*/
            //createGameObjectsonPage(gameNameElems, releaseDateElems, appIdList, photoUrlElems, ratingElems);
        }
        //System.out.println(steamStore.get("730").getName());
    }

    /**
     * Method ConnectToWebsite takes the current webpage and loads the page into a Document object
     *
     * @param url Link used for HTTP Get request on website
     * @return html page stored in a Document object
     * @throws IOException
     */
    private static Document connectToWebpage(String url) throws IOException {
        Document doc = Jsoup.connect(url).timeout(0).get();
        return doc;
    }

    /**
     * Method getAppId gets the unique identifier for the games on the Steam webpage
     *
     * @param webpage Webpage of current url that contains information about steam games
     * @return Elements object that holds all appids on the webpage
     */
    private static ArrayList<String> getAppId(Document webpage) {
        Elements appId = webpage.select("#search_result_container a");
        Game currGame;
        currGame = new Game(null, null, null, null, null, null, null);
        String appIdString;

        ArrayList<String> ids = new ArrayList<String>();
        for (Element a : appId) {
            appIdString = a.attr("data-ds-appid");
            //System.out.println(a.attr("data-ds-appid"));
            ids.add(appIdString);
            currGame.setAppID(appIdString);
            steamStore.put(appIdString, currGame);
        }
        //System.out.println(ids.toString());
        return ids;
    }

    /**
     * Method getGameTitle takes the current webpage and obtains the title of the steam game
     *
     * @param webpage Webpage of current url that contains information about steam games
     * @param appIds
     * @return Elements objects that hold all game titles on the webpage
     */
    private static void setGameNames(Document webpage, ArrayList<String> appIds)
    {
        //Game gameInfo = new Game();
        Elements titleElements = webpage.select("span.title");
        String game;
        int pointer = 0;
        String appid;
        Game currGame;

        System.out.println("in set games: " + appIds.toString());

        for(Element e: titleElements)
        {
            appid = appIds.get(pointer);
            game = e.text();
            currGame = new Game();
            currGame.setAppID(appid);
            currGame.setName(game);
            currGame.setDate(steamStore.get(appid).getDate());
            currGame.setGameUrl(steamStore.get(appid).getGameUrl());
            currGame.setOriginalPrice(steamStore.get(appid).getOriginalPrice());
            currGame.setDiscount(steamStore.get(appid).getDiscount());
            currGame.setRating(steamStore.get(appid).getRating());
            currGame.setSalePrice(steamStore.get(appid).getSalePrice());
            steamStore.remove(appid);
            steamStore.put(appid, currGame);
            pointer++;
        }

    }

    /**
     * Method getReleaseDates gets the release dates of the game on the current webpage
     *
     * @param webpage Webpage of current url that contains information about steam games
     * @param s
     * @return Elements object that holds all game release dates on the webpage
     */
    private static ArrayList<String> getReleaseDates(Document webpage, String s) {
        Elements dateElems = webpage.select("div.col.search_released.responsive_secondrow");
        String date;
        ArrayList<String> output = new ArrayList<String>();

        for(Element e: dateElems)
        {
            output.add(e.text());
            //System.out.println(e.text());
        }
        return output;
    }

    /**
     * Method getPhotoUrl gets the photoUrl of the game on the current webpage
     *
     * @param webpage Webpage of the current url that contains information about steam games
     * @param s
     * @return Elements object that holds all photoUrls of the game
     */
    private static ArrayList<String> getPhotoUrl(Document webpage, String s) {
        Elements photoUrls = webpage.select("div.col.search_capsule img");
        String urlString;
        ArrayList<String> urls = new ArrayList<String>();

        //System.out.println(photoUrls.attr("src"));
        for(Element e: photoUrls)
        {
            urlString = e.attr("src");
            urls.add(urlString);
            //System.out.println(urlString);
        }
        return urls;
    }

    /**
     * Method getRatings gets the rating of the game on the current webpage
     *
     * @param webpage Webpage of the current url that contains information about steam
     * @param s
     * @return Elements object that holds both positive and negative ratings of game
     */
    private static ArrayList<String> getRatings(Document webpage, String s) {
        Elements ratings = webpage.select("div.col.search_reviewscore.responsive_secondrow span");
        //System.out.println(ratings.attr("data-store-tooltip"));
        Boolean isInitial = true;
        String finalRating = null;
        ArrayList<String> review = new ArrayList<String>();
        for(Element e: ratings)
        {
            if(isInitial == true)
            {
                //System.out.println("in addas;lksda''d");
                isInitial = false;
                continue;
            }else
            {
                finalRating = e.attr("data-store-tooltip");
                finalRating = finalRating.replaceAll("<br>", " ");
                review.add(finalRating);
                //System.out.println(finalRating);
            }
        }

        //System.out.println(review.toArray().toString() + "array");
        return review;
    }

    /**
     * Method getOriginalPrice gets the price of the game on the current webpage without a sale
     *
     * @param webpage Webpage of the current url that contains information about steam
     * @param appids List that stores the appids on the current page
     * @return Elements object that holds both the sale price and original price
     */
    private static void setPrice(Document webpage, ArrayList<String> appids) {
        Elements price = webpage.select("div.col.search_price.responsive_secondrow");
        ArrayList<String> output = new ArrayList<String>();
        String currPrice;
        int pointer = 0;
        String currAppid;
        Game currGame;

        /*Goes through all prices on web pages*/
        for(Element e: price)
        {
            currPrice = e.text();
            currAppid = appids.get(pointer);

            if(currPrice.equalsIgnoreCase("Free To Play")) //checks if price is labeled as Free-To-Play
            {
                currGame = steamStore.get(currAppid);
                currGame.setSalePrice("0");
                currGame.setOriginalPrice("0");
                //ADD SALE PERCENT FIELD HERE. NEED TO ADD TO GAME CLASS
                steamStore.put(currAppid, currGame);
                //System.out.println(e.text());
            }else
            {
                String parts[] = currPrice.split(" ");
                if(parts.length == 1)//
                {
                    currGame = steamStore.get(currAppid);
                    currGame.setSalePrice(parts[0]);
                    currGame.setOriginalPrice(parts[0]);
                    steamStore.put(currAppid, currGame);

                }else if (parts.length == 2)
                {
                    currGame = steamStore.get(currAppid);
                    //System.out.println(currPrice);
                    currGame.setSalePrice(parts[1]);
                    currGame.setOriginalPrice(parts[0]);
                    steamStore.put(currAppid, currGame);
                }else
                {
                    System.out.println("Error: Cannot have more than the orginal or sale price");
                }
            }
            pointer++;
        }//end of for loop

    }


}
