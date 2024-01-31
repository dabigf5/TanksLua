package tools.important.tankslua;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.Screen;

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
    public Notification(NotificationType notificationType, double seconds, String text) {
        this.type = notificationType;
        this.secondsLeft = seconds;
        this.totalSeconds = secondsLeft;
        this.text = text;

        TanksLua.tanksLua.activeNotifications.add(this);
        this.index = TanksLua.tanksLua.activeNotifications.size();
    }
    private boolean markedForRemoval;

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
        Drawing.drawing.setColor(255,255,255, alpha);

        double width = screen.objWidth*1.3;
        double height = screen.objHeight*3;

        double x = screen.centerX*2.3-width;
        double y = screen.centerY*2.1-(height*(index*1.1));

        Drawing.drawing.fillInterfaceRect(x, y, width, height);
        Drawing.drawing.setColor(0,0,0, alpha);

        Drawing.drawing.setInterfaceFontSize(screen.titleSize/(this.text.length()/23d));
        Drawing.drawing.displayInterfaceText(x, y, this.text);

        Drawing.drawing.setColor(100*notifRemovalProgress,255*notifRemovalProgress,0, alpha*1.3);
        double progressBarHeight = height/20;

        Drawing.drawing.fillInterfaceProgressRect(x, y-(height/2)+(progressBarHeight/2), width, progressBarHeight, notifRemovalProgress);
    }
}
