package uiParts.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class HomeServiceQueues extends BasePage<HomeServiceQueues> {
    private final SelenideElement queueHeader = $("div[data-testid='patient-queue-header']");

    @Override
    public String url() {
        return "/home/service-queues";
    }
}