import Transport.Bike;
import Transport.Foot;
import Transport.Vehicle;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class GUI extends JFrame {

    public static String FromCode = "FROM";
    public static String ToCode = "TO";
    public static int currentVehicleCode = 0;
    public static final double minLat = 50.871838;
    public static final double maxLon = 5.745668;
    public static final double minLon = 5.638466;
    public static final double maxLat = 50.812057;

    private BufferedImage mapImageWithPoints;

    public static Point fromPoint;
    public static Point toPoint;
    private BufferedImage mapImage;

    public GUI() {
        setSize(700, 750);
        setResizable(false);
        setTitle("Distance Calculator of Maastricht Postal Codes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {

            mapImage = ImageIO.read(GUI.class.getResource("Map.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel fromLabel = new JLabel("From: ");
        controlPanel.add(fromLabel, gbc);

        JFormattedTextField postCodeFromField = new JFormattedTextField();
        postCodeFromField.setPreferredSize(new Dimension(100, 30));
        postCodeFromField.setText("6211AL");
        gbc.gridx++;
        controlPanel.add(postCodeFromField, gbc);

        JLabel toLabel = new JLabel("To: ");
        gbc.gridx++;
        controlPanel.add(toLabel, gbc);

        JFormattedTextField postCodeToField = new JFormattedTextField();
        postCodeToField.setPreferredSize(new Dimension(100, 30));
        postCodeToField.setText("6225AG");
        gbc.gridx++;
        controlPanel.add(postCodeToField, gbc);

        String[] vehicleList = {"Walk", "Bike"};
        JComboBox<String> vehicleBox = new JComboBox<>(vehicleList);
        vehicleBox.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 4;
        controlPanel.add(vehicleBox, gbc);

        JButton goButton = new JButton("Go");
        goButton.setPreferredSize(new Dimension(100, 30));
        goButton.setBackground(Color.WHITE);
        goButton.setForeground(Color.BLUE);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        controlPanel.add(goButton, gbc);

        JButton dijkstraButton = new JButton("Run Dijkstra");
        dijkstraButton.setPreferredSize(new Dimension(150, 30));
        gbc.gridy++;
        controlPanel.add(dijkstraButton, gbc);

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        add(mainPanel);

        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean accept = true;
                String codeTo = postCodeToField.getText().replace(" ", "");
                String codeFrom = postCodeFromField.getText().replace(" ", "");

                if (!acceptCode(codeTo)) {
                    JOptionPane.showMessageDialog(null, "The \"TO\" PostCode is Not in the proper format\nFormat: 1234AB or 1234 AB");
                    accept = false;
                }
                if (!acceptCode(codeFrom)) {
                    JOptionPane.showMessageDialog(null, "The \"FROM\" PostCode is Not in the proper format\nFormat: 1234AB or 1234 AB");
                    accept = false;
                }
                if (codeTo.equals(codeFrom)) {
                    JOptionPane.showMessageDialog(null, "The Post Codes are the same\n No distance between them");
                    accept = false;
                }
                if (accept) {
                    ToCode = codeTo;
                    FromCode = codeFrom;
                    String path = "data/distances.csv";
                    HashMap<String, PostAddress> postAddress = Utilities.initPostAddressMap(path);
                    PostAddress toAddress = postAddress.get(codeTo);
                    PostAddress fromAddress = postAddress.get(codeFrom);

                    drawPoints(fromAddress, toAddress);
                    mapImageWithPoints = drawPointsOnMap(mapImage, fromPoint, toPoint);
                    String selectedVehicle = (String) vehicleBox.getSelectedItem();
                    encodeVehicle(selectedVehicle);
                }
            }
        });

        dijkstraButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean accept = true;
                String codeTo = postCodeToField.getText().replace(" ", "");
                String codeFrom = postCodeFromField.getText().replace(" ", "");

                if (!acceptCode(codeTo)) {
                    JOptionPane.showMessageDialog(null, "The \"TO\" PostCode is Not in the proper format\nFormat: 1234AB or 1234 AB");
                    accept = false;
                }
                if (!acceptCode(codeFrom)) {
                    JOptionPane.showMessageDialog(null, "The \"FROM\" PostCode is Not in the proper format\nFormat: 1234AB or 1234 AB");
                    accept = false;
                }
                if (codeTo.equals(codeFrom)) {
                    JOptionPane.showMessageDialog(null, "The Post Codes are the same\n No distance between them");
                    accept = false;
                }
                if (accept) {
                    ToCode = codeTo;
                    FromCode = codeFrom;
                    String path = "data/distances.csv";
                    HashMap<String, PostAddress> postAddress = Utilities.initPostAddressMap(path);
                    PostAddress toAddress = postAddress.get(codeTo);
                    PostAddress fromAddress = postAddress.get(codeFrom);
                    String selectedVehicle = (String) vehicleBox.getSelectedItem();
                    encodeVehicle(selectedVehicle);
                    runDijkstraAlgorithm(fromAddress, toAddress);
                }

            }
        });

    }

    private BufferedImage drawPointsOnMap(BufferedImage mapImage, Point fromPoint, Point toPoint) {
        BufferedImage imageWithPoints = new BufferedImage(mapImage.getWidth(), mapImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) imageWithPoints.getGraphics();

        g.drawImage(mapImage, 0, 0, null);

        if (fromPoint != null && toPoint != null) {
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(5));
            g.drawLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y);

            g.setColor(Color.GREEN);
            g.fillOval(fromPoint.x - 10, fromPoint.y - 10, 20, 20);

            g.setColor(Color.BLUE);
            g.fillOval(toPoint.x - 10, toPoint.y - 10, 20, 20);
        }

        g.dispose();

        return imageWithPoints;
    }

    private void runDijkstraAlgorithm(PostAddress from, PostAddress to) {

        PostAddress startAddress = from;
        PostAddress endAddress = to;

        ShortestPathFinder pathFinder = new ShortestPathFinder();

        ArrayList<PostAddress> shortestPath = pathFinder.findPath(startAddress, endAddress);
        double value = pathFinder.getDistance(startAddress,endAddress);
        value = Double.parseDouble(new DecimalFormat("##.##").format(value));
        visualizeShortestPath(shortestPath);
        double speed = 0;
        if (currentVehicleCode == 0){
            Vehicle vehicle = new Foot(value);

            speed = vehicle.calculateTime();
        } else if (currentVehicleCode == 1) {
            Vehicle vehicle = new Bike(value);
            speed = vehicle.calculateTime();
        }
        speed = Double.parseDouble(new DecimalFormat("##.##").format(speed));

        JOptionPane.showMessageDialog(null, "Distance: " + value + "km\nCompleted In: " + (int) speed + " minutes");
    }

    private void visualizeShortestPath(ArrayList<PostAddress> shortestPath) {

        if (shortestPath.size() >= 2) {
            fromPoint = findPostCodeCoordinate(shortestPath.get(0).getLon(), shortestPath.get(0).getLat());
            toPoint = findPostCodeCoordinate(shortestPath.get(shortestPath.size() - 1).getLon(), shortestPath.get(shortestPath.size() - 1).getLat());
        }

        mapImage = drawShortestPathOnMap(mapImage, shortestPath);

        repaint();
    }

    private BufferedImage drawShortestPathOnMap(BufferedImage mapImage, ArrayList<PostAddress> shortestPath) {
        BufferedImage imageWithShortestPath = new BufferedImage(mapImage.getWidth(), mapImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) imageWithShortestPath.getGraphics();

        g.drawImage(mapImage, 0, 0, null);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(3));

        for (int i = 0; i < shortestPath.size() - 1; i++) {
            Point startPoint = findPostCodeCoordinate(shortestPath.get(i).getLon(), shortestPath.get(i).getLat());
            Point endPoint = findPostCodeCoordinate(shortestPath.get(i + 1).getLon(), shortestPath.get(i + 1).getLat());
            g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        }

        g.dispose();

        return imageWithShortestPath;
    }




    public static void encodeVehicle(String vehicle) {
        switch (vehicle) {
            case "Walk":
                currentVehicleCode = 0;
                break;
            case "Bike":
                currentVehicleCode = 1;
                break;
            default:
                System.out.println("ERROR");
                break;
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        if (mapImage != null) {
            g.drawImage(mapImage, 50, 200, 600, 500, this);
        }
        if (fromPoint != null && toPoint != null) {
            g.setColor(Color.RED);
            g2.setStroke(new BasicStroke(4));
            g2.drawLine(fromPoint.x + 30, fromPoint.y + 180, toPoint.x + 30, toPoint.y + 180);
        }
        if (fromPoint != null) {
            g.setColor(Color.GREEN);
            g.fillOval(fromPoint.x - 5 + 30, fromPoint.y - 5 + 180, 10, 10);
        }
        if (toPoint != null) {
            g.setColor(Color.BLUE);
            g.fillOval(toPoint.x - 5 + 30, toPoint.y - 5 + 180, 10, 10);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI frame = new GUI();
            frame.setVisible(true);
        });
    }

    public boolean acceptCode(String code) {
        if (code.length() != 6) {
            return false;
        }
        return true;
    }

    public Point findPostCodeCoordinate(double lon, double lat) {
        int imageWidth = mapImage.getWidth();
        int imageHeight = mapImage.getHeight();
        double lonPercent = (lon - minLon) / (maxLon - minLon);
        double latPercent = (lat - minLat) / (maxLat - minLat);
        int xPixel = (int) (lonPercent * imageWidth);
        int yPixel = (int) (latPercent * imageHeight);

        return new Point(xPixel, yPixel);
    }

    public void drawPoints(PostAddress from, PostAddress to) {
        double fromLon = from.getLon();
        double fromLat = from.getLat();
        double toLon = to.getLon();
        double toLat = to.getLat();

        fromPoint = findPostCodeCoordinate(fromLon, fromLat);
        toPoint = findPostCodeCoordinate(toLon, toLat);

        repaint();
    }
}