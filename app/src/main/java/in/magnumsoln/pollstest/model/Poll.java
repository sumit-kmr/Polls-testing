package in.magnumsoln.pollstest.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Poll implements Serializable {
    private String POLL_ID,QUESTION, TOPIC,STATUS, IMAGE_URL, SHARE_URL;
    private List<String> OPTIONS;
    private long CORRECT_OPTION, REWARD_AMOUNT;
    private Date START_TIME,CLOSE_TIME,DECLARE_TIME;

    public Poll(){}

    public Poll(String POLL_ID,String QUESTION, String TOPIC, Date START_TIME, Date CLOSE_TIME,Date DECLARE_TIME, String STATUS, String IMAGE_URL, String SHARE_URL, List<String> OPTIONS, long CORRECT_OPTION, long REWARD_AMOUNT) {
        this.POLL_ID = POLL_ID;
        this.QUESTION = QUESTION;
        this.TOPIC = TOPIC;
        this.START_TIME = START_TIME;
        this.CLOSE_TIME = CLOSE_TIME;
        this.DECLARE_TIME = DECLARE_TIME;
        this.STATUS = STATUS;
        this.IMAGE_URL = IMAGE_URL;
        this.SHARE_URL = SHARE_URL;
        this.OPTIONS = OPTIONS;
        this.CORRECT_OPTION = CORRECT_OPTION;
        this.REWARD_AMOUNT = REWARD_AMOUNT;
    }

    public String getPOLL_ID() {
        return POLL_ID;
    }

    public Date getSTART_TIME() {
        return START_TIME;
    }

    public Date getCLOSE_TIME() {
        return CLOSE_TIME;
    }

    public Date getDECLARE_TIME() {
        return DECLARE_TIME;
    }

    public String getQUESTION() {
        return QUESTION;
    }

    public String getTOPIC() {
        return TOPIC;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public String getIMAGE_URL() {
        return IMAGE_URL;
    }

    public String getSHARE_URL() {
        return SHARE_URL;
    }

    public List<String> getOPTIONS() {
        return OPTIONS;
    }

    public long getCORRECT_OPTION() {
        return CORRECT_OPTION;
    }

    public long getREWARD_AMOUNT() {
        return REWARD_AMOUNT;
    }

}
