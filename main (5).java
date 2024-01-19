import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

class Line {
    public double a, b, c;

    public Line(double x1, double y1, double x2, double y2) {
        a = y1 - y2;
        b = x2 - x1;
        c = x1 * y2 - x2 * y1;
    }

    public Line(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public Line(double k, double p) {
        a = -k;
        b = 1;
        c = -p;
    }

    public double distanceToZero() {
        return distanceToPoint(0, 0);
    }

    public double distanceToPoint(double x, double y) {
        return Math.abs(a * x + b * y + c) / Math.sqrt(a * a + b * b);
    }

    public double getSideSign(Point p) {
        return c + a * p.x + b * p.y;
    }
}

public class Main extends JFrame implements MouseListener, MouseMotionListener {
    private double MIN_LINE_LEN = 10;
    private Point [] points = new Point[1000];
    private Line [] lines = new Line[1000];
    private Point pressed;
    private Point current;
    private  int kLines = 0;
    private int kPoints = 0;
    private int indexOfPointClosestToLeftUpperCorner;
    private int indexOfUpperPoint;
    private int indexOfPointClosestToCenter;
    private int indexOfLineClosestToCenter;
    private Point closestPair;
    private Point farthestPair;
    private int indexOfAlphaLine;

    public void drawLine(Graphics g, Line l) {

        if(l.b == 0) {
            double x = -l.c/l.a;
            g.drawLine((int)x, 0, (int)x, getHeight());
            return;
        }

        double y1 = -l.c/l.b;
        double y2 = (-l.c - l.a * getWidth()) / l.b;

        g.drawLine(0, (int)y1, getWidth(), (int)y2);
    }

    public void fillPoint(Graphics g, int x, int y, int r) {
        g.fillOval(x - r, y - r, 2 * r, 2 * r);
    }

    public void fillPoint(Graphics g, Point p, int r) {
        fillPoint(g, p.x, p.y, r);
    }

    public void drawPoint(Graphics g, int x, int y, int r) {
        g.drawOval(x - r, y - r, 2 * r, 2 * r);
    }

    public void drawPoint(Graphics g, Point p, int r) {
        drawPoint(g, p.x, p.y, r);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for(int i = 0 ; i < kPoints ; i++)
            fillPoint(g, points[i].x, points[i].y, 4);

        if (kPoints > 0) {
            g.setColor(Color.RED);
            fillPoint(g, points[indexOfUpperPoint], 4);

            g.setColor(Color.MAGENTA);
            drawPoint(g, points[indexOfPointClosestToLeftUpperCorner], 6);

            g.setColor(Color.BLACK);
            drawPoint(g, points[indexOfPointClosestToCenter], 10);
        }

        if(kPoints > 1) {
            g.setColor(Color.GREEN);
            g.drawLine(points[closestPair.x].x, points[closestPair.x].y,
                    points[closestPair.y].x, points[closestPair.y].y);

            // farthest

            Point fp1 = points[farthestPair.x];
            Point fp2 = points[farthestPair.y];

            g.setColor(Color.GRAY);

            this.drawLine(g, new Line(fp1.x, fp1.y, fp2.x, fp2.y));

            g.setColor(Color.MAGENTA);
            g.drawLine(fp1.x, fp1.y, fp2.x, fp2.y);
        }

        g.setColor(Color.DARK_GRAY);

        for(int i = 0; i < kLines; i++) {
            drawLine(g, lines[i]);
        }

        if (pressed != null) {
            g.setColor(Color.BLACK);
            fillPoint(g, pressed, 3);
            fillPoint(g, current, 3);
            Line l = new Line(pressed.x, pressed.y, current.x, current.y);
            drawLine(g, l);
        }

        if (kLines > 0) {

            g.setColor(Color.GREEN);
            drawLine(g, lines[indexOfLineClosestToCenter]);

            g.setColor(Color.BLUE);
            drawLine(g, lines[indexOfAlphaLine]);
        }

    }
    public Main(String title) {
        super(title);
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addMouseListener(this);
        addMouseMotionListener(this);
        setVisible(true);
    }
    public static void main(String[] args) {
        Main window = new Main("Заголовок моего окна");
    }

