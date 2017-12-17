package scraping;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
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

public class Phantomjs {

	public static void main(String[] args) throws IOException, InterruptedException {
		// saveLoggedInCookies("thailt", "thaipassword");
		String username = "thailt";

		WebDriver driver = loginByCookie(username);
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		for (int i = 0; i < 10; i++) {
			jse.executeScript("window.scrollBy(0,1136)", "");
			Thread.sleep(4000);
			System.out.println("Scrolling at " + i);
			// if (i % 10 == 0)
			{
				File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				FileUtils.writeStringToFile(new File("C:\\tmp\\facebook\\pagesource_" + username + "_" + i + ".html"),
						driver.getPageSource(), "utf-8");
				FileUtils.copyFile(scrFile,
						new File("C:\\tmp\\facebook\\saveLoggedInCookies_" + username + "_" + i + ".png"));
			}
		}

	}

	private static WebDriver loginByCookie(String fileName) throws IOException {
		WebDriver driver = initializaBrowser();

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

	private static WebDriver initializaBrowser() {
		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setJavascriptEnabled(true);
		caps.setCapability("takesScreenshot", true);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
				"D:\\user_crawl_client\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");

		WebDriver driver = new PhantomJSDriver(caps);
		return driver;
	}

	private static void saveCookies(String filename, Set<Cookie> cookies) throws IOException {
		String path = "C:\\tmp\\";
		File file = new File(path + filename);
		file.createNewFile();
		FileWriter fileWrite = new FileWriter(file);
		BufferedWriter Bwrite = new BufferedWriter(fileWrite);

		for (Cookie ck : cookies) {
			Long expiredLong = ck.getExpiry() == null ? null : ck.getExpiry().getTime();
			Bwrite.write((ck.getName() + ";" + ck.getValue() + ";" + ck.getDomain() + ";" + ck.getPath() + ";"
					+ expiredLong + ";" + ck.isSecure()));
			Bwrite.newLine();
		}
		Bwrite.close();
		fileWrite.close();
	}

	private static Set<Cookie> loadCookies(String filename) throws IOException {
		Set<Cookie> result = new HashSet<>();
		String homepath = "C:\\tmp\\";
		File file = new File(homepath + filename);
		FileReader fileReader = new FileReader(file);

		BufferedReader Buffreader = new BufferedReader(fileReader);
		String strline;
		while ((strline = Buffreader.readLine()) != null) {
			StringTokenizer token = new StringTokenizer(strline, ";");
			while (token.hasMoreTokens()) {
				String name = token.nextToken();
				String value = token.nextToken();
				String domain = token.nextToken();
				String path = token.nextToken();
				Date expiry = null;

				String val;
				if (!(val = token.nextToken()).equals("null")) {
					expiry = new Date(Long.valueOf(val));
				}
				Boolean isSecure = new Boolean(token.nextToken()).booleanValue();
				Cookie ck = new Cookie(name, value, domain, path, expiry, isSecure);
				result.add(ck);
			}
		}

		Buffreader.close();
		fileReader.close();
		return result;
	}

	public static void saveLoggedInCookies(String username, String password) throws IOException {
		WebDriver driver = initializaBrowser();
		driver.manage().window().setSize(new Dimension(640, 1136));

		String baseUrl = "facebook.com";
		String protocol = "https://";

		driver.get(protocol + baseUrl + "/login");

		driver.findElement(By.id("email")).sendKeys(username);
		driver.findElement(By.id("pass")).sendKeys(password);
		driver.findElement(By.id("loginbutton")).click();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

		saveCookies(username, driver.manage().getCookies());
		File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(scrFile, new File("C:\\tmp\\saveLoggedInCookies_" + username + ".png"));

	}
}
