import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class GUI extends JFrame {

    public static int currentVehicleCode = -1;
    public static String FromCode;
    public static String ToCode;


    public GUI() {
        setSize(500, 500);
        setResizable(false);
        setTitle("Map");
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel controlPanel = new JPanel(); // Panel for controls
        controlPanel.setLayout(null);

        JFormattedTextField postCodeFromField = new JFormattedTextField();
        postCodeFromField.setBounds(20, 20, 100, 35);
        JFormattedTextField postCodeToField = new JFormattedTextField();
        postCodeToField.setBounds(140, 20, 100, 35);
        postCodeToField.setText("TO");
        postCodeFromField.setText("FROM");

        String[] vehicleList = {"Walking", "Bicycle", "Car", "Bus", "Train"};
        JComboBox<String> vehicleBox = new JComboBox<>(vehicleList);
        vehicleBox.setBounds(260, 20, 100, 35);
        vehicleBox.addActionListener(e -> {
            String selectedVehicle = (String) vehicleBox.getSelectedItem();
            encodeVehicle(selectedVehicle);
            ToCode = postCodeToField.getText();
            FromCode = postCodeFromField.getText();
            System.out.println("Selected vehicle: " + selectedVehicle);

            try {
                writeDataToFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        controlPanel.add(vehicleBox);
        controlPanel.add(postCodeToField);
        controlPanel.add(postCodeFromField);
        controlPanel.setSize(400, 80);
        controlPanel.setLocation(20, 20);

        JPanel imagePanel = new ImageDisplay(); // Panel for displaying the image
        imagePanel.setSize(300, 300);
        imagePanel.setLocation(100, 150);

        add(controlPanel);
        add(imagePanel);

        // Add ActionListener to postCodeFromField
        postCodeFromField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FromCode = postCodeFromField.getText();
                ToCode = postCodeToField.getText();
                String selectedVehicle = (String) vehicleBox.getSelectedItem();
                encodeVehicle(selectedVehicle);

                try {
                    writeDataToFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Add ActionListener to postCodeToField
        postCodeToField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ToCode = postCodeToField.getText();
                FromCode = postCodeFromField.getText();
                String selectedVehicle = (String) vehicleBox.getSelectedItem();
                encodeVehicle(selectedVehicle);
                try {
                    writeDataToFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI frame = new GUI();
            frame.setVisible(true);
        });
    }

    public static void encodeVehicle(String vehicle) {
        switch (vehicle) {
            case "Walking":
                currentVehicleCode = 0;
                break;
            case "Bicycle":
                currentVehicleCode = 1;
                break;
            case "Car":
                currentVehicleCode = 2;
                break;
            case "Bus":
                currentVehicleCode = 3;
                break;
            default:
                System.out.println("ERROR");
                break;
        }
    }

    public static void writeDataToFile() throws IOException {
        File file = new File("GUIData.txt");
        FileWriter writer = new FileWriter(file);
        writer.write(currentVehicleCode + "\n" + FromCode + "\n" + ToCode);
        writer.flush();
        writer.close();

    }
    public static String readGUIData(String returnType) throws IOException {
        File file = new File("GUIData.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        currentVehicleCode = Integer.parseInt(reader.readLine());
        FromCode = reader.readLine();
        ToCode = reader.readLine();
        reader.close(); // don't forget to close the reader
        switch (returnType){
            case "TO":
                return ToCode;

            case "FROM":
                return FromCode;

            case "VEHICLE":
                return String.valueOf(currentVehicleCode);
        }
        return "ERROR";
    }


}
class ImageDisplay extends JPanel {
    private Image image;

    public ImageDisplay() {
        try {
            // Load the image from resources
            image = ImageIO.read(GUI.class.getResource("/Map.png"));
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception gracefully (e.g., display an error message)
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (image != null) {
            return new Dimension(image.getWidth(this), image.getHeight(this));
        } else {
            return super.getPreferredSize();
        }
    }
}