    private void updatePairRelations() {
        if (kPoints < 2)
            return;
        else if (kPoints == 2) {
            closestPair = new Point(0, 1);
            farthestPair = new Point(0, 1);
            return;
        }
        double maxDistance = points[farthestPair.x].distance(points[farthestPair.y]);
        double minDistance = points[closestPair.x].distance(points[closestPair.y]);
        int lastId = kPoints - 1;
        Point newPoint = points[lastId];

        for (int i = 0; i < kPoints - 1; i++) {
            double distance = points[i].distance(newPoint);

            if (distance > maxDistance) {
                farthestPair.x = i;
                farthestPair.y = lastId;
                maxDistance = distance;
            }
            if (distance < minDistance) {
                closestPair.x = i;
                closestPair.y = lastId;
                minDistance = distance;
            }
        }
    }

    private void chooseMinSideDifferenceLine() {
        if (kLines == 0)
            return;

        indexOfAlphaLine = -1;
        int minSideDifference = 9999;

        for(int w = 0; w < kLines; w++) {
            Line l = lines[w];
            int neg = 0;
            int pos = 0;

            for(int i = 0; i < kPoints; i++) {
                double s = l.getSideSign(points[i]);
                if (s < 0)
                    neg++;
                else if(s > 0)
                    pos++;
            }

            int diff = Math.abs(neg - pos);
            if (diff < minSideDifference) {
                minSideDifference = diff;
                indexOfAlphaLine = w;
            }
        }

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) { }
    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        pressed = mouseEvent.getPoint();
    }
    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        Point currentPos = mouseEvent.getPoint();

        if (pressed.distance(currentPos) >= MIN_LINE_LEN) {
            Line l = new Line(currentPos.x, currentPos.y, pressed.x, pressed.y);
            lines[kLines] = l;
            kLines += 1;

            chooseMinSideDifferenceLine();
            chooseLineClosestToCenter();
        }
        else {
            points[kPoints] = mouseEvent.getPoint();

            if (points[kPoints].y < points[indexOfUpperPoint].y)
                indexOfUpperPoint = kPoints;

            if (points[kPoints].distance(0, 0) < points[indexOfPointClosestToLeftUpperCorner].distance(0, 0))
                indexOfPointClosestToLeftUpperCorner = kPoints;

            kPoints++;

            choosePointClosestToCenter();
            chooseMinSideDifferenceLine();
            updatePairRelations();
        }

        pressed = null;

        repaint();
    }
    @Override
    public void mouseEntered(MouseEvent mouseEvent) { }
    @Override
    public void mouseExited(MouseEvent mouseEvent) { }

    private void choosePointClosestToCenter() {
        if(kPoints < 1)
            return;

        indexOfPointClosestToCenter = 0;
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        double distance = points[0].distance(center);

        for(int i = 1; i < kPoints; i++) {
            double newDistance = points[i].distance(center);
            if (newDistance > distance)
                continue;
            indexOfPointClosestToCenter = i;
            distance = newDistance;
        }
    }

    private void chooseLineClosestToCenter() {
        if (kLines < 1)
            return;
        indexOfLineClosestToCenter = 0;
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        double distance = lines[0].distanceToPoint(center.x, center.y);
        for (int i = 1; i < kLines; i++) {
            double newDistance = lines[i].distanceToPoint(center.x, center.y);
            if(newDistance > distance)
                continue;
            indexOfLineClosestToCenter = i;
            distance = newDistance;
        }
    }

    @Override
    public void validate() {
        // resize
        super.validate();
        choosePointClosestToCenter();
        chooseLineClosestToCenter();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.getPoint().distance(pressed) >= MIN_LINE_LEN)
            repaint();
        current = e.getPoint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}