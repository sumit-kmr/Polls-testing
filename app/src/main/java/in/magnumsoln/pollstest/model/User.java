package in.magnumsoln.pollstest.model;

public class User {
    private Poll POLL;
    private int COINS_REDEEMED, POLL_COINS, SHARE_COIN;
    private String DEVICE_ID, PAYTM_NUMBER, PHONE_NUMBER, REFERRED_BY;
    private boolean MONEY_REQUESTED;

    public User() {
    }

    public User(Poll POLL, int COINS_REDEEMED, int POLL_COINS, int SHARE_COIN, String DEVICE_ID, String PAYTM_NUMBER, String PHONE_NUMBER, String REFERRED_BY, boolean MONEY_REQUESTED) {
        this.POLL = POLL;
        this.COINS_REDEEMED = COINS_REDEEMED;
        this.POLL_COINS = POLL_COINS;
        this.SHARE_COIN = SHARE_COIN;
        this.DEVICE_ID = DEVICE_ID;
        this.PAYTM_NUMBER = PAYTM_NUMBER;
        this.PHONE_NUMBER = PHONE_NUMBER;
        this.REFERRED_BY = REFERRED_BY;
        this.MONEY_REQUESTED = MONEY_REQUESTED;
    }

    public Poll getPOLL() {
        return POLL;
    }

    public int getCOINS_REDEEMED() {
        return COINS_REDEEMED;
    }

    public int getPOLL_COINS() {
        return POLL_COINS;
    }

    public int getSHARE_COIN() {
        return SHARE_COIN;
    }

    public String getDEVICE_ID() {
        return DEVICE_ID;
    }

    public String getPAYTM_NUMBER() {
        return PAYTM_NUMBER;
    }

    public String getPHONE_NUMBER() {
        return PHONE_NUMBER;
    }

    public String getREFERRED_BY() {
        return REFERRED_BY;
    }

    public boolean isMONEY_REQUESTED() {
        return MONEY_REQUESTED;
    }
}
