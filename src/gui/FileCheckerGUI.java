package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.format.DateTimeFormatter;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicArrowButton;

import hashing.*;
import db.MaliciousDB;
import report.HistoryManager;
import main.IntegrityChecker;
import model.FileIntegrityRecord;

public class FileCheckerGUI {

    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> hashSelector;
    private JTextField searchField;
    private JLabel fileLabel;
    private List<File> selectedFiles = new ArrayList<>();
    private HistoryManager history;
    private MaliciousDB db;
    private DateTimeFormatter dtFormatter;
    private java.util.List<String> filePaths = new ArrayList<>();

    public FileCheckerGUI() throws Exception {
        history = new HistoryManager();
        db = new MaliciousDB();
        dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Main frame
        frame = new JFrame("Secure File Integrity Checker");
        frame.setSize(1200, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(15, 17, 26));

        // Header panel
        JPanel header = new JPanel();
        header.setBackground(new Color(17, 24, 39));
        header.setPreferredSize(new Dimension(frame.getWidth(), 110));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(18, 32, 18, 32));

        JLabel title = new JLabel("Secure File Integrity Checker", SwingConstants.CENTER);
        title.setForeground(new Color(243, 244, 246));
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 30));

        JLabel subtitle = new JLabel("Verify file integrity and detect malicious changes with confidence");
        subtitle.setForeground(new Color(156, 163, 175));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        ((JLabel)title).setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.CENTER);
        frame.add(header, BorderLayout.NORTH);

        // Right side buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(15,17,26));
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.setBorder(new EmptyBorder(20,20,20,20));

        JPanel buttonInner = new JPanel();
        buttonInner.setOpaque(false);
        buttonInner.setLayout(new GridLayout(4,1,10,10));

        // Professional single accent color
        Color accent = new Color(59, 130, 246); // calm blue

        JButton browseBtn = createButton("Browse Files", accent, accent.darker());
        browseBtn.addActionListener(e -> browseFiles());

        JButton computeBtn = createButton("Compute Hash", accent, accent.darker());
        computeBtn.addActionListener(e -> computeHashes());

        JButton exportBtn = createButton("Export History", accent, accent.darker());
        exportBtn.addActionListener(e -> exportHistory());

        buttonInner.add(browseBtn);
        buttonInner.add(computeBtn);
        buttonInner.add(exportBtn);

        JButton clearBtn = createButton("Clear Table", accent, accent.darker());
        clearBtn.addActionListener(e -> {
            tableModel.setRowCount(0);
            filePaths.clear();
        });
        buttonInner.add(clearBtn);

        JLabel actionsLabel = new JLabel("Actions");
        actionsLabel.setForeground(new Color(200, 205, 215));
        actionsLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));

        JPanel actionsHeader = new JPanel(new BorderLayout());
        actionsHeader.setOpaque(false);
        actionsHeader.add(actionsLabel, BorderLayout.WEST);
        actionsHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        buttonPanel.add(actionsHeader, BorderLayout.NORTH);
        buttonPanel.add(buttonInner, BorderLayout.CENTER);

        frame.add(buttonPanel, BorderLayout.EAST);

        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(16,16,16,16));
        centerPanel.setBackground(new Color(15,17,26));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        topPanel.setBackground(new Color(24, 26, 39));
        topPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        fileLabel = new JLabel("No file selected");
        fileLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        fileLabel.setForeground(new Color(209, 213, 219));
        topPanel.add(fileLabel);

        // Hash selection combo box
        String[] algorithms = {"MD5", "SHA-1", "SHA-256", "SHA-512"};
        Color inputBg = new Color(15, 23, 42);
        hashSelector = new JComboBox<>(algorithms);
        hashSelector.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hashSelector.setForeground(Color.WHITE);
        hashSelector.setBackground(inputBg);
        hashSelector.setOpaque(true);
        hashSelector.setFocusable(false);
        hashSelector.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));
        hashSelector.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                BasicArrowButton button = new BasicArrowButton(
                        BasicArrowButton.SOUTH,
                        inputBg,
                        inputBg.darker(),
                        Color.WHITE,
                        inputBg
                );
                button.setBorder(BorderFactory.createEmptyBorder());
                return button;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // Ensure the whole left part (selected value area) uses the dark input background
                g.setColor(inputBg);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
        hashSelector.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

                // Background and text colors aligned with search field
                Color bgNormal = inputBg;
                Color fgNormal = Color.WHITE;

                // index == -1 means the "selected" value shown in the combo box
                if (index == -1) {
                    lbl.setBackground(bgNormal);
                    lbl.setForeground(fgNormal);
                } else if (isSelected) {
                    lbl.setBackground(new Color(59, 130, 246)); // accent
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(bgNormal);
                    lbl.setForeground(fgNormal);
                }

                if (list != null) {
                    list.setSelectionBackground(new Color(59, 130, 246));
                    list.setSelectionForeground(Color.WHITE);
                    list.setBackground(bgNormal);
                    list.setForeground(fgNormal);
                }
                lbl.setOpaque(true);
                return lbl;
            }
        });
        topPanel.add(hashSelector);

        // Search field for filtering results
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(new Color(156, 163, 175));
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topPanel.add(Box.createHorizontalStrut(15));
        topPanel.add(searchLabel);

        searchField = new JTextField(16);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setForeground(new Color(229, 231, 235));
        searchField.setBackground(new Color(15, 23, 42));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81)),
                new EmptyBorder(2, 6, 2, 6)
        ));
        topPanel.add(searchField);

        centerPanel.add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(){
            public boolean isCellEditable(int row,int column){return false;}
        };
        tableModel.setColumnIdentifiers(new String[]{"File Name","Algorithm","Hash Value","Malicious","Integrity Status","Scan Time"});

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setBackground(new Color(17, 24, 39));
        table.setForeground(new Color(243,244,246));
        table.setSelectionBackground(accent);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column){
                Component c = super.getTableCellRendererComponent(tbl,value,isSelected,hasFocus,row,column);
                if(column==3){ // Malicious column
                    String status = value.toString();
                    if(status.equalsIgnoreCase("Yes")){
                        c.setForeground(new Color(229,57,53));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(new Color(67,160,71));
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                } else if(column==4){ // Integrity Status column
                    String status = value.toString();
                    if(status.equalsIgnoreCase("Modified")){
                        c.setForeground(new Color(229,57,53));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if(status.equalsIgnoreCase("Unchanged")){
                        c.setForeground(new Color(67,160,71));
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    } else { // New
                        c.setForeground(new Color(255,193,7));
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                } else c.setForeground(new Color(243,244,246));
                c.setBackground(row%2==0?new Color(24,31,49):new Color(17,24,39));
                return c;
            }
        });

        // Table sorter and search filter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateFilter() {
                String text = searchField.getText();
                if (text == null || text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { updateFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { updateFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { updateFilter(); }
        });

        // Table header styling
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        tableHeader.setOpaque(true);
        tableHeader.setBackground(new Color(31, 41, 55));
        tableHeader.setForeground(new Color(209, 213, 219));

        tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
                lbl.setForeground(new Color(209, 213, 219));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(new Color(31, 41, 55));
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(15,17,26));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(scroll, BorderLayout.CENTER);

        // Drag-and-drop overlay
        JPanel dragOverlay = new JPanel();
        dragOverlay.setBackground(new Color(4, 10, 25, 120));
        dragOverlay.setLayout(new GridBagLayout());
        JLabel dragLabel = new JLabel("Drop files here");
        dragLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 26));
        dragLabel.setForeground(new Color(236, 240, 241));
        dragOverlay.add(dragLabel);
        dragOverlay.setVisible(false);
        frame.setGlassPane(dragOverlay);

        // Context menu for table
        JPopupMenu tableMenu = new JPopupMenu();
        JMenuItem copyHashItem = new JMenuItem("Copy hash value");
        copyHashItem.addActionListener(e -> copySelectedHash());
        JMenuItem openLocationItem = new JMenuItem("Open file location");
        openLocationItem.addActionListener(e -> openSelectedFileLocation());
        tableMenu.add(copyHashItem);
        tableMenu.add(openLocationItem);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            private void showMenu(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0 && row < table.getRowCount()) {
                    table.setRowSelectionInterval(row, row);
                }
                tableMenu.show(e.getComponent(), e.getX(), e.getY());
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) showMenu(e);
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) showMenu(e);
            }
        });

        new DropTarget(frame, new DropTargetListener() {
            @Override public void dragEnter(DropTargetDragEvent dtde){dragOverlay.setVisible(true);}
            @Override public void dragOver(DropTargetDragEvent dtde){}
            @Override public void dropActionChanged(DropTargetDragEvent dtde){}
            @Override public void dragExit(DropTargetEvent dte){dragOverlay.setVisible(false);}
            @Override
            public void drop(DropTargetDropEvent dtde){
                try{
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    selectedFiles.clear();
                    selectedFiles.addAll(droppedFiles);
                    fileLabel.setText(selectedFiles.size()+" file(s) selected");
                    dragOverlay.setVisible(false);
                } catch(Exception ex){ex.printStackTrace();}
            }
        });
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JButton createButton(String text, Color colorStart, Color colorEnd){
        JButton btn = new JButton(text){
            @Override
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colorStart);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(10,24,10,24));
        btn.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseEntered(java.awt.event.MouseEvent e){btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
            public void mouseExited(java.awt.event.MouseEvent e){btn.setCursor(Cursor.getDefaultCursor());}
        });
        return btn;
    }

    private void browseFiles(){
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle("Select File(s) to Check Integrity");
        int option = chooser.showOpenDialog(frame);
        if(option==JFileChooser.APPROVE_OPTION){
            selectedFiles.clear();
            selectedFiles.addAll(Arrays.asList(chooser.getSelectedFiles()));
            fileLabel.setText(selectedFiles.size()+" file(s) selected");
        }
    }

    private void computeHashes(){
        if(selectedFiles.isEmpty()){
            JOptionPane.showMessageDialog(frame,"Please select at least one file.","No File Selected",JOptionPane.WARNING_MESSAGE);
            return;
        }

        String algorithm = (String) hashSelector.getSelectedItem();
        HashAlgorithm hasher;
        if ("MD5".equals(algorithm)) {
            hasher = new MD5Hasher();
        } else if ("SHA-1".equals(algorithm)) {
            hasher = new SHA1Hasher();
        } else if ("SHA-256".equals(algorithm)) {
            hasher = new SHA256Hasher();
        } else if ("SHA-512".equals(algorithm)) {
            hasher = new SHA512Hasher();
        } else {
            hasher = new MD5Hasher();
        }

        // Progress dialog
        JDialog progressDialog = new JDialog(frame, "Computing hashes…", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorder(new EmptyBorder(10, 10, 10, 10));
        progressBar.setForeground(new Color(59, 130, 246));
        progressDialog.getContentPane().setLayout(new BorderLayout());
        progressDialog.getContentPane().add(new JLabel("  Please wait while hashes are computed..."), BorderLayout.NORTH);
        progressDialog.getContentPane().add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(320, 100);
        progressDialog.setLocationRelativeTo(frame);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private final java.util.List<Object[]> rows = new ArrayList<>();
            private final java.util.List<String> paths = new ArrayList<>();

            @Override
            protected Void doInBackground() throws Exception {
                IntegrityChecker checker = new IntegrityChecker(hasher, history, db);
                for(File f : selectedFiles){
                    FileIntegrityRecord record = checker.checkFile(f.getAbsolutePath());
                    rows.add(new Object[]{
                            record.getFileName(),
                            record.getHashAlgorithm(),
                            record.getHashValue(),
                            record.isMalicious() ? "Yes" : "No",
                            record.getIntegrityStatus(),
                            record.getScanTime().format(dtFormatter)
                    });
                    paths.add(f.getAbsolutePath());
                }
                return null;
            }

            @Override
            protected void done() {
                try{
                    for (int i = 0; i < rows.size(); i++) {
                        tableModel.addRow(rows.get(i));
                        filePaths.add(paths.get(i));
                    }
                } catch(Exception e){
                    JOptionPane.showMessageDialog(frame,"Error: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressDialog.dispose();
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    private void exportHistory(){
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Scan History");
        int option = chooser.showSaveDialog(frame);
        if(option==JFileChooser.APPROVE_OPTION){
            try{
                history.exportHistory(chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(frame,"History exported successfully!");
            } catch(Exception e){
                JOptionPane.showMessageDialog(frame,"Error exporting history: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args){
        try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception ignored){}
        SwingUtilities.invokeLater(()->{
            try{new FileCheckerGUI();} catch(Exception e){e.printStackTrace();}
        });
    }

    // --- Context menu helpers ---
    private void copySelectedHash() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object value = tableModel.getValueAt(modelRow, 2); // Hash Value column
        if (value != null) {
            StringSelection selection = new StringSelection(value.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }
    }

    private void openSelectedFileLocation() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= filePaths.size()) return;

        String path = filePaths.get(modelRow);
        if (path == null) return;
        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null && parent.exists()) {
                Desktop.getDesktop().open(parent);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,"Unable to open file location: "+e.getMessage(),
                    "Error",JOptionPane.ERROR_MESSAGE);
        }
    }
}
