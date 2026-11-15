import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BOX GAME BRIDGE 1.0 - Linux/X11 prototype
 *
 * Host -> Arduino frame protocol (all multi-byte integers big-endian):
 *   'B' 'X' 0x01 0x01 seq_hi seq_lo tile_count_hi tile_count_lo
 *   repeated tile_count times:
 *     tile_x tile_y (0..19, 0..15), followed by 8*8 RGB565 pixels,
 *     each pixel high byte then low byte (128 bytes per tile).
 *
 * Arduino -> Host serial lines:
 *   J:HH\n = joystick bit mask (hexadecimal)
 *   R\n    = block drawn successfully; PC may send the next tile
 * Joystick bits: 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT, 4=ENTER/SW.
 *
 * Display frame is 160x128 landscape. The Arduino TFT firmware must rotate
 * the ST7735 to landscape and implement the packet format above.
 */
public class BoxGameBridge {
    private static final int OUT_W = 160;
    private static final int OUT_H = 128;
    private static final int TILE = 8;
    private static final int TILE_COLS = OUT_W / TILE;  // 20
    private static final int TILE_ROWS = OUT_H / TILE;  // 16
    private static final int TILE_BYTES = 2 + TILE * TILE * 2; // x,y + RGB565
    private static final int BAUD = 500000;
    private static final int TARGET_FPS = 10;
    // 30 tiles ~= 3.9 KB per 100 ms at 500 kbaud, leaving serial headroom.
    private static final int MAX_TILES_PER_FRAME = 30;

    private final JFrame frame = new JFrame("BOX GAME BRIDGE 1.0");
    private final JLabel gameStatus = new JLabel("Undertale: não localizado");
    private final JLabel portStatus = new JLabel("Arduino: não conectado");
    private final JLabel runStatus = new JLabel("Pronto para procurar os dispositivos.");
    private final JLabel fpsStatus = new JLabel("Transmissão: parada");
    private final JLabel previewLabel = new JLabel("Prévia da captura do PC aparecerá aqui", SwingConstants.CENTER);
    private final JComboBox<String> portBox = new JComboBox<>();
    private final JButton findGameButton = new JButton("Procurar Undertale");
    private final JButton findPortButton = new JButton("Procurar Arduino");
    private final JButton connectButton = new JButton("Conectar Arduino");
    private final JButton startButton = new JButton("Iniciar transmissão");
    private final JButton stopButton = new JButton("Parar");

    private volatile WindowInfo gameWindow;
    private volatile boolean connected = false;
    private volatile boolean streaming = false;
    private volatile int joystickMask = 0;
    private volatile FileInputStream serialIn;
    private volatile FileOutputStream serialOut;
    private Thread serialReaderThread;
    private Thread captureThread;
    // Cada bloco enviado precisa ser confirmado pelo Arduino com uma linha R.
    private final BlockingQueue<Boolean> tileAcks = new LinkedBlockingQueue<>();
    private final AtomicBoolean closing = new AtomicBoolean(false);
    private int sequence = 0;
    private long totalTilesSent = 0;

