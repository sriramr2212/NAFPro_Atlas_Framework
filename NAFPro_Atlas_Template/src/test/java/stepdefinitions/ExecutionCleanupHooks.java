package stepdefinitions;

import io.cucumber.java.After;
import novac.wrapper.GenericWrapper;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Extend with overlay/tab/session/temp-file cleanup as needed
public class ExecutionCleanupHooks {
    
    private static final Logger logger = LogManager.getLogger(ExecutionCleanupHooks.class);
    
    @After
    public void cleanupModalState() {
        try {
            var modals = GenericWrapper.getDriver().findElements(By.xpath("//div[contains(@class,'ant-modal-wrap')]"));
            if (!modals.isEmpty() && modals.get(0).isDisplayed()) {
                modals.get(0).sendKeys(Keys.ESCAPE);
                Thread.sleep(200);
                logger.debug("Modal cleaned up after scenario");
            }
        } catch (Exception e) {
            // No action needed
        }
    }
}
