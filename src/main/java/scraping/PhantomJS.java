package scraping;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class PhantomJS {

    public static void main(String[] args) throws IOException, InterruptedException {
        String username = "thailt";
        String password = "thailtpassword";
        // saveLoggedInCookies(username, password);

        WebDriver driver = loginByCookie(username);
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        for (int i = 0; i < 21; i++) {
            jse.executeScript("window.scrollBy(0,11360)", "");
            Thread.sleep(4000);
            System.out.println("Scrolling at " + i);
            if (i % 10 == 0) {
                File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                FileUtils.writeStringToFile(new File("C:\\tmp\\facebook\\pagesource_" + username + "_" + i + ".html"),
                        driver.getPageSource(), "utf-8");
                FileUtils.copyFile(scrFile,
                        new File("C:\\tmp\\facebook\\saveLoggedInCookies_" + username + "_" + i + ".png"));
            }
        }

        driver.quit();

    }

    private static WebDriver loginByCookie(String fileName) throws IOException {
        WebDriver driver = initializaPhantomJSBrowser();

        String baseUrl = "facebook.com";

        String protocol = "https://";
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        driver.get(protocol + baseUrl);

        driver.manage().window().setSize(new Dimension(640, 1136));
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        for (Cookie coki : loadCookies(fileName)) {
            if (coki.getDomain().contains(baseUrl)) {
                driver.manage().addCookie(coki);
            }
        }

        driver.get(protocol + baseUrl);

        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File("C:\\tmp\\facebook_login_by_cookie_" + fileName + ".png"));

        return driver;
    }

    private static WebDriver initializaPhantomJSBrowser() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability("takesScreenshot", true);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                "E:\\software\\browser\\phantomjs\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");

        WebDriver driver = new PhantomJSDriver(caps);
        return driver;
    }

    private static void saveCookies(String facebookUserName, Set<Cookie> cookies) throws IOException {

        Type hashSetOfCookies = new TypeToken<HashSet<Cookie>>() {
        }.getType();
        String cookiesJson = new Gson().toJson(cookies, hashSetOfCookies);

        String path = "C:\\tmp\\";
        String jsonExtension = ".json";
        File file = new File(path + facebookUserName + jsonExtension);
        FileUtils.writeStringToFile(file, cookiesJson, "utf-8");
    }

    private static Set<Cookie> loadCookies(String facebookUserName) throws IOException {
        String homepath = "C:\\tmp\\";
        String jsonExtension = ".json";
        File file = new File(homepath + facebookUserName + jsonExtension);
        String cookiesJson = FileUtils.readFileToString(file, "utf-8");

        Type hashSetOfCookies = new TypeToken<HashSet<Cookie>>() {
        }.getType();

        Set<Cookie> cookies = new Gson().fromJson(cookiesJson, hashSetOfCookies);
        return cookies;
    }

    public static void saveLoggedInCookies(String username, String password) throws IOException {
        WebDriver driver = initializaPhantomJSBrowser();
        driver.manage().window().setSize(new Dimension(640, 1136));

        String baseUrl = "facebook.com";
        String protocol = "https://";

        driver.get(protocol + baseUrl + "/login");

        driver.findElement(By.id("email")).sendKeys(username);
        driver.findElement(By.id("pass")).sendKeys(password);
        driver.findElement(By.id("loginbutton")).click();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        Gson gson = new Gson();
        Type listType = new TypeToken<HashSet<Cookie>>() {
        }.getType();

        Set<Cookie> cookies = gson.fromJson(gson.toJson(driver.manage().getCookies(), listType), listType);
        saveCookies(username, cookies);
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File("C:\\tmp\\saveLoggedInCookies_" + username + ".png"));

        driver.quit();
    }
}
