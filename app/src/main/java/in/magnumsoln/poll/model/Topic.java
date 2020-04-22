package in.magnumsoln.poll.model;

public class Topic implements Comparable<Topic>{
    private String IMAGE_URL,SHARE_URL,TOPIC_NAME;

    public Topic(){}

    public Topic(String TOPIC_NAME,String IMAGE_URL, String SHARE_URL) {
        this.TOPIC_NAME = TOPIC_NAME;
        this.IMAGE_URL = IMAGE_URL;
        this.SHARE_URL = SHARE_URL;
    }

    public String getIMAGE_URL() {
        return IMAGE_URL;
    }

    public String getSHARE_URL() {
        return SHARE_URL;
    }

    public String getTOPIC_NAME() {
        return TOPIC_NAME;
    }

    public void setTOPIC_NAME(String TOPIC_NAME) {
        this.TOPIC_NAME = TOPIC_NAME;
    }

    @Override
    public int compareTo(Topic topic) {
        return this.TOPIC_NAME.compareTo(topic.getTOPIC_NAME());
    }
}