    private static final int[] KEY_BITS = {1, 2, 4, 8, 16};
    private static final String[] KEY_NAMES = {"Up", "Down", "Left", "Right", "Return"};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BoxGameBridge().show());
    }

    private void show() {
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                shutdown();
                frame.dispose();
            }
        });

        JPanel root = new JPanel();
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("BOX GAME BRIDGE 1.0");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel subtitle = new JLabel("Undertale no PC  →  TFT do Arduino");
        subtitle.setForeground(new Color(30, 145, 170));

        root.add(title);
        root.add(Box.createVerticalStrut(4));
        root.add(subtitle);
        root.add(Box.createVerticalStrut(18));
        root.add(section("1. JANELA DO JOGO", gameStatus,
                row(findGameButton)));
        root.add(Box.createVerticalStrut(12));
        root.add(section("2. PORTA USB DO ARDUINO", portStatus,
                row(portBox, findPortButton, connectButton)));
        root.add(Box.createVerticalStrut(12));
        previewLabel.setPreferredSize(new Dimension(240, 192));
        previewLabel.setMinimumSize(new Dimension(240, 192));
        previewLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        previewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        root.add(section("3. TRANSMISSÃO", runStatus, fpsStatus, previewLabel,
                row(startButton, stopButton)));
        root.add(Box.createVerticalStrut(14));
        JTextArea info = new JTextArea(
                "Meta inicial: até 10 FPS.\n" +
                "A imagem é reduzida para 160x128 e dividida em blocos 8x8.\n" +
                "O PC envia apenas os blocos que mudaram.\n\n" +
                "Controles: joystick = setas; botão SW = Enter.\n" +
                "Requer uma sessão X11/XWayland, wmctrl e xdotool.");
        info.setEditable(false);
        info.setOpaque(false);
        info.setFocusable(false);
        info.setFont(info.getFont().deriveFont(12f));
        root.add(info);

        findGameButton.addActionListener(e -> findGame());
        findPortButton.addActionListener(e -> findPorts());
        connectButton.addActionListener(e -> connectArduino());
        startButton.addActionListener(e -> startStreaming());
        stopButton.addActionListener(e -> stopStreaming());
        stopButton.setEnabled(false);
        startButton.setEnabled(false);

        frame.setContentPane(root);
        frame.pack();
        frame.setMinimumSize(new Dimension(650, 440));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        findGame();
        findPorts();
    }

    private JPanel section(String heading, JComponent... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(heading);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        panel.add(label);
        for (JComponent c : components) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(Box.createVerticalStrut(5));
            panel.add(c);
        }
        return panel;
    }

    private JPanel row(JComponent... components) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JComponent c : components) p.add(c);
        return p;
    }

    private void findGame() {
        try {
            String output = runCommand(List.of("wmctrl", "-lG"), 3);
            WindowInfo found = null;
            for (String line : output.split("\\R")) {
                String[] parts = line.trim().split("\\s+", 8);
                if (parts.length < 8) continue;
                String title = parts[7];
                if (title.toLowerCase(Locale.ROOT).contains("undertale")) {
                    try {
                        found = new WindowInfo(parts[0],
                                Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
                                Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), title);
                        break;
                    } catch (NumberFormatException ignored) { }
                }
            }
            gameWindow = found;
            if (found == null) {
                setLabel(gameStatus, "Undertale: não localizado — abra o jogo primeiro.");
                setLabel(runStatus, "Não encontrei uma janela com 'Undertale' no título.");
            } else {
                setLabel(gameStatus, "Encontrado: " + found.title + " (" + found.width + "x" + found.height + ")");
                setLabel(runStatus, "Janela do jogo localizada.");
            }
            updateStartEnabled();
        } catch (Exception ex) {
            setLabel(gameStatus, "Não consegui listar as janelas: " + ex.getMessage());
            setLabel(runStatus, "Instale wmctrl e confira se está usando X11/XWayland.");
        }
    }

    private void findPorts() {
        String previous = (String) portBox.getSelectedItem();
        portBox.removeAllItems();
        List<String> ports = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("/dev"))) {
            for (Path p : ds) {
                String n = p.getFileName().toString();
                if (n.matches("tty(ACM|USB)[0-9]+")) ports.add(p.toString());
            }
        } catch (IOException ignored) { }
        Collections.sort(ports);
        for (String p : ports) portBox.addItem(p);
        if (previous != null) portBox.setSelectedItem(previous);
        if (ports.isEmpty()) setLabel(portStatus, "Arduino: nenhuma porta /dev/ttyACM* ou /dev/ttyUSB* encontrada.");
        else setLabel(portStatus, "Portas encontradas: " + String.join(", ", ports));
        updateStartEnabled();
    }

    private void connectArduino() {
        if (connected) {
            disconnectArduino();
            return;
        }
        String device = (String) portBox.getSelectedItem();
        if (device == null) {
            setLabel(portStatus, "Nenhuma porta selecionada. Clique em Procurar Arduino.");
            return;
        }
        try {
            // Configure the Linux serial TTY without requiring an extra Java library.
            runCommand(List.of("stty", "-F", device, Integer.toString(BAUD),
                    "raw", "-echo", "-ixon", "-ixoff", "-crtscts"), 3);
            serialIn = new FileInputStream(device);
            serialOut = new FileOutputStream(device);
            Thread.sleep(2200); // Uno may reset when its USB serial port is opened.
            connected = true;
            serialReaderThread = new Thread(this::readArduino, "BOX-Serial-Reader");
            serialReaderThread.setDaemon(true);
            serialReaderThread.start();
            // Avisa ao firmware que a conexão foi aberta, mas a transmissão ainda não começou.
            serialOut.write('W');
            serialOut.flush();
            connectButton.setText("Desconectar");
            setLabel(portStatus, "Conectado em " + device + " a " + BAUD + " baud.");
        } catch (Exception ex) {
            closeSerialStreams();
            connected = false;
            setLabel(portStatus, "Falha ao conectar: " + ex.getMessage());
            setLabel(runStatus, "Confira a porta e a permissão de acesso ao dispositivo.");
        }
        updateStartEnabled();
    }

    private void disconnectArduino() {
        stopStreaming();
        connected = false;
        closeSerialStreams();
        releaseAllKeys();
        connectButton.setText("Conectar Arduino");
        setLabel(portStatus, "Arduino desconectado.");
        updateStartEnabled();
    }

    private void readArduino() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(serialIn, StandardCharsets.US_ASCII))) {
            String line;
            while (connected && (line = reader.readLine()) != null) {
                line = line.trim();
                if (line.equals("R")) {
                    tileAcks.offer(Boolean.TRUE);
                    continue;
                }
                if (line.startsWith("J:") && line.length() >= 4) {
                    try {
                        int newMask = Integer.parseInt(line.substring(2, 4), 16) & 0x1F;
                        handleJoystick(newMask);
                    } catch (NumberFormatException ignored) { }
                }
            }
        } catch (IOException ex) {
            if (connected) {
                setLabel(portStatus, "Leitura serial interrompida: " + ex.getMessage());
                connected = false;
                updateStartEnabled();
            }
        }
    }

    private synchronized void handleJoystick(int newMask) {
        WindowInfo target = gameWindow;
        int old = joystickMask;
        if (target != null) {
            for (int i = 0; i < KEY_BITS.length; i++) {
                boolean wasDown = (old & KEY_BITS[i]) != 0;
                boolean isDown = (newMask & KEY_BITS[i]) != 0;
                if (wasDown != isDown) {
                    runKeyCommand(isDown ? "keydown" : "keyup", target.id, KEY_NAMES[i]);
                }
            }
        }
        joystickMask = newMask;
    }

    private void startStreaming() {
        if (!connected) {
            setLabel(runStatus, "Conecte o Arduino primeiro.");
            return;
        }
        findGame();
        WindowInfo target = gameWindow;
        if (target == null) return;
        try {
            runCommand(List.of("wmctrl", "-ia", target.id), 2);
            Robot robot = new Robot();
            tileAcks.clear();
            streaming = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            totalTilesSent = 0;
            captureThread = new Thread(() -> captureLoop(robot), "BOX-Frame-Capture");
            captureThread.setDaemon(true);
            captureThread.start();
            setLabel(runStatus, "Transmitindo alterações da janela do Undertale...");
        } catch (Exception ex) {
            streaming = false;
            setLabel(runStatus, "Não consegui iniciar a captura: " + ex.getMessage());
            updateStartEnabled();
        }
    }

    private void stopStreaming() {
        streaming = false;
        setLabel(fpsStatus, "Transmissão: parada");
        stopButton.setEnabled(false);
        updateStartEnabled();
    }

    private void captureLoop(Robot robot) {
        int[] lastSent = new int[OUT_W * OUT_H];
        Arrays.fill(lastSent, -1); // force initial full refresh, spread across frames
        long lastReport = System.nanoTime();
        int frames = 0;
        long frameStart = System.nanoTime();
        try {
            while (streaming && connected) {
                long tick = System.nanoTime();
                WindowInfo target = gameWindow;
                if (target == null) break;
                BufferedImage screen = robot.createScreenCapture(
                        new Rectangle(target.x, target.y, target.width, target.height));
                BufferedImage small = fitToDisplay(screen);
                // Prévia mostra exatamente a imagem reduzida que será enviada à TFT.
                SwingUtilities.invokeLater(() -> previewLabel.setIcon(new ImageIcon(small)));
                int sent = sendChangedTiles(small, lastSent);
                frames++;
                long elapsed = System.nanoTime() - tick;
                long sleepNanos = TimeUnit.MILLISECONDS.toNanos(100) - elapsed;
                if (sleepNanos > 0) TimeUnit.NANOSECONDS.sleep(sleepNanos);
                long now = System.nanoTime();
                if (now - lastReport >= TimeUnit.SECONDS.toNanos(1)) {
                    double fps = frames * 1_000_000_000.0 / (now - lastReport);
                    final String status = String.format(Locale.ROOT,
                            "Captura: %.1f FPS | blocos enviados: %d nesta sessão | último quadro: %d blocos",
                            fps, totalTilesSent, sent);
                    SwingUtilities.invokeLater(() -> setLabel(fpsStatus, status));
                    frames = 0;
                    lastReport = now;
                }
            }
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> setLabel(runStatus, "Erro na transmissão: " + ex.getMessage()));
        } finally {
            streaming = false;
            SwingUtilities.invokeLater(() -> {
                stopButton.setEnabled(false);
                updateStartEnabled();
            });
        }
    }

    private BufferedImage fitToDisplay(BufferedImage source) {
        BufferedImage out = new BufferedImage(OUT_W, OUT_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, OUT_W, OUT_H);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        double scale = Math.min((double) OUT_W / source.getWidth(), (double) OUT_H / source.getHeight());
        int w = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int h = Math.max(1, (int) Math.round(source.getHeight() * scale));
        int x = (OUT_W - w) / 2;
        int y = (OUT_H - h) / 2;
        g.drawImage(source, x, y, w, h, null);
        g.dispose();
        return out;
    }

    private int sendChangedTiles(BufferedImage image, int[] baseline)
            throws IOException, InterruptedException {
        if (!connected || serialOut == null) return 0;

        List<int[]> changed = new ArrayList<>(MAX_TILES_PER_FRAME);
        for (int ty = 0; ty < TILE_ROWS && changed.size() < MAX_TILES_PER_FRAME; ty++) {
            for (int tx = 0; tx < TILE_COLS && changed.size() < MAX_TILES_PER_FRAME; tx++) {
                boolean different = false;
                for (int py = 0; py < TILE && !different; py++) {
                    for (int px = 0; px < TILE; px++) {
                        int index = (ty * TILE + py) * OUT_W + (tx * TILE + px);
                        if ((image.getRGB(tx * TILE + px, ty * TILE + py) & 0xFFFFFF) != baseline[index]) {
                            different = true;
                            break;
                        }
                    }
                }
                if (different) changed.add(new int[]{tx, ty});
            }
        }

        int sent = 0;
        for (int[] tile : changed) {
            if (!connected || !streaming) break;

            int tx = tile[0], ty = tile[1];
            ByteArrayOutputStream payload = new ByteArrayOutputStream(8 + TILE_BYTES);
            payload.write('B');
            payload.write('X');
            payload.write(1); // versão do protocolo
            payload.write(1); // pacote de blocos de imagem
            payload.write((sequence >>> 8) & 0xFF);
            payload.write(sequence & 0xFF);
            payload.write(0); // exatamente um bloco por pacote
            payload.write(1);

            payload.write(tx);
            payload.write(ty);
            for (int py = 0; py < TILE; py++) {
                for (int px = 0; px < TILE; px++) {
                    int x = tx * TILE + px;
                    int y = ty * TILE + py;
                    int rgb = image.getRGB(x, y);
                    int r = (rgb >>> 16) & 0xFF;
                    int g = (rgb >>> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    int rgb565 = ((r & 0xF8) << 8) | ((g & 0xFC) << 3) | (b >>> 3);
                    payload.write((rgb565 >>> 8) & 0xFF);
                    payload.write(rgb565 & 0xFF);
                }
            }

            // Envia um único bloco. O Arduino responde R somente depois de desenhá-lo.
            serialOut.write(payload.toByteArray());
            serialOut.flush();

            Boolean ack = null;
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while (ack == null && connected && streaming && System.nanoTime() < deadline) {
                ack = tileAcks.poll(100, TimeUnit.MILLISECONDS);
            }

            if (ack == null) {
                if (!connected || !streaming) break;
                throw new IOException("O Arduino não confirmou o desenho do bloco (sem resposta R).");
            }

            // Só marca os pixels como enviados depois da confirmação do Arduino.
            for (int py = 0; py < TILE; py++) {
                for (int px = 0; px < TILE; px++) {
                    int x = tx * TILE + px;
                    int y = ty * TILE + py;
                    baseline[y * OUT_W + x] = image.getRGB(x, y) & 0xFFFFFF;
                }
            }

            sequence = (sequence + 1) & 0xFFFF;
            sent++;
        }

        totalTilesSent += sent;
        return sent;
    }

    private void runKeyCommand(String action, String windowId, String key) {
        try {
            // Direct X11 window targeting keeps the bridge's Swing window from stealing game input.
            new ProcessBuilder("xdotool", action, "--window", windowId, key)
                    .redirectErrorStream(true).start();
        } catch (IOException ignored) { }
    }

    private void releaseAllKeys() {
        WindowInfo target = gameWindow;
        if (target != null) {
            for (int i = 0; i < KEY_BITS.length; i++) {
                if ((joystickMask & KEY_BITS[i]) != 0) runKeyCommand("keyup", target.id, KEY_NAMES[i]);
            }
        }
        joystickMask = 0;
    }

    private void updateStartEnabled() {
        boolean canStart = connected && gameWindow != null && !streaming;
        SwingUtilities.invokeLater(() -> startButton.setEnabled(canStart));
    }

    private void setLabel(JLabel label, String text) {
        SwingUtilities.invokeLater(() -> label.setText(text));
    }

    private void closeSerialStreams() {
        try { if (serialIn != null) serialIn.close(); } catch (IOException ignored) { }
        try { if (serialOut != null) serialOut.close(); } catch (IOException ignored) { }
        serialIn = null;
        serialOut = null;
    }

    private void shutdown() {
        if (!closing.compareAndSet(false, true)) return;
        stopStreaming();
        connected = false;
        releaseAllKeys();
        closeSerialStreams();
    }

    private static String runCommand(List<String> command, int timeoutSeconds) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(command).redirectErrorStream(true).start();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thread reader = new Thread(() -> {
            try (InputStream in = p.getInputStream()) { in.transferTo(out); }
            catch (IOException ignored) { }
        });
        reader.setDaemon(true);
        reader.start();
        if (!p.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new IOException("tempo esgotado: " + String.join(" ", command));
        }
        reader.join(500);
        int exit = p.exitValue();
        String text = out.toString(StandardCharsets.UTF_8);
        if (exit != 0) throw new IOException(text.isBlank() ? "comando terminou com código " + exit : text.trim());
        return text;
    }

    private static final class WindowInfo {
        final String id;
        final int x, y, width, height;
        final String title;
        WindowInfo(String id, int x, int y, int width, int height, String title) {
            this.id = id; this.x = x; this.y = y;
            this.width = width; this.height = height; this.title = title;
        }
    }
}