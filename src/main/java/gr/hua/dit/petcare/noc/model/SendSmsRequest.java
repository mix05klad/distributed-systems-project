package gr.hua.dit.petcare.noc.model;

public class SendSmsRequest {

    private String e164;
    private String content;

    public String getE164() {
        return e164;
    }

    public void setE164(String e164) {
        this.e164 = e164;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
