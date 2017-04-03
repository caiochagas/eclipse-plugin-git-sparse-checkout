package views;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import resources.Messages;
import resources.Utils;

public class MainView extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField textFieldProjectPath;
	private JButton btnSave;
	private JTextArea textAreaContent;
	private JLabel lblRefreshing;
	
	public File projectDir;
	public File sparseCheckoutFile;
	public File storageFile;
	
	public static final String STORAGE_DATA_PATH = System.getenv("APPDATA") + "/eclipse-plugin-git-sparse-checkout";
	private JScrollPane scrollPaneContent;


	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainView frame = new MainView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void checkStorageData() {
		try {
			storageFile = Utils.getFile(STORAGE_DATA_PATH);
			String projectDirPath = Utils.getFileContent(storageFile);
			if(!"".equals(projectDirPath)) { 
				projectDir = new File(projectDirPath);
				fillFields();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setStorageData(String projectDirPath) {
		try {
			FileWriter fw = new FileWriter(storageFile);
			fw.write(projectDirPath);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void fillFields() {
		if(Utils.isValidGitProject(projectDir)) {
			Utils.setConfigSparseCheckout(true, projectDir);
			String projectDirPath = projectDir.getAbsolutePath();
			setStorageData(projectDirPath);
			textFieldProjectPath.setText(projectDirPath);
			
			try {
				sparseCheckoutFile = Utils.getFile(projectDirPath + "/.git/info/sparse-checkout"); 
				String content = Utils.getFileContent(sparseCheckoutFile);
				textAreaContent.setText(content);
			} catch (IOException e) {
				System.err.println("Can't access sparse checkout content"); 
				e.printStackTrace();
			}
		} else {
			System.err.println("Invalid git project was selected!"); 
			JOptionPane.showMessageDialog(MainView.this, Messages.getString("MSG_INVALID_GIT_PROJECT"), Messages.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public MainView() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(MainView.class.getResource("/assets/icon.png")));
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle("git sparse-checkout"); 
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 600, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textFieldProjectPath = new JTextField();
		textFieldProjectPath.setEnabled(false);
		textFieldProjectPath.setBounds(10, 31, 460, 25);
		contentPane.add(textFieldProjectPath);
		textFieldProjectPath.setColumns(10);
		
		JButton btnSearch = new JButton(Messages.getString("BTN_SEARCH")); 
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int response = fileChooser.showOpenDialog(MainView.this);
				if(response == JFileChooser.APPROVE_OPTION) {
					projectDir = fileChooser.getSelectedFile();
					fillFields();
				}
			}
		});
		btnSearch.setBounds(480, 30, 104, 26);
		contentPane.add(btnSearch);
		
		scrollPaneContent = new JScrollPane();
		scrollPaneContent.setBounds(10, 67, 574, 459);
		contentPane.add(scrollPaneContent);
		
		textAreaContent = new JTextArea();
		scrollPaneContent.setViewportView(textAreaContent);
		textAreaContent.setLineWrap(true);
		
		btnSave = new JButton(Messages.getString("BTN_SAVE")); 
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(sparseCheckoutFile == null) {
					JOptionPane.showMessageDialog(MainView.this, Messages.getString("MSG_SELECT_PROJECT_FIRST"), Messages.getString("ATTENTION"), JOptionPane.WARNING_MESSAGE);  //$NON-NLS-2$
					return;
				}
				lblRefreshing.setVisible(true);

				new java.util.Timer().schedule( 
				        new java.util.TimerTask() {
				            @Override
				            public void run() {
				            	try {
									MainView.this.revalidate();
									MainView.this.repaint();
									FileWriter fw = new FileWriter(sparseCheckoutFile.getAbsoluteFile());
									textAreaContent.write(fw);
									
									Process process = Runtime.getRuntime().exec("git read-tree -m -u HEAD", null, projectDir);
									String output = Utils.getInputAsString(process.getInputStream());
									String outputError = Utils.getInputAsString(process.getErrorStream());
									
									if("".equals(output) && "".equals(outputError)) {
										int input = JOptionPane.showOptionDialog(MainView.this, Messages.getString("MSG_SUCCESS"), Messages.getString("SUCCESS"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
									} else {
										throw new Exception(output + " " + outputError);
									}
								} catch (Exception e) {
									JOptionPane.showMessageDialog(MainView.this, e.getMessage(), Messages.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
									e.printStackTrace();
								} finally {
									lblRefreshing.setVisible(false);
								}
				            }
				        }, 10 
				);
				
				
			}
		});
		btnSave.setBounds(480, 537, 104, 26);
		contentPane.add(btnSave);
		
		JLabel lblDirectory = new JLabel(Messages.getString("LABEL_SELECT_DIR")); 
		lblDirectory.setFont(new Font("Tahoma", Font.PLAIN, 12)); 
		lblDirectory.setBounds(10, 11, 188, 14);
		contentPane.add(lblDirectory);
		
		lblRefreshing = new JLabel(Messages.getString("LABEL_REFRESHING"));
		lblRefreshing.setBounds(10, 537, 188, 14);
		lblRefreshing.setVisible(false);
		contentPane.add(lblRefreshing);
		setLocationRelativeTo(null);
		
		JScrollBar vertical = scrollPaneContent.getVerticalScrollBar();
		vertical.setValue( vertical.getMaximum() );
		
		checkStorageData();
	}
}
