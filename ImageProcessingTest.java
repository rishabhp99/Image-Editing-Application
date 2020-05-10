import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.RescaleOp;
import java.awt.image.ShortLookupTable;
import java.io.File;
import java.io.IOException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageProcessingTest {
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new ImageProcessingFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
}

class ImageProcessingFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public ImageProcessingFrame() {
		setTitle("ImageProcessingTest");
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

		add(new JComponent() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
				if (image != null)
					g.drawImage(image, 0, 0, null);
			}
		});

		createToolBar();


		
		JMenu fileMenu = new JMenu("File");
		
		JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				openFile();
			}
		});
		fileMenu.add(openItem);

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
		fileMenu.add(exitItem);

		JMenu editMenu = new JMenu("Edit");
		JMenuItem blurItem = new JMenuItem("Blur");
		blurItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				float weight = 1.0f / 9.0f;
				float[] elements = new float[9];
				for (int i = 0; i < 9; i++)
					elements[i] = weight;
				convolve(elements);
			}
		});
		editMenu.add(blurItem);

		JMenuItem sharpenItem = new JMenuItem("Sharpen");
		sharpenItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				float[] elements = { 0.0f, -1.0f, 0.0f, -1.0f, 5.f, -1.0f, 0.0f, -1.0f, 0.0f };
				convolve(elements);
			}
		});
		editMenu.add(sharpenItem);

		JMenuItem brightenItem = new JMenuItem("Brighten");
		brightenItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				float a = 1.1f;
				// float b = 20.0f;
				float b = 0;
				RescaleOp op = new RescaleOp(a, b, null);
				filter(op);
			}
		});
		editMenu.add(brightenItem);

		JMenuItem edgeDetectItem = new JMenuItem("Edge detect");
		edgeDetectItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				float[] elements = { 0.0f, -1.0f, 0.0f, -1.0f, 4.f, -1.0f, 0.0f, -1.0f, 0.0f };
				convolve(elements);
			}
		});
		editMenu.add(edgeDetectItem);

		JMenuItem negativeItem = new JMenuItem("Negative");
		negativeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				short[] negative = new short[256 * 1];
				for (int i = 0; i < 256; i++)
					negative[i] = (short) (255 - i);
				ShortLookupTable table = new ShortLookupTable(0, negative);
				LookupOp op = new LookupOp(table, null);
				filter(op);
			}
		});
		editMenu.add(negativeItem);

		JMenuItem rotateItem = new JMenuItem("Rotate");
		rotateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (image == null)
					return;
				AffineTransform transform = AffineTransform.getRotateInstance(Math.toRadians(5), image.getWidth() / 2,
						image.getHeight() / 2);
				AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
				filter(op);
			}
		});
		editMenu.add(rotateItem);

		JMenuItem compressItem = new JMenuItem("Compress");
		compressItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
					if (image == null)
					return;
					try{
					File output = new File("new.jpeg");
					OutputStream out = new FileOutputStream(output);
					ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
					ImageOutputStream ios = ImageIO.createImageOutputStream(out);
					writer.setOutput(ios);

					ImageWriteParam param = writer.getDefaultWriteParam();
				     
				        if (param.canWriteCompressed()){
				            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				            param.setCompressionQuality(0.05f);
				        }

				        writer.write(null, new IIOImage(image, null, null), param);
		
				        out.close();
				        ios.close();
				        writer.dispose();
				    } catch (Exception e1){
				    	e1.printStackTrace();
				    }



				
			}
		});
		editMenu.add(compressItem);

		JMenuItem cropItem = new JMenuItem("Crop");
		cropItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (image == null)
					return;
				AffineTransform transform = AffineTransform.getScaleInstance(0.50, 0.50);
				AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
				filter(op);
			}
		});
		editMenu.add(cropItem);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		setJMenuBar(menuBar);
	}

	public void openFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		String[] extensions = ImageIO.getReaderFileSuffixes();
		chooser.setFileFilter(new FileNameExtensionFilter("Image files", extensions));
		int r = chooser.showOpenDialog(this);
		if (r != JFileChooser.APPROVE_OPTION)
			return;

		try {
			Image img = ImageIO.read(chooser.getSelectedFile());
			image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
			image.getGraphics().drawImage(img, 0, 0, null);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, e);
		}
		repaint();
	}

	private void filter(BufferedImageOp op) {
		if (image == null)
			return;
		image = op.filter(image, null);
		repaint();
	}

	private void convolve(float[] elements) {
		Kernel kernel = new Kernel(3, 3, elements);
		ConvolveOp op = new ConvolveOp(kernel);
		filter(op);
	}

	private BufferedImage image;
	private static final int DEFAULT_WIDTH = 400;
	private static final int DEFAULT_HEIGHT = 400;

	private void createToolBar(){

		JToolBar toolbar = new JToolBar();

		var openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				openFile();
			}
		});
		toolbar.add(openButton);

		var exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
		toolbar.add(exitButton);

		var blurButton = new JButton("Blur");
		blurButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				float weight = 1.0f / 9.0f;
				float[] elements = new float[9];
				for (int i = 0; i < 9; i++)
					elements[i] = weight;
				convolve(elements);
			}
		});
		toolbar.add(blurButton);


		var sharpenButton = new JButton("Sharpen");
		sharpenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				float[] elements = { 0.0f, -1.0f, 0.0f, -1.0f, 5.f, -1.0f, 0.0f, -1.0f, 0.0f };
				convolve(elements);
			}
		});
		toolbar.add(sharpenButton);


		var brightenButton = new JButton("Brighten");
		brightenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				float a = 1.1f;
				// float b = 20.0f;
				float b = 0;
				RescaleOp op = new RescaleOp(a, b, null);
				filter(op);
			}
		});
		toolbar.add(brightenButton);

		var edgeDetectButton = new JButton("Edge Detect");
		edgeDetectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				float[] elements = { 0.0f, -1.0f, 0.0f, -1.0f, 4.f, -1.0f, 0.0f, -1.0f, 0.0f };
				convolve(elements);
			}
		});
		toolbar.add(edgeDetectButton);

		var negativeButton = new JButton("Negative");
		negativeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				short[] negative = new short[256 * 1];
				for (int i = 0; i < 256; i++)
					negative[i] = (short) (255 - i);
				ShortLookupTable table = new ShortLookupTable(0, negative);
				LookupOp op = new LookupOp(table, null);
				filter(op);
			}
		});
		toolbar.add(negativeButton);

		var rotateButton = new JButton("Rotate");
		rotateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (image == null)
					return;
				AffineTransform transform = AffineTransform.getRotateInstance(Math.toRadians(5), image.getWidth() / 2,
						image.getHeight() / 2);
				AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
				filter(op);
			}
		});
		toolbar.add(rotateButton);

		var compressButton = new JButton("Compress");
		compressButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
					if (image == null)
					return;
					try{
					File output = new File("new.jpeg");
					OutputStream out = new FileOutputStream(output);
					ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
					ImageOutputStream ios = ImageIO.createImageOutputStream(out);
					writer.setOutput(ios);

					ImageWriteParam param = writer.getDefaultWriteParam();
				     
				        if (param.canWriteCompressed()){
				            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				            param.setCompressionQuality(0.05f);
				        }

				        writer.write(null, new IIOImage(image, null, null), param);
		
				        out.close();
				        ios.close();
				        writer.dispose();
				    } catch (Exception e1){
				    	e1.printStackTrace();
				    }



				
			}
		});		toolbar.add(compressButton);

		var cropButton =  new JButton("Crop");
		cropButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (image == null)
					return;
				AffineTransform transform = AffineTransform.getScaleInstance(0.50, 0.50);
				AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
				filter(op);
			}
		});
		toolbar.add(cropButton);

		add(toolbar, BorderLayout.NORTH);

	}
}
