package novac.utils;

import novac.wrapper.GenericWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class ElementScrollHelper {

    private static final Logger logger = LogManager.getLogger(ElementScrollHelper.class);

    public static void scrollIntoViewIfNeeded(String pageName, String elementName) {
        try {
            ConstantsResolver.ElementInfo info = ConstantsResolver.resolve(pageName, elementName);
            WebDriver driver = GenericWrapper.getDriver();

            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");

            ((JavascriptExecutor) driver).executeScript(
                "document.querySelectorAll('div[class*=\\'ant-layout-content\\'], div[class*=\\'ant-table-body\\'], main')"
                + ".forEach(function(el){ el.scrollTop = 0; });");

            WebElement element = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> d.findElement(By.xpath(info.getXpath())));

            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'center'});", element);

            new WebDriverWait(driver, Duration.ofMillis(500))
                .until(d -> ((JavascriptExecutor) d)
                    .executeScript("return document.readyState").equals("complete"));

            logger.debug("Scrolled element '{}' into view on '{}'", elementName, pageName);
        } catch (Exception e) {
            logger.debug("Pre-scroll skipped for '{}' on '{}': {}", elementName, pageName, e.getMessage());
        }
    }
}
