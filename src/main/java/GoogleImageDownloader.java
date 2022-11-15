import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;

public class GoogleImageDownloader {

    static final String googleImages = "https://www.google.ru/imghp?hl=en&ogbl";

    /**
     * Максимальное возможное количество получаемых картинок.
     */
    static final Integer MAX_IMAGES_AMOUNT = 1000;

    /**
     * Получение списка картинок для скачивания.
     * @param searchingWord запрос.
     * @param imagesCount количество желаемых картинок.
     * @return
     */
    static public ArrayList<String> getImagesUrl(String searchingWord, Integer imagesCount) {
        WebDriverManager.chromedriver().setup();

        var driver = new ChromeDriver();

        driver.get(googleImages);

        driver.manage().window().maximize();

        var inputELement = driver.findElement(By.tagName("input"));

        inputELement.sendKeys(searchingWord);

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

                var gettingImgUrlThread = new Thread(()-> {
                    var src = new WebDriverWait(driver, Duration.ofSeconds(10))
                            .until(temp -> bigImg.findElement(By.cssSelector("img[jsname='HiaYvf']"))).getAttribute("src");

                    imagesLinks.add(src);
                });

                gettingImgUrlThread.start();

                gettingImgUrlThread.join();
            }
            catch (Exception ex) {
            }
        }

        return imagesLinks;
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
