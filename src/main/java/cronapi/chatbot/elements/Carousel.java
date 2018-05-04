package cronapi.chatbot.elements;

import java.util.List;

//https://developers.facebook.com/docs/messenger-platform/reference/template/generic
// https://stackoverflow.com/questions/41525950/send-location-in-facebook-messenger?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
public class Carousel {
    private String message;
    private String image;
    private List<QuickReply> quickReply;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<QuickReply> getQuickReply() {
        return quickReply;
    }

    public void setQuickReply(List<QuickReply> quickReply) {
        this.quickReply = quickReply;
    }
}
