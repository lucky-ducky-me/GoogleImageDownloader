import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.Console;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class GoogleImageDownloader {
    String destinationDir;

    int imageId = 0;

    final String googleImages = "https://www.google.ru/imghp?hl=en&ogbl";

    /**
     * Максимальное возможное количество получаемых картинок.
     */
    final Integer MAX_IMAGES_AMOUNT = 1000;

    /**
     * Получение списка картинок для скачивания.
     * @param searchingWord запрос.
     * @param imagesCount количество желаемых картинок.
     * @return
     */
    public ArrayList<String> getImagesUrl(String searchingWord, Integer imagesCount) {
        WebDriverManager.chromedriver().setup();

        var driver = new ChromeDriver();

        driver.get(googleImages);

        driver.manage().window().maximize();

        var inputELement = driver.findElement(By.tagName("input"));

        inputELement.sendKeys(searchingWord);

        //inputELement.sendKeys(Keys.ENTER);

        var searchButton = driver.findElement(By.className("Tg7LZd"));

        searchButton.click();

        var scrollingThread = new Thread(() -> {
            scrollToEnd(driver);
        });

        scrollingThread.start();

        try {
            scrollingThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        var imagesLinks = new ArrayList<String>();

        for (var i = 1; (i <= imagesCount || imagesLinks.size() < imagesCount) && i < MAX_IMAGES_AMOUNT; i++) {
            try {
                int finalI = i;
                var img =  new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(temp -> driver.findElement(By.xpath("//*[@id=\"islrg\"]/div[1]/div[" +
                                finalI +
                        "]/a[1]/div[1]/img")));

                img.click();

                var bigImg = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(temp -> driver.findElement(By.id("Sva75c")));

                WebElement srcElem = null;

                srcElem = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(temp -> bigImg.findElement(By.cssSelector("img[class='n3VNCb KAlRDb']")));

                imagesLinks.add(srcElem.getAttribute("src"));
            }
            catch (Exception ex) {
            }
        }

        return imagesLinks;
    }

    public void saveImagesInFile(String searchingWord, Integer imagesCount, String sourceFile) {
        AtomicReference<ArrayList<String>> links = new AtomicReference<>();

        var gettingUrlsThread = new Thread(() -> {
            links.set(getImagesUrl(searchingWord, imagesCount));
        }, "gettingUrlsThread");

        gettingUrlsThread.start();

        try {
            gettingUrlsThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        var urls = links.get();
        var imageLoader = new ImageLoader(sourceFile);

        for (var url: urls) {
            var savingInFileThread = new Thread(() -> {

                try {
                    imageLoader.saveImage(url);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }

            }, "savingInFileThread");

            savingInFileThread.start();

            try {
                savingInFileThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Скрол страницы до конца.
     * @param driver веб-драйвер.
     */
    private static void scrollToEnd(WebDriver driver) {
        var lastHeight = ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");

        while (true) {

            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            var newHeight = ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");

            try {
                driver.findElement(By.cssSelector(".YstHxe input")).click();
            }
            catch (Exception ex) {

            }

            if (Objects.equals(newHeight, lastHeight)) {
                break;
            }

            lastHeight = newHeight;
        }
    }
}
