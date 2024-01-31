package tools.important.tankslua;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.Screen;

import java.awt.*;
import java.util.HashMap;

/**
 * A class used to send the player a notification.
 */
public class Notification {
    private int index;
    public double totalSeconds;
    public double secondsLeft;
    public String text;
    enum NotificationType {
        INFO,
        WARN
    }
    public NotificationType type;

    /**
     * Constructing a Notification will immediately cause that notification to pop up on the player's screen.
     * @param notificationType The type of notification. This changes the color of the notification as well as the sound played.
     * @param seconds The amount of seconds the notification will stay active.
     * @param text The text of the notification
     */
    public Notification(NotificationType notificationType, double seconds, String text) {
        this.type = notificationType;
        this.secondsLeft = seconds;
        this.totalSeconds = secondsLeft;
        this.text = text;

        TanksLua.tanksLua.activeNotifications.add(this);
        this.index = TanksLua.tanksLua.activeNotifications.size();

        if (Game.game.window == null) return;
        switch(this.type) {
            case INFO -> Drawing.drawing.playSound("join.ogg", 1.5f);
            case WARN -> Drawing.drawing.playSound("leave.ogg", 1.25f);
        }

    }
    private boolean markedForRemoval;
    private static final HashMap<NotificationType, Color> typeBackgroundColors = new HashMap<>();
    static {
        typeBackgroundColors.put(NotificationType.INFO, new Color(255,255,255));
        typeBackgroundColors.put(NotificationType.WARN, new Color(255, 215, 118));
    }
    private void markForRemoval() {
        markedForRemoval = true;
        TanksLua.tanksLua.notificationsMarkedForRemoval.add(this);
    }
    public void update() {
        if (markedForRemoval) return;
        index = TanksLua.tanksLua.activeNotifications.indexOf(this)+1;
        secondsLeft -= Math.min(0.01, Panel.frameFrequency/125);
        if (secondsLeft <= 0) markForRemoval();
    }
    public void draw() {
        if (markedForRemoval) return;
        double notifRemovalProgress = (secondsLeft/totalSeconds);

        Screen screen = Game.screen;

        double alpha = Math.min(notifRemovalProgress*255, 255);
        Color color = typeBackgroundColors.get(this.type);

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();


        Drawing.drawing.setColor(r,g,b,alpha);

        double width = screen.objWidth*1.3;
        double height = screen.objHeight*3;

        double x = screen.centerX*2.3-width;
        double y = screen.centerY*2.1-(height*(index*1.1));

        Drawing.drawing.fillInterfaceRect(x, y, width, height);
        Drawing.drawing.setColor(0,0,0, alpha);

        Drawing.drawing.setInterfaceFontSize(screen.titleSize/(this.text.length()/23d));
        Drawing.drawing.displayInterfaceText(x, y, this.text);

        Drawing.drawing.setColor(r*0.6,g*0.9,b*0.6, alpha*1.3);
        double progressBarHeight = height/20;

        Drawing.drawing.fillInterfaceProgressRect(x, y-(height/2)+(progressBarHeight/2), width, progressBarHeight, notifRemovalProgress);
    }
}
