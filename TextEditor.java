import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class TextEditor extends JFrame {
    private JTextPane textPane;
    private JLabel statusLabel;
    private JLabel lineLabel;
    private JFileChooser fileChooser;
    private File currentFile;
    private UndoManager undoManager;

    private AttributeSet defaultStyle;
    private AttributeSet keywordStyle;
    private AttributeSet commentStyle;
    private AttributeSet stringStyle;

    private StyledDocument doc;

    public TextEditor() {
        setTitle("Text Editor by Jafar Lone");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.addActionListener(e -> newFile());
        fileMenu.add(newMenuItem);

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(e -> openFile());
        fileMenu.add(openMenuItem);

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(e -> saveFile());
        fileMenu.add(saveMenuItem);

        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        saveAsMenuItem.addActionListener(e -> saveAsFile());
        fileMenu.add(saveAsMenuItem);

        fileMenu.addSeparator();

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> exit());
        fileMenu.add(exitMenuItem);

        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);

        JMenuItem cutMenuItem = new JMenuItem("Cut");
        cutMenuItem.addActionListener(e -> textPane.cut());
        editMenu.add(cutMenuItem);

        JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.addActionListener(e -> textPane.copy());
        editMenu.add(copyMenuItem);

        JMenuItem pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.addActionListener(e -> textPane.paste());
        editMenu.add(pasteMenuItem);

        editMenu.addSeparator();

        JMenuItem findMenuItem = new JMenuItem("Find");
        findMenuItem.addActionListener(e -> findText());
        editMenu.add(findMenuItem);

        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);

        JMenuItem colorMenuItem = new JMenuItem("Select Text Color");
        colorMenuItem.addActionListener(e -> selectTextColor());
        viewMenu.add(colorMenuItem);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        getContentPane().add(toolBar, BorderLayout.NORTH);

        JComboBox<String> fontComboBox = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        fontComboBox.addActionListener(e -> setFontFamily((String) fontComboBox.getSelectedItem()));
        toolBar.add(fontComboBox);

        JComboBox<Integer> fontSizeComboBox = new JComboBox<>(new Integer[]{8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32});
        fontSizeComboBox.addActionListener(e -> setFontSize((Integer) fontSizeComboBox.getSelectedItem()));
        toolBar.add(fontSizeComboBox);

        JButton boldButton = new JButton("Bold");
        boldButton.addActionListener(e -> toggleBold());
        toolBar.add(boldButton);

        JButton italicButton = new JButton("Italic");
        italicButton.addActionListener(e -> toggleItalic());
        toolBar.add(italicButton);

        JButton underlineButton = new JButton("Underline");
        underlineButton.addActionListener(e -> toggleUnderline());
        toolBar.add(underlineButton);

        JButton colorButton = new JButton("Color");
        colorButton.addActionListener(e -> selectTextColor());
        toolBar.add(colorButton);

        textPane = new JTextPane();
        textPane.setFont(new Font("Arial", Font.PLAIN, 12));
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateStatus();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateStatus();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateStatus();
            }
        });

        JScrollPane scrollPane = new JScrollPane(textPane);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        getContentPane().add(statusPanel, BorderLayout.SOUTH);

        statusLabel = new JLabel("Characters: 0 Words: 0");
        statusPanel.add(statusLabel);

        lineLabel = new JLabel("Line: 1 Column: 1");
        statusPanel.add(lineLabel);

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

        undoManager = new UndoManager();
        textPane.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        doc = textPane.getStyledDocument();
        initializeStyles();

        new Timer(5000, e -> autoSave()).start();
    }

    private void initializeStyles() {
        StyleContext styleContext = StyleContext.getDefaultStyleContext();

        defaultStyle = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
        defaultStyle = styleContext.addAttribute(defaultStyle, StyleConstants.FontFamily, "Arial");
        defaultStyle = styleContext.addAttribute(defaultStyle, StyleConstants.FontSize, 12);

        keywordStyle = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.BLUE);

        commentStyle = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.GRAY);

        stringStyle = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.GREEN);
    }

    private void newFile() {
        textPane.setText("");
        currentFile = null;
        setTitle("Untitled - Text Editor");
    }

    private void openFile() {
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                textPane.read(reader, null);
                setTitle(currentFile.getName() + " - Text Editor");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage());
            }
        }
    }

    private void saveFile() {
        if (currentFile != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                textPane.write(writer);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
            }
        } else {
            saveAsFile();
        }
    }

    private void saveAsFile() {
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            saveFile();
        }
    }

    private void exit() {
        if (textPane.getDocument().getLength() > 0) {
            int option = JOptionPane.showConfirmDialog(this, "Do you want to save changes?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                saveFile();
                System.exit(0);
            } else if (option == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    private void findText() {
        String searchText = JOptionPane.showInputDialog(this, "Find:");
        if (searchText != null && !searchText.isEmpty()) {
            String textContent = textPane.getText();
            int startIndex = textContent.indexOf(searchText);
            if (startIndex != -1) {
                textPane.setCaretPosition(startIndex);
                textPane.select(startIndex, startIndex + searchText.length());
                textPane.grabFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Text not found!");
            }
        }
    }

    private void selectTextColor() {
        Color color = JColorChooser.showDialog(this, "Choose Text Color", textPane.getForeground());
        if (color != null) {
            MutableAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setForeground(attributes, color);
            textPane.setCharacterAttributes(attributes, false);
        }
    }

    private void setFontFamily(String fontFamily) {
        MutableAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attributes, fontFamily);
        textPane.setCharacterAttributes(attributes, false);
    }

    private void setFontSize(int fontSize) {
        MutableAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setFontSize(attributes, fontSize);
        textPane.setCharacterAttributes(attributes, false);
    }

    private void toggleBold() {
        MutableAttributeSet attributes = new SimpleAttributeSet(textPane.getCharacterAttributes());
        boolean isBold = StyleConstants.isBold(attributes);
        StyleConstants.setBold(attributes, !isBold);
        textPane.setCharacterAttributes(attributes, false);
    }

    private void toggleItalic() {
        MutableAttributeSet attributes = new SimpleAttributeSet(textPane.getCharacterAttributes());
        boolean isItalic = StyleConstants.isItalic(attributes);
        StyleConstants.setItalic(attributes, !isItalic);
        textPane.setCharacterAttributes(attributes, false);
    }

    private void toggleUnderline() {
        MutableAttributeSet attributes = new SimpleAttributeSet(textPane.getCharacterAttributes());
        boolean isUnderline = StyleConstants.isUnderline(attributes);
        StyleConstants.setUnderline(attributes, !isUnderline);
        textPane.setCharacterAttributes(attributes, false);
    }

    private void updateStatus() {
        String text = textPane.getText();
        int characters = text.length();
        int words = text.isEmpty() ? 0 : text.split("\\s+").length;
        statusLabel.setText("Characters: " + characters + " Words: " + words);

        int caretPos = textPane.getCaretPosition();
        Element root = doc.getDefaultRootElement();
        int line = root.getElementIndex(caretPos) + 1;
        int column = caretPos - root.getElement(line - 1).getStartOffset() + 1;
        lineLabel.setText("Line: " + line + " Column: " + column);
    }

    private void autoSave() {
        if (currentFile != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                textPane.write(writer);
                setTitle(currentFile.getName() + " - Text Editor (Auto-Saved)");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error auto-saving file: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TextEditor editor = new TextEditor();
            editor.setVisible(true);
        });
    }
}
