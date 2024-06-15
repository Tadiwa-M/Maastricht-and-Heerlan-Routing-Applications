

import Transport.*;
import dbTables.DirectRoute;
import dbTables.BusStop;
import dbTables.PostAddress;
import dbTables.TransferRoute;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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


    private JFormattedTextField postCodeFromField;
    private JFormattedTextField postCodeToField;
    private JComboBox<String> vehicleBox;
    private String SelectedVehicle;
    private JButton goButton;
    private JButton algorithmButton;
    private JToggleButton busRouteButton;
    private JButton accessibilityButton;


    private JToggleButton footButton;
    private JToggleButton bikeButton;
    private JToggleButton busButton;




    public GUI() {
        setSize(900, 600);
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
        JPanel mapPanel = createMapPanel();


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlPanel, mapPanel);
        splitPane.setDividerLocation(200);
        splitPane.setEnabled(false);
        splitPane.setOneTouchExpandable(true);


        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel);


        setupActionListeners();
    }








    private JPanel createMapPanel() {
        JPanel mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (mapImage != null) {
                    g.drawImage(mapImage, 0, 0, 621, 557, this);
                }
                if (fromPoint != null && toPoint != null) {
                    g.setColor(Color.RED);
                    ((Graphics2D) g).setStroke(new BasicStroke(4));
                    g.drawLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y);
                }
                if (fromPoint != null) {
                    g.setColor(Color.GREEN);
                    g.fillOval(fromPoint.x - 5, fromPoint.y - 5, 10, 10);
                }
                if (toPoint != null) {
                    g.setColor(Color.BLUE);
                    g.fillOval(toPoint.x - 5, toPoint.y - 5, 10, 10);
                }
            }
        };
        mapPanel.setPreferredSize(new Dimension(621, 557));
        return mapPanel;
    }




    private void setupActionListeners() {
        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean accept = buttonClickSharedOperations(postCodeFromField, postCodeToField, vehicleBox, true,accessibilityButton);
                if (!accept) return;
                drawPoints(getAddressFromDataManager(FromCode), getAddressFromDataManager(ToCode));
                mapImageWithPoints = drawPointsOnMap(mapImage, fromPoint, toPoint);
                showStraightLineDistance();
            }
        });


        algorithmButton.addActionListener(e -> {
            boolean accept = buttonClickSharedOperations(postCodeFromField, postCodeToField, vehicleBox, true,accessibilityButton);
            if (!accept)  return;
            try {
                runPathFindingAlgorithm(getAddressFromDataManager(FromCode), getAddressFromDataManager(ToCode));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });


        busRouteButton.addActionListener(e -> {
            Object[] options = showBusRouteOptionsDialog();
            if (options != null) {
                processBusRouteOptions((JCheckBox) options[0], (JTextField) options[1]);
            }
        });




        accessibilityButton.addActionListener(e -> {
            try {
                showPostalCodeAccessibilityScores();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });


    }



    private Object[] showBusRouteOptionsDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JLabel transferLabel = new JLabel("Allow Transfers?");
        JCheckBox transferCheckBox = new JCheckBox();
        JLabel timeLabel = new JLabel("Preferred Time (HH:mm:ss):");
        JTextField timeField = new JTextField();
        JLabel timeNoteLabel = new JLabel("! Keep empty for current time");

        panel.add(transferLabel);
        panel.add(transferCheckBox);
        panel.add(timeLabel);
        panel.add(timeField);
        panel.add(timeNoteLabel);

        int result = JOptionPane.showConfirmDialog(null, panel, "Bus Route Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return new Object[]{transferCheckBox, timeField};
        }
        return null;
    }

    private void processBusRouteOptions(JCheckBox transferCheckBox, JTextField timeField) {
        boolean allowTransfers = transferCheckBox.isSelected();
        String preferredTime = timeField.getText();

        if (preferredTime.isEmpty()) {
            preferredTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        }

        if (!preferredTime.matches("\\d{2}:\\d{2}:\\d{2}")) {
            JOptionPane.showMessageDialog(null, "Invalid time format. Please use HH-mm-ss.");
            return;
        }

        boolean accept = buttonClickSharedOperations(postCodeFromField, postCodeToField, vehicleBox, false, accessibilityButton);
        if (!accept) return;

        PostAddress first = getAddressFromDataManager(FromCode);
        PostAddress last = getAddressFromDataManager(ToCode);

        if (!allowTransfers) {
            handleDirectBusRoute(first, last, preferredTime);
        } else {
            handleTransfers(first, last, preferredTime);
        }

        busRouteButton.setSelected(false);
    }



    private void handleTransfers(PostAddress first, PostAddress last, String preferredTime) {
        BusRouteFinder finder = new BusRouteFinder(first, last);
        TransferRoute route = finder.findShortestTransferRouteWithTime(preferredTime);
        if (route == null){
            noBusError();
            return;
        }
        int length = route.getStartBusStops().size();

        for (BusStop stop: route.getStartBusStops()){
            System.out.println(stop.getStopName());
        }
        System.out.println();

        for (BusStop stop: route.getEndBusStops()){
            System.out.println(stop.getStopName());
        }

        route.getStartBusStops().addAll(route.getEndBusStops());
        List<BusStop> totalBusStops = new ArrayList<>();
        totalBusStops.add(createBusStopFromPostalCode(first));
        totalBusStops.addAll(route.getStartBusStops());
        totalBusStops.add(createBusStopFromPostalCode(last));

        Graphics2D g = (Graphics2D) mapImage.getGraphics();
        DirectRoute directRoute = new DirectRoute(totalBusStops);
        drawShortestPathOnMapBusRoute(g,directRoute, length);
        showBusStopsPopup(directRoute);
    }

    private void handleDirectBusRoute(PostAddress first, PostAddress last, String prefferedTime){
        BusRouteFinder finder = new BusRouteFinder(first, last);
        DirectRoute directRoute = finder.findShortestDirectBusRouteWithTime(prefferedTime);
        if (directRoute == null) {
            noBusError();
            return;
        }
        directRoute.getBusStops().add(0,createBusStopFromPostalCode(first));
        directRoute.getBusStops().add(createBusStopFromPostalCode(last));
        Graphics2D g = (Graphics2D) mapImage.getGraphics();
        drawShortestPathOnMapBusRoute(g, directRoute, 0);
        showBusStopsPopup(directRoute);
    }

    private BusStop createBusStopFromPostalCode(PostAddress postalCode){
        return new BusStop("0", 0,postalCode.getPostalCode() , null, null, (float) postalCode.getLat(), (float) postalCode.getLon(), null);
    }

    private void showPostalCodeAccessibilityScores() throws Exception {
        JFrame parentFrame = new JFrame();
        parentFrame.setResizable(false);
        parentFrame.setVisible(true);
        parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Adjust as needed


        // Create a JPanel to hold your content
        JPanel panel = new JPanel(new BorderLayout());
        parentFrame.add(panel);


        // Create your custom content (JTextArea in a JScrollPane)
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Monospaced font for alignment


        // Build the text content
        StringBuilder scoresBuilder = new StringBuilder("Postal Code Accessibility Scores:\n\n");
        scoresBuilder.append(String.format("%-15s %s\n", "Postal Code", "Accessibility Score (Higher is Better)"));
        scoresBuilder.append("--------------------------------------------------------\n");

        java.util.List<PostAddress> addressList = AddressFinder.getAllAddresses();

        for (PostAddress address: addressList) {
            scoresBuilder.append(String.format("%-15s %s\n", address, AmenetiesCalc.calculateScore(address)));
        }
        textArea.setText(scoresBuilder.toString());


        // Add text area to scroll pane
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);


        // Set frame properties
        parentFrame.setTitle("Accessibility Scores");
        parentFrame.setSize(400, 600);
        parentFrame.setLocationRelativeTo(null); // Center on screen


        // Show the frame
        parentFrame.setVisible(true);
    }







    private void noBusError(){
        JOptionPane.showMessageDialog(null, "There is no bus route that connects these two postal codes");
        busRouteButton.setSelected(false);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }


    private void showBusStopsPopup(DirectRoute route) {
        String routeName = route.getBusStops().get(1).getRouteName();
        StringBuilder stopNames = new StringBuilder("Bus Stops for line (" + routeName + "):\n");
        for (BusStop busStop : route.getBusStops()) {
            stopNames.append(busStop.getStopName()).append("\n");
        }
        String firstArrivalTime = route.getBusStops().get(1).getArrivalTime();
        String lastDepartureTime = route.getBusStops().get(route.getBusStops().size() - 2).getDepartureTime();
        long totalTimeMinutes = calculateTimeDifference(firstArrivalTime, lastDepartureTime);

        stopNames.append("\nTotal Travel Time: ").append(totalTimeMinutes + 5).append(" minutes");

        JOptionPane.showMessageDialog(null, stopNames.toString(), "Bus Route", JOptionPane.INFORMATION_MESSAGE);
    }




    private long calculateTimeDifference(String startTime, String endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime start = LocalTime.parse(startTime, formatter);
        LocalTime end = LocalTime.parse(endTime, formatter);
        return ChronoUnit.MINUTES.between(start, end);
    }
    private void drawShortestPathOnMapBusRoute(Graphics2D g, DirectRoute route, int index) {
        DrawBaseImage(g);





        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(3));



        for (int i = 0; i < route.getBusStops().size() - 1; i++) {
            Point startPoint = findPostCodeCoordinate(route.getBusStops().get(i).getStopLon(), route.getBusStops().get(i).getStopLat());
            Point endPoint = findPostCodeCoordinate(route.getBusStops().get(i + 1).getStopLon(), route.getBusStops().get(i + 1).getStopLat());
            g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);


            if (i == 0) {
//                String route_color = route.getBusStops().get(0).getRouteColor();
                g.setColor(Color.RED);
            }
            else if (i == index){
                g.setColor(Color.BLUE);
            }
            else if(i == route.getBusStops().size() - 3){
                g.setColor(Color.BLACK);
            }
        }
    }
    private JFormattedTextField createPostCodeField(String contents, Point textPanelPosition) {
        JFormattedTextField postCodeField = new JFormattedTextField();
        postCodeField.setPreferredSize(new Dimension(60, 30));
        postCodeField.setText(contents);



        int xOffset = textPanelPosition.x + 100 + 20;
        int yOffset = textPanelPosition.y;
        postCodeField.setBounds(xOffset, yOffset, 80, 30);


        return postCodeField;
    }






    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setPreferredSize(new Dimension(200, 600));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;


        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel fromLabel = new JLabel("From: ");
        controlPanel.add(fromLabel, gbc);


        gbc.gridy++;
        postCodeFromField = createPostCodeField("6211AL", fromLabel.getLocation());
        controlPanel.add(postCodeFromField, gbc);


        gbc.gridy++;
        JLabel toLabel = new JLabel("To: ");
        controlPanel.add(toLabel, gbc);


        gbc.gridy++;
        postCodeToField = createPostCodeField("6225AG", toLabel.getLocation());
        controlPanel.add(postCodeToField, gbc);


        gbc.gridy++;
        footButton = createToggleButton("FOOT", "walk_hollow.png", "walk.png", 20, 20);
        controlPanel.add(footButton, gbc);


        gbc.gridy++;
        bikeButton = createToggleButton("BIKE", "bike_hollow.png", "bike.png", 20, 20);
        controlPanel.add(bikeButton, gbc);


        gbc.gridy++;
        busButton = createToggleButton("CAR", "car_hollow.png", "car.png", 20, 20);
        controlPanel.add(busButton, gbc);


        gbc.gridy++;
        busRouteButton = createBusRouteButton();
        controlPanel.add(busRouteButton, gbc);


        gbc.gridy++;
        gbc.gridwidth = 1;  // Reset gridwidth to 1 for individual buttons
        goButton = createAlgorithmButton("Straight Line");
        controlPanel.add(goButton, gbc);


        gbc.gridy++;
        algorithmButton = createAlgorithmButton("Shortest Distance");
        controlPanel.add(algorithmButton, gbc);


        gbc.gridy++;
        accessibilityButton = createAlgorithmButton("Accessibility Score");
        controlPanel.add(accessibilityButton, gbc);


        return controlPanel;
    }












    private JToggleButton createBusRouteButton(){


        String prefix = "data/img/icons/";
        ImageIcon hollow = new ImageIcon(new ImageIcon(prefix + "bus_hollow.png").getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
        ImageIcon filled = new ImageIcon(new ImageIcon(prefix + "bus.png").getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
        JToggleButton button = new JToggleButton("Bus Route", hollow);
        button.setSelectedIcon(filled);
        button.setPreferredSize(new Dimension(20 + 100, 20));
        return button;


    }
    private JButton createAlgorithmButton(String name){
        JButton algorithmButton = new JButton(name);
        algorithmButton.setPreferredSize(new Dimension(150, 30));
        algorithmButton.setBackground(Color.WHITE);
        algorithmButton.setForeground(Color.BLUE);
        return algorithmButton;
    }






    private JToggleButton createToggleButton(String text, String hollowIcon, String fillIcon, int width, int height) {
        String prefix = "data/img/icons/";
        ImageIcon hollow = new ImageIcon(new ImageIcon(prefix + hollowIcon).getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
        ImageIcon filled = new ImageIcon(new ImageIcon(prefix + fillIcon).getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
        JToggleButton button = new JToggleButton(text, hollow);
        button.setSelectedIcon(filled);
        button.setPreferredSize(new Dimension(width + 100, height));
        button.addActionListener(e -> {
            footButton.setSelected(false);
            bikeButton.setSelected(false);
            busButton.setSelected(false);
            button.setSelected(true);
            encodeVehicle(text);
            SelectedVehicle = text;
        });
        return button;
    }






    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(() -> {
                GUI frame = new GUI();
                frame.setVisible(true);
            });
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An error occurred while initializing the GUI");
            main(args);
        }
    }






    private boolean buttonClickSharedOperations(JFormattedTextField postCodeFromField, JFormattedTextField postCodeToField, JComboBox<String> vehicleBox, boolean careForVehicle, JButton accessibilityButton){
        DrawBaseImage((Graphics2D) mapImage.getGraphics());
        boolean accept = true;
        repaint();
        fromPoint = null;
        toPoint = null;
        String codeToString = postCodeToField.getText().replace(" ", "");
        String codeFromString = postCodeFromField.getText().replace(" ", "");


        accept = buttonClickConditionals(codeFromString,codeToString);
        boolean vehicleFlag = true;
        if (careForVehicle && SelectedVehicle == null){
            vehicleFlag = false;
            JOptionPane.showMessageDialog(null, "Foreign vehicle or none selected. \nPlease select another and try again");


        }
        accept = accept && vehicleFlag;
        if (accept){
            ToCode = codeToString;
            FromCode = codeFromString;
        }
        return accept;
    }



    private boolean buttonClickConditionals(String codeFrom, String codeTo){
        if (!acceptCode(codeTo)) {
            JOptionPane.showMessageDialog(null, "The \"TO\" PostCode is Not in the proper format\nFormat: 1234AB or 1234 AB");
            return false;
        }
        if (!acceptCode(codeFrom)) {
            JOptionPane.showMessageDialog(null, "The \"FROM\" PostCode is Not in the proper format\nFormat: 1234AB or 1234 AB");
            return false;
        }
        if (codeTo.equals(codeFrom)) {
            JOptionPane.showMessageDialog(null, "The Post Codes are the same\n No distance between them");
            return false;
        }
        if(getAddressFromDataManager(codeFrom) == null){
            JOptionPane.showMessageDialog(null, "The \"FROM\" PostCode is not valid");
            return false;
        }
        if(getAddressFromDataManager(codeTo) == null){
            JOptionPane.showMessageDialog(null, "The \"TO\" PostCode is not valid");
            return false;
        }
        return true;
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


    private void runPathFindingAlgorithm(PostAddress from, PostAddress to) throws Exception {
//        Graph graph = new GraphCreator().createGraph();
//        ShortestPathFinder pathFinder = new ShortestPathFinder(graph);
//        pathFinder.setShortestPathAlgorithm(new Dijkstra(graph));


       /*
       ArrayList<dbTables.PostAddress> shortestPath = pathFinder.findPath(from, to);
       double value = pathFinder.getDistance(from, to);
       */


        GraphHopperUtil graphHopperUtil = new GraphHopperUtil();
        QueryResponse queryResponse = graphHopperUtil.calculateRoute(from.getPostalCode(), to.getPostalCode(), currentVehicle.toString().toLowerCase());


        double value = queryResponse.getDistance() / 1000;
        ArrayList<PostAddress> shortestPath = queryResponse.getPath();


        visualizeShortestPath(shortestPath);


        value = Double.parseDouble(new DecimalFormat("##.##").format(value));


        Vehicle vehicle;
        if (currentVehicle == VehicleType.FOOT) {
            vehicle = new Foot(value);
        } else if (currentVehicle == VehicleType.BIKE)
            vehicle = new Bike(value);
        else if (currentVehicle == VehicleType.CAR){
            vehicle = new Car(value);
        }
        else {

            JOptionPane.showMessageDialog(null, "The vehicle is not supported");
            return;
        }
        showAlgorithmDistanceMessage(value, vehicle);
    }


    private void showAlgorithmDistanceMessage(double value, Vehicle vehicle){
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



        Graphics2D g = (Graphics2D) mapImage.getGraphics();
        drawShortestPathOnMap(g, shortestPath);


        repaint();
    }



    private void drawShortestPathOnMap(Graphics2D g, ArrayList<PostAddress> shortestPath) {

        DrawBaseImage(g);





        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(3));



        for (int i = 0; i < shortestPath.size() - 1; i++) {
            Point startPoint = findPostCodeCoordinate(shortestPath.get(i).getLon(), shortestPath.get(i).getLat());
            Point endPoint = findPostCodeCoordinate(shortestPath.get(i + 1).getLon(), shortestPath.get(i + 1).getLat());
            g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        }
    }


    private void DrawBaseImage(Graphics2D g) {
        g.drawImage(clearMapImage, 0, 0, null);
    }


    public static boolean encodeVehicle(String vehicle) {
        try{
            currentVehicle = VehicleType.valueOf(vehicle.toUpperCase());
            return false;
        }
        catch (NullPointerException e){
            return true;
        }


    }






    public PostAddress getAddressFromDataManager(String postalCode) {
        try {
            return AddressFinder.getAddress(postalCode);
        } catch (Exception e) {
            // Handle the exception gracefully, such as logging an error message
            e.printStackTrace(); // Print the stack trace for debugging purposes
            return null; // Return null to indicate that the address retrieval failed
        }
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

