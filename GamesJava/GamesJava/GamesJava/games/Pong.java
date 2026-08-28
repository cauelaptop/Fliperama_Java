import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Pong extends JPanel implements KeyListener, ActionListener {
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;

    enum Estado { MENU, JOGANDO, FIM }
    private Estado estadoAtual = Estado.MENU;

    private int dificuldade = 1; // 1 = Fácil, 2 = Médio, 3 = Difícil

    // Raquetes e Bola
    private int raqueteJ1_Y, raqueteCPU_Y;
    private int bolaX, bolaY;
    private int velBolaX, velBolaY;
    private int velCPU;
    private final int RAQUETE_LARGURA = 15;
    private final int RAQUETE_ALTURA = 100;
    private final int BOLA_TAM = 15;

    // Placar
    private int placarJ1 = 0;
    private int placarCPU = 0;
    private String mensagemFim = "";

    private boolean subindo = false, descendo = false;
    private Timer timer;

    public Pong() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        timer = new Timer(16, this); // ~60 FPS
        timer.start();
    }

    private void configurarDificuldade() {
        if (dificuldade == 1) { // FÁCIL
            velBolaX = 5; velBolaY = 5; velCPU = 4;
        } else if (dificuldade == 2) { // MÉDIO
            velBolaX = 8; velBolaY = 8; velCPU = 7;
        } else { // DIFÍCIL
            velBolaX = 12; velBolaY = 12; velCPU = 11;
        }
    }

    private void iniciarJogo() {
        placarJ1 = 0;
        placarCPU = 0;
        resetarRodada();
        estadoAtual = Estado.JOGANDO;
    }

    private void resetarRodada() {
        raqueteJ1_Y = ALTURA / 2 - RAQUETE_ALTURA / 2;
        raqueteCPU_Y = ALTURA / 2 - RAQUETE_ALTURA / 2;
        bolaX = LARGURA / 2 - BOLA_TAM / 2;
        bolaY = ALTURA / 2 - BOLA_TAM / 2;
        configurarDificuldade();
        
        // Joga a bola para o lado de quem tomou o ponto (ou aleatório no começo)
        velBolaX = (Math.random() > 0.5 ? 1 : -1) * Math.abs(velBolaX);
        velBolaY = (Math.random() > 0.5 ? 1 : -1) * Math.abs(velBolaY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (estadoAtual == Estado.JOGANDO) {
            atualizarJogo();
        }
        repaint();
    }

    private void atualizarJogo() {
        // Movimento J1
        if (subindo && raqueteJ1_Y > 0) raqueteJ1_Y -= 8;
        if (descendo && raqueteJ1_Y < ALTURA - RAQUETE_ALTURA) raqueteJ1_Y += 8;

        // Movimento CPU (Persegue a bola)
        int centroRaqueteCPU = raqueteCPU_Y + RAQUETE_ALTURA / 2;
        if (centroRaqueteCPU < bolaY && raqueteCPU_Y < ALTURA - RAQUETE_ALTURA) {
            raqueteCPU_Y += velCPU;
        } else if (centroRaqueteCPU > bolaY && raqueteCPU_Y > 0) {
            raqueteCPU_Y -= velCPU;
        }

        // Movimento Bola
        bolaX += velBolaX;
        bolaY += velBolaY;

        // Colisão com Teto e Chão
        if (bolaY <= 0 || bolaY >= ALTURA - BOLA_TAM) {
            velBolaY = -velBolaY;
        }

        // Colisão Raquetes
        Rectangle rectBola = new Rectangle(bolaX, bolaY, BOLA_TAM, BOLA_TAM);
        Rectangle rectJ1 = new Rectangle(30, raqueteJ1_Y, RAQUETE_LARGURA, RAQUETE_ALTURA);
        Rectangle rectCPU = new Rectangle(LARGURA - 45, raqueteCPU_Y, RAQUETE_LARGURA, RAQUETE_ALTURA);

        if (rectBola.intersects(rectJ1)) {
            velBolaX = Math.abs(velBolaX); // Vai pra direita
        } else if (rectBola.intersects(rectCPU)) {
            velBolaX = -Math.abs(velBolaX); // Vai pra esquerda
        }

        // Pontuação
        if (bolaX <= 0) {
            placarCPU++;
            checarVitoria();
        } else if (bolaX >= LARGURA) {
            placarJ1++;
            checarVitoria();
        }
    }

    private void checarVitoria() {
        if (placarJ1 >= 5) {
            mensagemFim = "VOCÊ VENCEU!";
            estadoAtual = Estado.FIM;
        } else if (placarCPU >= 5) {
            mensagemFim = "CPU VENCEU!";
            estadoAtual = Estado.FIM;
        } else {
            resetarRodada();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (estadoAtual == Estado.MENU) {
            desenharMenu(g2d);
        } else if (estadoAtual == Estado.JOGANDO) {
            desenharJogo(g2d);
        } else {
            desenharJogo(g2d);
            desenharFim(g2d);
        }
    }

    private void desenharMenu(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 60));
        g.drawString("PONG", LARGURA / 2 - 70, 200);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        String dif = "< DIFICULDADE: " + (dificuldade == 1 ? "FÁCIL" : dificuldade == 2 ? "MÉDIO" : "DIFÍCIL") + " >";
        g.drawString(dif, (LARGURA - g.getFontMetrics().stringWidth(dif)) / 2, 300);

        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.drawString("(Setas Esq/Dir para mudar)", LARGURA / 2 - 110, 330);

        g.setColor(Color.GREEN);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.drawString("Pressione ESPAÇO para Iniciar", LARGURA / 2 - 170, 420);
        
        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.drawString("[ ESC ] VOLTAR A CENTRAL", LARGURA / 2 - 115, 480);
    }

    private void desenharJogo(Graphics2D g) {
        // Linha do meio
        g.setColor(Color.DARK_GRAY);
        Stroke dash = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{15}, 0);
        g.setStroke(dash);
        g.drawLine(LARGURA / 2, 0, LARGURA / 2, ALTURA);
        g.setStroke(new BasicStroke(1));

        // Placar
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 50));
        g.drawString(String.valueOf(placarJ1), LARGURA / 2 - 70, 60);
        g.drawString(String.valueOf(placarCPU), LARGURA / 2 + 40, 60);

        // Raquetes e Bola
        g.fillRect(30, raqueteJ1_Y, RAQUETE_LARGURA, RAQUETE_ALTURA);
        g.fillRect(LARGURA - 45, raqueteCPU_Y, RAQUETE_LARGURA, RAQUETE_ALTURA);
        g.fillOval(bolaX, bolaY, BOLA_TAM, BOLA_TAM);
    }

    private void desenharFim(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, LARGURA, ALTURA);

        g.setColor(mensagemFim.contains("VOCÊ") ? Color.GREEN : Color.RED);
        g.setFont(new Font("Monospaced", Font.BOLD, 45));
        g.drawString(mensagemFim, (LARGURA - g.getFontMetrics().stringWidth(mensagemFim)) / 2, ALTURA / 2 - 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        String recomecar = "Pressione ESPAÇO para voltar ao Menu";
        g.drawString(recomecar, (LARGURA - g.getFontMetrics().stringWidth(recomecar)) / 2, ALTURA / 2 + 30);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (estadoAtual == Estado.MENU) {
            if (key == KeyEvent.VK_LEFT) dificuldade = Math.max(1, dificuldade - 1);
            if (key == KeyEvent.VK_RIGHT) dificuldade = Math.min(3, dificuldade + 1);
            if (key == KeyEvent.VK_SPACE) iniciarJogo();
            if (key == KeyEvent.VK_ESCAPE) fecharJogo();
        } else if (estadoAtual == Estado.JOGANDO) {
            if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) subindo = true;
            if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) descendo = true;
            if (key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        } else if (estadoAtual == Estado.FIM) {
            if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) subindo = false;
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) descendo = false;
    }
    
    @Override public void keyTyped(KeyEvent e) {}

    private void fecharJogo() {
        timer.stop();
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) frame.dispose();
    }
}