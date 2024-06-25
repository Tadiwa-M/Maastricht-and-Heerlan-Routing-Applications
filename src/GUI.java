import dbTables.*;

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
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class GUI extends JFrame {

    //used to store the input field data
    public static String FromCode = "FROM";
    public static String ToCode = "TO";
    public static VehicleType currentVehicle = VehicleType.FOOT;

    //Bounds of map, used to draw on the map image
    public static final double minLat = 50.871838;
    public static final double maxLon = 5.745668;
    public static final double minLon = 5.638466;
    public static final double maxLat = 50.812057;

    //Map Images, one clear and the other used to draw on top of
    private BufferedImage clearMapImage;
    private BufferedImage mapImage;

    //Elements of main frame
    private JFormattedTextField postCodeFromField;
    private JFormattedTextField postCodeToField;

    private String SelectedVehicle;

    private JButton goButton;
    private JButton algorithmButton;
    private JButton accessibilityButton;

    private JToggleButton busRouteButton;
    private JToggleButton footButton;
    private JToggleButton bikeButton;
    private JToggleButton carButton;


    //main method that creates GUI object
    public static void main(String[] args) {
        try {
            CompletableFuture<Void> guiFuture = CompletableFuture.runAsync(() -> {
                SwingUtilities.invokeLater(() -> {
                    GUI frame = new GUI();
                    frame.setVisible(true);
                });
            });

            CompletableFuture<Void> gtfsLoaderFuture = CompletableFuture.runAsync(GTFSLoader::loadGraph);

            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(guiFuture, gtfsLoaderFuture);
            combinedFuture.join();  // Wait for both tasks to complete
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An error occurred while initializing the GUI or loading the graph");
        }
    }

    //method that creates GUI object
    public GUI() {
        //create Main window
        setSize(900, 600);
        setResizable(false);
        setTitle("Maastricht Route Finder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        //initialize Images
        try {
            mapImage = ImageIO.read(new File("data/img/Map.png"));
            clearMapImage = ImageIO.read(new File("data/img/Map.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));


        //create map panel
        JPanel controlPanel = createControlPanel();
        JPanel mapPanel = createMapPanel();

        //create utility panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlPanel, mapPanel);
        splitPane.setDividerLocation(200);
        splitPane.setEnabled(false);
        splitPane.setOneTouchExpandable(false);

        //assemble Main Frame
        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel);

        //Setup Action Listeners
        setupActionListeners();
    }

    //creates the map Image
    private JPanel createMapPanel() {
        JPanel mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (mapImage != null) {
                    g.drawImage(mapImage, 0, 0, 621, 557, this);
                }

            }
        };
        mapPanel.setPreferredSize(new Dimension(621, 557));
        return mapPanel;
    }


    private void drawStraightLine(PostAddress startPoint, PostAddress endPoint){
        Graphics2D g = getMapGraphics();

        Point fromPoint = findPostCodeCoordinate(startPoint.getLon(), startPoint.getLat());
        Point toPoint = findPostCodeCoordinate(endPoint.getLon(), endPoint.getLat());
        g.setColor(Color.RED);
        ((Graphics2D) g).setStroke(new BasicStroke(4));
        g.drawLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y);
        g.setColor(Color.GREEN);
        g.fillOval(fromPoint.x - 5, fromPoint.y - 5, 10, 10);
        g.setColor(Color.BLUE);
        g.fillOval(toPoint.x - 5, toPoint.y - 5, 10, 10);
    }

    private double getDistance() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return LineDistanceCalculator.basicDistances(getAddressFromDataManager(FromCode),getAddressFromDataManager(ToCode));
    }

    //sets up action Listeners for the buttons in the main frame
    private void setupActionListeners() {
        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean accept = buttonClickSharedOperations(postCodeFromField, postCodeToField,true);
                if (!accept) return;
                PostAddress fromAddress = getAddressFromDataManager(FromCode);
                PostAddress toAddress = getAddressFromDataManager(ToCode);
                drawStraightLine(fromAddress,toAddress);
                double distance = getDistance();
                showAlgorithmDistanceMessage(0, distance);
            }
        });


        algorithmButton.addActionListener(e -> {
            boolean accept = buttonClickSharedOperations(postCodeFromField, postCodeToField,true);
            if (!accept)  return;
            try {
                runPathFindingAlgorithm(getAddressFromDataManager(FromCode), getAddressFromDataManager(ToCode));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });


        busRouteButton.addActionListener(e -> {
            untoggleAllButtons();
            Object[] options = showBusRouteOptionsDialog();
            if (options != null) {
                try {
                    processBusRouteOptions((JCheckBox) options[0], (JRadioButton) options[1] , (JSpinner) options[2], (JSpinner) options[3], (JSpinner) options[4]);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });




        accessibilityButton.addActionListener(e -> {
            accessibilityScoreDialog();
        });


    }

    //untoggles all buttons, quite self explanatory, nice for visual reasons
    private void untoggleAllButtons(){
        footButton.setSelected(false);
        bikeButton.setSelected(false);
        carButton.setSelected(false);
    }

    //Opens this dialog when the Accessibility Score button gets pressed, gives option to find the score of a single postal code or all by leaving the input field empty, also choice to open the heat map that is created live
    private  void accessibilityScoreDialog() {
        JFrame frame = new JFrame("Accessibility Score Calculator");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLayout(new FlowLayout());


        JLabel postalCodeLabel = new JLabel("Postal Code:");
        frame.add(postalCodeLabel);


        JTextField postalCodeTextField = new JTextField(20);
        frame.add(postalCodeTextField);


        JButton submitButton = new JButton("Submit");
        frame.add(submitButton);


        JButton heatMapButton = new JButton("HeatMap");
        frame.add(heatMapButton);


        List<AddressScore> scores = new ArrayList<>();


        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String postalCode = postalCodeTextField.getText();
                PostAddress postAddress = getAddressFromDataManager(postalCode);
                if (postAddress != null) {

                    AmenitiesCalculator.calculateAllScores(scores);
                    AddressScore addressScore = AmenitiesCalculator.getAddressScore(postAddress.getPostalCode(), scores);

                    if (addressScore != null) {

                        frame.dispose();
                        displayScores(postalCode, addressScore.getShopScore(), addressScore.getAmenityScore(), addressScore.getTourismScore(), addressScore.getScore());
                    } else {
                        JOptionPane.showMessageDialog(frame, "Score not found for the given postal code", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    AmenitiesCalculator.calculateAllScores(scores);
                    displayAllScores(scores);
                }
            }
        });


        heatMapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                AmenitiesCalculator.calculateAllScores(scores);

                createHeatMap(scores);
                repaint();

            }
        });


        frame.setVisible(true);
    }

    //Displays all the postal codes as well as their scores, calculated live
    private void displayAllScores(List<AddressScore> scores) {
        DecimalFormat df = new DecimalFormat("#.###");

        JFrame resultFrame = new JFrame("All Accessibility Scores");
        resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        resultFrame.setSize(800, 400);
        resultFrame.setLayout(new GridBagLayout());
        resultFrame.setResizable(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        gbc.gridx = 0;
        gbc.gridy = 0;
        resultFrame.add(new JLabel("Postal Code"), gbc);

        gbc.gridx = 1;
        resultFrame.add(new JLabel("Shop Score"), gbc);

        gbc.gridx = 2;
        resultFrame.add(new JLabel("Amenity Score"), gbc);

        gbc.gridx = 3;
        resultFrame.add(new JLabel("Tourism Score"), gbc);

        gbc.gridx = 4;
        resultFrame.add(new JLabel("Total Score"), gbc);


        for (int i = 0; i < scores.size(); i++) {
            AddressScore addressScore = scores.get(i);

            gbc.gridy = i + 1;
            gbc.gridx = 0;
            resultFrame.add(new JLabel(addressScore.getAddress().getPostalCode()), gbc);

            gbc.gridx = 1;
            resultFrame.add(new JLabel(df.format(addressScore.getShopScore())), gbc);

            gbc.gridx = 2;
            resultFrame.add(new JLabel(df.format(addressScore.getAmenityScore())), gbc);

            gbc.gridx = 3;
            resultFrame.add(new JLabel(df.format(addressScore.getTourismScore())), gbc);

            gbc.gridx = 4;
            resultFrame.add(new JLabel(df.format(addressScore.getScore())), gbc);
        }


        JScrollPane scrollPane = new JScrollPane(resultFrame.getContentPane());
        JFrame scrollableFrame = new JFrame("All Accessibility Scores");
        scrollableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        scrollableFrame.setSize(800, 400);
        scrollableFrame.add(scrollPane);
        scrollableFrame.setVisible(true);
    }

    //creates the heat Map when its button gets clicked, loops over all the AddressScore object in the scores list and handles them accordingly
    public void createHeatMap(List<AddressScore> scores) {
        Graphics2D g = getMapGraphics();
        DrawBaseImage(g);

        int counter = 0;
        for (AddressScore addressScore : scores) {

            //find coordinates for placement on the map image and the score to decide on the hue with later calculations
            double lon = addressScore.getAddress().getLon();
            double lat = addressScore.getAddress().getLat();
            double score = addressScore.getScore();

            //get the color of the point
            Color color = getColorForScore(score);
            Point point = findPostCodeCoordinate(lon, lat);

            if (point != null) {

                g.setColor(color);
                g.fillOval(point.x - 5, point.y - 5, 10, 10);
            } else {
                System.err.println("Invalid coordinates for lon: " + lon + ", lat: " + lat);
            }
        }
    }


    //find the proper color of the point using its score, blue is lowest score and red the highest score
    private static Color getColorForScore(double score) {
        int red = (int) (255 * (score / 100));
        int blue = 255 - red;
        return new Color(red, 0, blue);
    }

    //similar to displayAllScores but for a single one
    private static void displayScores(String postalCode, double shopScore, double amenityScore, double tourismScore, double totalScore) {
        DecimalFormat df = new DecimalFormat("#.###");

        JFrame resultFrame = new JFrame("Accessibility Scores");
        resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        resultFrame.setSize(600, 120);
        resultFrame.setLayout(new GridBagLayout());
        resultFrame.setResizable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        gbc.gridx = 0;
        gbc.gridy = 0;
        resultFrame.add(new JLabel("Postal Code:"), gbc);

        gbc.gridx = 1;
        resultFrame.add(new JLabel("Shop Score:"), gbc);

        gbc.gridx = 2;
        resultFrame.add(new JLabel("Amenity Score:"), gbc);

        gbc.gridx = 3;
        resultFrame.add(new JLabel("Tourism Score:"), gbc);

        gbc.gridx = 4;
        resultFrame.add(new JLabel("Total Score:"), gbc);


        gbc.gridy = 1;
        gbc.gridx = 0;
        resultFrame.add(new JLabel(postalCode), gbc);

        gbc.gridx = 1;
        resultFrame.add(new JLabel(df.format(shopScore)), gbc);

        gbc.gridx = 2;
        resultFrame.add(new JLabel(df.format(amenityScore)), gbc);

        gbc.gridx = 3;
        resultFrame.add(new JLabel(df.format(tourismScore)), gbc);

        gbc.gridx = 4;
        resultFrame.add(new JLabel(df.format(totalScore)), gbc);


        resultFrame.setVisible(true);
    }


    //Allows the user to choose between transfer or non and timed or non bus route between two postal codes
    private Object[] showBusRouteOptionsDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JLabel transferLabel = new JLabel("Allow Transfers?");
        JCheckBox transferCheckBox = new JCheckBox();

        JRadioButton currentTimeButton = new JRadioButton("Use Current Time");
        JRadioButton manualTimeButton = new JRadioButton("Enter Manual Time");
        ButtonGroup timeOptionGroup = new ButtonGroup();
        timeOptionGroup.add(currentTimeButton);
        timeOptionGroup.add(manualTimeButton);
        currentTimeButton.setSelected(true);

        JPanel timePanel = new JPanel(new GridLayout(1, 6));
        JLabel hourLabel = new JLabel("HH:");
        JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
        JLabel minuteLabel = new JLabel("mm:");
        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        JLabel secondLabel = new JLabel("ss:");
        JSpinner secondSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));

        timePanel.add(hourLabel);
        timePanel.add(hourSpinner);
        timePanel.add(minuteLabel);
        timePanel.add(minuteSpinner);
        timePanel.add(secondLabel);
        timePanel.add(secondSpinner);


        hourSpinner.setEnabled(false);
        minuteSpinner.setEnabled(false);
        secondSpinner.setEnabled(false);


        currentTimeButton.addActionListener(e -> {
            hourSpinner.setEnabled(false);
            minuteSpinner.setEnabled(false);
            secondSpinner.setEnabled(false);
        });

        manualTimeButton.addActionListener(e -> {
            hourSpinner.setEnabled(true);
            minuteSpinner.setEnabled(true);
            secondSpinner.setEnabled(true);
        });

        panel.add(transferLabel);
        panel.add(transferCheckBox);
        panel.add(currentTimeButton);
        panel.add(manualTimeButton);
        panel.add(timePanel);

        int result = JOptionPane.showConfirmDialog(null, panel, "Bus Route Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return new Object[]{transferCheckBox, currentTimeButton, hourSpinner, minuteSpinner, secondSpinner};
        }
        return null;
    }

    //process the data from the showBusRouteOptionsDialog using the boolean transfer value and the time that was given through the spinner objects
    private void processBusRouteOptions(JCheckBox transferCheckBox, JRadioButton currentTimeButton, JSpinner hourSpinner, JSpinner minuteSpinner, JSpinner secondSpinner) throws Exception {
        boolean allowTransfers = transferCheckBox.isSelected();
        String preferredTime;

        if (currentTimeButton.isSelected()) {
            preferredTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        } else {
            int hours = (int) hourSpinner.getValue();
            int minutes = (int) minuteSpinner.getValue();
            int seconds = (int) secondSpinner.getValue();
            preferredTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

        boolean accept = buttonClickSharedOperations(postCodeFromField, postCodeToField,  false);
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

    //get the arrival times for each stop in the transfer method
    public List<String> getTimes(List<AStarWithTime.PathNode> path, String endStopId) {
        String finalArrivalTime = null;
        List<String> times = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            AStarWithTime.PathNode node = path.get(i);
            String nextStopId = (i + 1 < path.size()) ? path.get(i + 1).previousStopId : endStopId;
            List<BusStop> segmentStops = GTFSLoader.getBusStopsForTrip(node.tripId, node.previousStopId, nextStopId);
            for (BusStop segmentStop : segmentStops) {
                times.add(segmentStop.getArrivalTime());
            }
            finalArrivalTime = node.arrivalTime;
        }
        Stop stop = GTFSLoader.getStopDetails(endStopId);
        times.add(finalArrivalTime);
        return times;

    }

    private void handleTransfers(PostAddress first, PostAddress last, String preferredTime) throws Exception {
        JourneyRouteResult result = RoutingApplication.findBestRoute(first.getPostalCode(), last.getPostalCode(), preferredTime);



        if (result == null) {
            noBusError();
            return;
        }

        List<AStarWithTime.PathNode> path = result.path;
        String endStopId = result.route.endStopId;

        if (path == null) {
            noBusError();
            return;
        }

        List<Stop> totalStops = new ArrayList<>();
        List<Integer> transferIndices = new ArrayList<>();
        RoutingApplication.printPathDetails(path, first.getPostalCode(), last.getPostalCode(), preferredTime);
        Stop startStop = createStopFromPostalCode(first);
        totalStops.add(startStop);
        String previousTripId = null;
        int index = 1;

        for (int i = 0; i < path.size(); i++) {
            AStarWithTime.PathNode node = path.get(i);
            Stop stop = GTFSLoader.getStopDetails(node.previousStopId);

            if (stop != null) {
                if (previousTripId != null && !previousTripId.equals(node.tripId)) {
                    transferIndices.add(totalStops.size() - 1);
                }
                totalStops.add(stop);
                previousTripId = node.tripId;
                index++;
            }
            String nextStopId = (i + 1 < path.size()) ? path.get(i + 1).previousStopId : endStopId;
            if (nextStopId != null) {
                List<BusStop> segmentStops = GTFSLoader.getBusStopsForTrip(node.tripId, node.previousStopId, nextStopId);
                for (BusStop segmentStop : segmentStops) {
                    Stop intermediateStop = new Stop(segmentStop.getStopName(), segmentStop.getStopName(), segmentStop.getStopLat(), segmentStop.getStopLon());
                    totalStops.add(intermediateStop);
                }
            }
        }


        Stop endStop = createStopFromPostalCode(last);
        totalStops.add(endStop);
        drawShortestPathOnMapBusRouteTransfer(totalStops, transferIndices);
        showBusStopsPopupTransfer(totalStops,transferIndices,result.travelTime, result.path, result.route.endStopId);
    }

    // summarizes the data from the Transfer route, by creating a new window that contains stop information, transfer position and the duration of the trip
    private void showBusStopsPopupTransfer(List<Stop> totalStops, List<Integer> transferIndices, int travelTime, List<AStarWithTime.PathNode> path, String endStopId) {
        if (totalStops == null || totalStops.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No stops available", "Bus Route", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder stopNames = new StringBuilder("Bus Stops with Transfers:\n");
        Set<String> uniqueNames = new HashSet<>();
        List<String> times = getTimes(path, endStopId);

        int index = 0;
        for (int i = 0; i < totalStops.size(); i++) {
            Stop stop = totalStops.get(i);
            String stopName = stop.stopName();



            boolean isTransfer = transferIndices.contains(i);


            if (!uniqueNames.contains(stopName)) {
                stopNames.append(stopName);
                if (i != 0 && i != totalStops.size() - 1) {
                    stopNames.append(" at: ").append(times.get(index).substring(0, 5));
                    index++;
                }
                uniqueNames.add(stopName);

                if (isTransfer) {
                    stopNames.append(" (Transfer)");
                }
                stopNames.append("\n");
            }
        }

        stopNames.append("\nTotal Travel Time: ").append(travelTime / 60).append(" minutes");

        JOptionPane.showMessageDialog(null, stopNames.toString(), "Bus Route", JOptionPane.INFORMATION_MESSAGE);
    }




    //draws the Bus route with transfer on the map
    public void drawShortestPathOnMapBusRouteTransfer(List<Stop> totalStops, List<Integer> transferIndices) {
        Graphics2D g = getMapGraphics();
        DrawBaseImage(g);
        g.setStroke(new BasicStroke(3));
        int circleSize = 6;
        int textPadding = 4;
        int offsetX = 20;
        Font font = new Font("Arial", Font.PLAIN, 12);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);

        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK};
        int colorIndex = 0;

        for (int i = 0; i < totalStops.size() - 1; i++) {
            Stop startStop = totalStops.get(i);
            Stop endStop = totalStops.get(i + 1);
            Point startPoint = findPostCodeCoordinate(startStop.stopLon(), startStop.stopLat());
            Point endPoint = findPostCodeCoordinate(endStop.stopLon(), endStop.stopLat());


            if (transferIndices.contains(i)) {
                colorIndex = (colorIndex + 1) % colors.length;
            }

            g.setColor(colors[colorIndex]);
            g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        }


        for (int i = 0; i < totalStops.size(); i++) {
            Stop stop = totalStops.get(i);
            Point point = findPostCodeCoordinate(stop.stopLon(), stop.stopLat());

            if (transferIndices.contains(i)) {
                g.setColor(Color.RED);
                g.fillOval(point.x - 5, point.y - 5, 10, 10);
            } else {
                g.setColor(Color.BLUE);
                g.fillOval(point.x - 3, point.y - 3, 6, 6);
            }
        }


        for (int i = 0; i < totalStops.size(); i++) {
            Stop stop = totalStops.get(i);
            Point point = findPostCodeCoordinate(stop.stopLon(), stop.stopLat());
            String stopName = stop.stopName().replace("Maastricht, ", "");


            int textWidth = metrics.stringWidth(stopName);
            int textHeight = metrics.getHeight();


            int textX = point.x - textWidth / 2 + offsetX;
            int textY = point.y - circleSize / 2 - textPadding;

            if (i % 2 == 0 || i == totalStops.size() - 1) {

                g.setColor(Color.WHITE);
                g.fillRect(textX - textPadding, textY - textHeight + textPadding / 2, textWidth + 2 * textPadding, textHeight);


                g.setColor(Color.BLACK);
                g.drawRect(textX - textPadding, textY - textHeight + textPadding / 2, textWidth + 2 * textPadding, textHeight);


                if (i == 0 || i == totalStops.size() - 1 || transferIndices.contains(i)) {
                    g.setColor(Color.RED);
                } else {
                    g.setColor(Color.BLACK);
                }


                g.drawString(stopName, textX, textY);
            }
        }
    }



    //translate PostAddress objects to Stop objects
    private Stop createStopFromPostalCode(PostAddress address) {
        return new Stop(address.getPostalCode(), address.getPostalCode(), address.getLat(), address.getLon());
    }
    //translates PostAddress object to BusStop Object
    private BusStop createBusStopFromPostalCode(PostAddress postalCode) {
        return new BusStop("0", 0, postalCode.getPostalCode(), null, null, (float) postalCode.getLat(), (float) postalCode.getLon(), null);
    }

    //Creates Bus Route and Calls the Drawing method for non transfer routes
    private void handleDirectBusRoute(PostAddress first, PostAddress last, String preferredTime) {
        BusRouteFinder finder = new BusRouteFinder(first, last);
        DirectRoute directRoute = finder.findShortestDirectBusRouteWithTime(preferredTime);
        if (directRoute == null) {
            noBusError();
            return;
        }
        directRoute.getBusStops().add(0, createBusStopFromPostalCode(first));
        directRoute.getBusStops().add(createBusStopFromPostalCode(last));
        drawShortestPathOnMapBusRoute(directRoute);
        showBusStopsPopup(directRoute);
    }




    private void noBusError(){
        JOptionPane.showMessageDialog(null, "There is no bus route that connects these two postal codes");
        busRouteButton.setSelected(false);
    }



    //summarizes the Bus Route without transfer by showing the line, bus stops that are passed though, duration and line number
    private void showBusStopsPopup(DirectRoute route) {
        String routeName = route.getBusStops().get(1).getRouteName();
        StringBuilder stopNames = new StringBuilder("Bus Stops for line (" + routeName + "):\n");
        for (BusStop busStop : route.getBusStops()) {
            if (busStop.getArrivalTime() == null) {
                stopNames.append(busStop.getStopName()).append("\n");
            } else {
                stopNames.append(busStop.getStopName()).append(" at : ").append(busStop.getArrivalTime()).append("\n");
            }
        }
        String firstArrivalTime = route.getBusStops().get(1).getArrivalTime();
        String lastDepartureTime = route.getBusStops().get(route.getBusStops().size() - 2).getDepartureTime();
        long totalTimeMinutes = calculateTimeDifference(firstArrivalTime, lastDepartureTime);

        stopNames.append("\nTotal Travel Time: ").append(totalTimeMinutes + 5).append(" minutes");

        JOptionPane.showMessageDialog(null, stopNames.toString(), "Bus Route", JOptionPane.INFORMATION_MESSAGE);
    }

    //calculates the duration of the bus Route
    private long calculateTimeDifference(String startTime, String endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime start = LocalTime.parse(startTime, formatter);
        LocalTime end = LocalTime.parse(endTime, formatter);
        return ChronoUnit.MINUTES.between(start, end);
    }

    //Draws the Bus Route without transfers to the map Image
    private void drawShortestPathOnMapBusRoute(DirectRoute route) {
        Graphics2D g = getMapGraphics();
        DrawBaseImage(g);

        g.setStroke(new BasicStroke(3));
        int circleSize = 6;
        int textPadding = 4;
        Font font = new Font("Arial", Font.PLAIN, 12);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);

        for (int i = 0; i < route.getBusStops().size() - 1; i++) {
            g.setColor(Color.RED);

            Point startPoint = findPostCodeCoordinate(route.getBusStops().get(i).getStopLon(), route.getBusStops().get(i).getStopLat());
            Point endPoint = findPostCodeCoordinate(route.getBusStops().get(i + 1).getStopLon(), route.getBusStops().get(i + 1).getStopLat());
            g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);

            g.setColor(Color.BLACK);
            g.fillOval(startPoint.x - circleSize / 2, startPoint.y - circleSize / 2, circleSize, circleSize);


            if (i % 2 == 0) {
                String stopName = route.getBusStops().get(i).getStopName().replaceFirst("^Maastricht, ", "");
                int textWidth = metrics.stringWidth(stopName);
                int textHeight = metrics.getHeight();
                int textX = startPoint.x - textWidth / 2 + 40;
                int textY = startPoint.y - circleSize / 2 - textPadding;


                g.setColor(Color.WHITE);
                g.fillRect(textX - textPadding, textY - textHeight + textPadding / 2, textWidth + 2 * textPadding, textHeight);


                g.setColor(Color.BLACK);
                g.drawRect(textX - textPadding, textY - textHeight + textPadding / 2, textWidth + 2 * textPadding, textHeight);
                g.setColor(Color.BLACK);

                if (i == 0){
                    g.setColor(Color.RED);
                }

                g.drawString(stopName, textX, textY);
            }
        }


        Point lastPoint = findPostCodeCoordinate(route.getBusStops().get(route.getBusStops().size() - 1).getStopLon(), route.getBusStops().get(route.getBusStops().size() - 1).getStopLat());
        g.fillOval(lastPoint.x - circleSize/2, lastPoint.y - circleSize/2, circleSize, circleSize);
        String lastStopName = route.getBusStops().get(route.getBusStops().size() - 1).getStopName().replaceFirst("^Maastricht, ", "");
        int lastTextWidth = metrics.stringWidth(lastStopName);
        int lastTextHeight = metrics.getHeight();
        int lastTextX = lastPoint.x - lastTextWidth / 2 + 40;
        int lastTextY = lastPoint.y - circleSize / 2 - textPadding;


        g.setColor(Color.WHITE);
        g.fillRect(lastTextX - textPadding, lastTextY - lastTextHeight + textPadding / 2, lastTextWidth + 2 * textPadding, lastTextHeight);


        g.setColor(Color.BLACK);
        g.drawRect(lastTextX - textPadding, lastTextY - lastTextHeight + textPadding / 2, lastTextWidth + 2 * textPadding, lastTextHeight);


        g.setColor(Color.RED);
        g.drawString(lastStopName, lastTextX, lastTextY);
    }



    //Creates Custom Input field
    private JFormattedTextField createPostCodeField(String contents, Point textPanelPosition) {
        JFormattedTextField postCodeField = new JFormattedTextField();
        postCodeField.setPreferredSize(new Dimension(60, 30));
        postCodeField.setText(contents);

        int xOffset = textPanelPosition.x + 100 + 20;
        int yOffset = textPanelPosition.y;
        postCodeField.setBounds(xOffset, yOffset, 80, 30);


        return postCodeField;
    }





    //Assembles the control panel by initializing all elements and adding them to a single panel
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
        postCodeFromField = createPostCodeField("6218BK", fromLabel.getLocation());
        controlPanel.add(postCodeFromField, gbc);


        gbc.gridy++;
        JLabel toLabel = new JLabel("To: ");
        controlPanel.add(toLabel, gbc);


        gbc.gridy++;
        postCodeToField = createPostCodeField("6229GV", toLabel.getLocation());
        controlPanel.add(postCodeToField, gbc);


        gbc.gridy++;
        footButton = createToggleButton("FOOT", "walk_hollow.png", "walk.png");
        controlPanel.add(footButton, gbc);


        gbc.gridy++;
        bikeButton = createToggleButton("BIKE", "bike_hollow.png", "bike.png");
        controlPanel.add(bikeButton, gbc);


        gbc.gridy++;
        carButton = createToggleButton("CAR", "car_hollow.png", "car.png");
        controlPanel.add(carButton, gbc);


        gbc.gridy++;
        busRouteButton = createToggleButton("BUS", "bus_hollow.png", "bus.png");
        controlPanel.add(busRouteButton, gbc);


        gbc.gridy++;
        gbc.gridwidth = 1;
        goButton = createAlgorithmButton("Straight Line");
        controlPanel.add(goButton, gbc);


        gbc.gridy++;
        algorithmButton = createAlgorithmButton("Shortest Route");
        controlPanel.add(algorithmButton, gbc);


        gbc.gridy++;
        accessibilityButton = createAlgorithmButton("Accessibility Score");
        controlPanel.add(accessibilityButton, gbc);


        return controlPanel;
    }

    //Create button that calls any algorithm
    private JButton createAlgorithmButton(String name){
        JButton algorithmButton = new JButton(name);
        algorithmButton.setPreferredSize(new Dimension(150, 30));
        algorithmButton.setBackground(Color.WHITE);
        algorithmButton.setForeground(Color.BLUE);
        return algorithmButton;
    }





    //Create button for any vehicle
    private JToggleButton createToggleButton(String text, String hollowIcon, String fillIcon) {
        String prefix = "data/img/icons/";
        int width = 20;
        int height = 20;
        ImageIcon hollow = new ImageIcon(new ImageIcon(prefix + hollowIcon).getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
        ImageIcon filled = new ImageIcon(new ImageIcon(prefix + fillIcon).getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
        JToggleButton button = new JToggleButton(text, hollow);
        button.setSelectedIcon(filled);
        button.setPreferredSize(new Dimension(width + 100, height));
        button.addActionListener(e -> {
            untoggleAllButtons();
            button.setSelected(true);
            SelectedVehicle = text;
        });
        return button;
    }




    //Controls the Input, which is the same operation for all buttons since we care about validity
    private boolean buttonClickSharedOperations(JFormattedTextField postCodeFromField, JFormattedTextField postCodeToField,boolean careForVehicle){
        DrawBaseImage(getMapGraphics());
        boolean accept = true;
        repaint();

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


    //Checks for illegal postal codes and gives the proper error messages
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



    private void runPathFindingAlgorithm(PostAddress from, PostAddress to){
        GraphHopperUtil graphHopperUtil = new GraphHopperUtil();
        QueryResponse queryResponse = graphHopperUtil.calculateRoute(from.getPostalCode(), to.getPostalCode(), SelectedVehicle.toLowerCase());
        System.out.println(SelectedVehicle.toLowerCase());

        ArrayList<PostAddress> shortestPath = queryResponse.path();
        drawShortestPathOnMap(shortestPath);

        double distance = queryResponse.distance() / 1000;
        long time = queryResponse.time();

        distance = Double.parseDouble(new DecimalFormat("##.##").format(distance));
        showAlgorithmDistanceMessage(time, distance);
    }

    //shows simple distance and time for the straight distance and non bus route pathfinding algorithm
    private void showAlgorithmDistanceMessage(long time, double distance) {
        int timeInSeconds = (int) (time / 1000);
        int hours = (timeInSeconds / 3600);
        int minutes = ((timeInSeconds % 3600) / 60);
        int seconds = (timeInSeconds % 60);

        String distanceMessage = String.format("Distance : %.2f km\nTime : ", distance);

        String timeMessage = "Not Applicable";
        if (distance != 0) {
            String hoursMessage = hours != 0 ? hours + " hours " : "";
            String minutesMessage = minutes != 0 ? minutes + " minutes " : "";
            String secondsMessage = seconds != 0 ? seconds + " seconds" : "";

            timeMessage = hoursMessage + minutesMessage + secondsMessage;
        }
        JOptionPane.showMessageDialog(null, distanceMessage + timeMessage + "\n");
    }



    //Visualization for Shortest Path Algorithm
    private void drawShortestPathOnMap(ArrayList<PostAddress> shortestPath) {
        Graphics2D g = getMapGraphics();
        DrawBaseImage(g);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(3));

        for (int i = 0; i < shortestPath.size() - 1; i++) {
            Point startPoint = findPostCodeCoordinate(shortestPath.get(i).getLon(), shortestPath.get(i).getLat());
            Point endPoint = findPostCodeCoordinate(shortestPath.get(i + 1).getLon(), shortestPath.get(i + 1).getLat());
            g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        }
    }

    //gets the Map Graphics from the Map Image
    private Graphics2D getMapGraphics(){
        return (Graphics2D) mapImage.getGraphics();
    }

    //Clears previous drawings on the map Image
    private  void DrawBaseImage(Graphics2D g) {
        g.drawImage(clearMapImage, 0, 0, null);
    }

    //Takes a String and returns the corresponding PostAddress
    public static PostAddress getAddressFromDataManager(String postalCode) {
        try {
            return AddressFinder.getAddress(postalCode);
        } catch (Exception e) {
            System.err.println("Error getting address from postal code: " + postalCode);
            return null;
        }
    }

    //decline postal code if not 6 characters
    public boolean acceptCode(String code) {
        return code.length() == 6;
    }

    //maps the coordinates onto the image by transforming them into relative pixel positions
    public  Point findPostCodeCoordinate(double lon, double lat) {
        int imageWidth = mapImage.getWidth();
        int imageHeight = mapImage.getHeight();
        double lonPercent = (lon - minLon) / (maxLon - minLon);
        double latPercent = (lat - minLat) / (maxLat - minLat);
        int xPixel = (int) (lonPercent * imageWidth);
        int yPixel = (int) (latPercent * imageHeight);


        return new Point(xPixel, yPixel);
    }

}