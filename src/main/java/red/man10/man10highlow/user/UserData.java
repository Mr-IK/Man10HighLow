package red.man10.man10highlow.user;

import java.util.UUID;

public class UserData {

    // ユーザー名
    private String playerName;

    // 固有ID
    private final UUID uuid;

    //最大獲得金額...一度の勝負で獲得した最大の金額
    private Long max_win;

    //合計獲得金額...今までの勝負で獲得した金額の総額
    private Long total_win;

    public UserData(UUID uuid,String name,Long max_win,Long total_win){
        this.uuid = uuid;
        this.playerName = name;
        this.max_win = max_win;
        this.total_win = total_win;
    }

    public String getName() {
        return playerName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Long getMaxWin() {
        return max_win;
    }

    public void setMaxWin(Long max_win) {
        this.max_win = max_win;
    }

    public Long getTotalWin() {
        return total_win;
    }

    public void setTotalWin(Long total_win) {
        this.total_win = total_win;
    }
}
