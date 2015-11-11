package barqsoft.footballscores.data;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Veronika Rodionova nika.blaine@gmail.com
 */
public class FootballScore {

    private String home;
    private String away;
    private int homeGoals;
    private int awayGoals;
    private String date;
    private String time;

    public FootballScore(String home, String away, int homeGoals, int awayGoals, String date, String time) {
        this.home = home;
        this.away = away;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.date = date;
        this.time = time;
    }

    public String getHome() {
        return home;
    }

    public String getAway() {
        return away;
    }

    public int getHomeGoals() {
        return homeGoals;
    }

    public int getAwayGoals() {
        return awayGoals;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
                .append(home)
                .append(away)
                .append(homeGoals)
                .append(awayGoals)
                .append(date)
                .append(time)
                .build();
    }
}
