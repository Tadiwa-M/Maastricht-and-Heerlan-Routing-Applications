import Transport.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class GUI extends JFrame {

    public static String FromCode = "FROM";
    public static String ToCode = "TO";
    public static VehicleType currentVehicle = VehicleType.FOOT;
    public static final double minLat = 50.871838;
    public static final double maxLon = 5.745668;
    public static final double minLon = 5.638466;
    public static final double maxLat = 50.812057;

    private BufferedImage mapImageWithPoints;
    private BufferedImage clearMapImage;

    public static Point fromPoint;
    public static Point toPoint;
    private BufferedImage mapImage;
    public static GridBagConstraints gbc;

    public GUI() {
        setSize(700, 750);
        setResizable(false);
        setTitle("Distance Calculator using Maastricht Postal Codes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {

            mapImage = ImageIO.read(new File("data/img/Map.png"));
            clearMapImage = ImageIO.read(new File("data/img/Map.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));


        JPanel controlPanel = createControlPanel();

        JLabel fromLabel = new JLabel("From: ");
        controlPanel.add(fromLabel, gbc);


        JFormattedTextField postCodeFromField = createPostCodeField("6211AL");

        controlPanel.add(postCodeFromField, gbc);

        JLabel toLabel = new JLabel("To: ");
        gbc.gridx++;
        controlPanel.add(toLabel, gbc);

        JFormattedTextField postCodeToField = createPostCodeField("6225AG");
        controlPanel.add(postCodeToField, gbc);

        JComboBox<String> vehicleBox = createVehicleBox();
        controlPanel.add(vehicleBox, gbc);

        JButton goButton = createGoButton();
        controlPanel.add(goButton, gbc);

        JButton dijkstraButton = createDijkstraButton();
        controlPanel.add(dijkstraButton, gbc);

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        add(mainPanel);

        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean accept = buttonClickSharedOperations(postCodeFromField, postCodeToField,vehicleBox);
                drawPoints(getAddressFromDataManager(FromCode), getAddressFromDataManager(ToCode));
                mapImageWithPoints = drawPointsOnMap(mapImage, fromPoint, toPoint);
                showStraightLineDistance();

            }
        });

        dijkstraButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean accept = buttonClickSharedOperations(postCodeFromField, postCodeToField,vehicleBox);
                if (!accept){return;}
                runPathFindingAlgorithm(getAddressFromDataManager(FromCode), getAddressFromDataManager(ToCode));

            }
        });

    }

    private JFormattedTextField createPostCodeField(String contents){
        JFormattedTextField postCodeField = new JFormattedTextField();
        postCodeField.setPreferredSize(new Dimension(100, 30));
        postCodeField.setText(contents);
        gbc.gridx++;
        return postCodeField;
    }

    private JPanel createControlPanel(){
        JPanel controlPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        return controlPanel;
    }

    private JButton createGoButton() {
        JButton goButton = new JButton("Go");
        goButton.setPreferredSize(new Dimension(100, 30));
        goButton.setBackground(Color.WHITE);
        goButton.setForeground(Color.BLUE);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        return goButton;
    }
    private JComboBox<String> createVehicleBox(){
        String[] vehicleList = {"Walk", "Bike", "Bus"};
        JComboBox<String> vehicleBox = new JComboBox<>(vehicleList);
        vehicleBox.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 4;
        return vehicleBox;
    }
    private JButton createDijkstraButton(){
        JButton dijkstraButton = new JButton("Run Algorithm");
        dijkstraButton.setPreferredSize(new Dimension(150, 30));
        gbc.gridy++;
        return dijkstraButton;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI frame = new GUI();
            frame.setVisible(true);
        });
    }


    //This function does some shared variable initializations, and checks whether or not the operations that follow them in the action listeners should be executed,
    // returns the value of the accept variable which tells us if any violations happened or not
    private boolean buttonClickSharedOperations(JFormattedTextField postCodeFromField, JFormattedTextField postCodeToField, JComboBox<String> vehicleBox){
        DrawBaseImage((Graphics2D) mapImage.getGraphics(), mapImage);
        boolean accept = true;
        repaint();
        fromPoint = null;
        toPoint = null;
        String codeToString = postCodeToField.getText().replace(" ", "");
        String codeFromString = postCodeFromField.getText().replace(" ", "");

        buttonClickConditionals(codeFromString,codeToString);

        if (accept){
            ToCode = codeToString;
            FromCode = codeFromString;
            String selectedVehicle = (String) vehicleBox.getSelectedItem();
            assert selectedVehicle != null;
            encodeVehicle(selectedVehicle);
        }
        return accept;
    }

    //works with the buttonClickSharedOperations() method to allow or disallow certain post codes depending on their format, returns the accept code based on the conditionals
    private boolean buttonClickConditionals(String codeFrom, String codeTo){
        boolean accept = true;
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
        return accept;
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

    private void runPathFindingAlgorithm(PostAddress from, PostAddress to) {
        Graph graph = new GraphCreator().createGraph();
        ShortestPathFinder pathFinder = new ShortestPathFinder(graph);
        pathFinder.setShortestPathAlgorithm(new Dijkstra(graph));

        ArrayList<PostAddress> shortestPath = pathFinder.findPath(from, to);
        double value = pathFinder.getDistance(from, to);

        value = Double.parseDouble(new DecimalFormat("##.##").format(value));

        visualizeShortestPath(shortestPath);

        Vehicle vehicle;
        if (currentVehicle == VehicleType.FOOT){
            vehicle = new Foot(value);
        } else if (currentVehicle == VehicleType.BIKE)
            vehicle = new Bike(value);
        else {
            System.out.println("ERROR");
            vehicle = null;
        }
        showDijkstraDistanceMessage(value, vehicle);
        double time = Double.parseDouble(new DecimalFormat("##.##").format(vehicle.calculateTime()));

        JOptionPane.showMessageDialog(null, "Distance: " + value + "km\nCompleted In: " + (int) time + " minutes");
    }

    private void showDijkstraDistanceMessage(double value, Vehicle vehicle){
        double time = Double.parseDouble(new DecimalFormat("##.##").format(vehicle.calculateTime()));
        JOptionPane.showMessageDialog(null, "Distance: " + value + "km\nCompleted In: " + (int) time + " minutes");

    }
    private void showStraightLineDistance(){
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        float distance = Float.parseFloat(df.format(PostAddress.basicDistances(getAddressFromDataManager(FromCode),getAddressFromDataManager(ToCode))));
        JOptionPane.showMessageDialog(null, "The Bird's Flight Distance is: " + distance + "km");
    }

    private void visualizeShortestPath(ArrayList<PostAddress> shortestPath) {

        if (shortestPath.size() >= 2) {
            fromPoint = findPostCodeCoordinate(shortestPath.get(0).getLon(), shortestPath.get(0).getLat());
            toPoint = findPostCodeCoordinate(shortestPath.get(shortestPath.size() - 1).getLon(), shortestPath.get(shortestPath.size() - 1).getLat());
        }

        // Assuming this code is inside a paintComponent or similar method where you have access to Graphics2D object
        Graphics2D g = (Graphics2D) mapImage.getGraphics();
        drawShortestPathOnMap(g, mapImage, shortestPath);

        repaint();
    }

    //the drawing method of the Dijkstra/A_STAR Algorithm,
    private void drawShortestPathOnMap(Graphics2D g, BufferedImage mapImage, ArrayList<PostAddress> shortestPath) {
        // Draw the mapImage

        DrawBaseImage(g,mapImage);


        // Draw the shortest path
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(3));

        //draw line between two points as long as the end has not been reacher, gives the impression of a continuous line
        for (int i = 0; i < shortestPath.size() - 1; i++) {
            Point startPoint = findPostCodeCoordinate(shortestPath.get(i).getLon(), shortestPath.get(i).getLat());
            Point endPoint = findPostCodeCoordinate(shortestPath.get(i + 1).getLon(), shortestPath.get(i + 1).getLat());
            g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        }
    }

    private void DrawBaseImage(Graphics2D g, BufferedImage mapImage) {
        g.drawImage(clearMapImage, 0, 0, null);
    }

    public static void encodeVehicle(String vehicle) {
        currentVehicle = VehicleType.valueOf(vehicle.toUpperCase());
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

    public PostAddress getAddressFromDataManager(String postalCode) {
        return AddressFinder.getAddress(postalCode);
    }

    public boolean acceptCode(String code) {
        return code.length() == 6;
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
        fromPoint = findPostCodeCoordinate(from.getLon(), from.getLat());
        toPoint = findPostCodeCoordinate(to.getLon(), to.getLat());

        repaint();
    }
}