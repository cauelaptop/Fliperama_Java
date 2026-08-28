import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class Breakout extends JPanel implements KeyListener, ActionListener {
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;

    enum Estado { MENU, JOGANDO, FIM_DE_JOGO, VITORIA }
    private Estado estadoAtual = Estado.MENU;

    // Raquete
    private int raqueteX = 350;
    private final int LARGURA_RAQUETE = 110;
    private final int ALTURA_RAQUETE = 15;
    private final int RAQUETE_Y = 530;

    // Bola
    private double bolaX = 392, bolaY = 510;
    private double bolaDX = 0, bolaDY = 0;
    private final int TAMANHO_BOLA = 15;
    private boolean bolaPresa = true;

    // Tijolos
    private final int LINHAS_TIJOLOS = 5;
    private final int COLUNAS_TIJOLOS = 10;
    private ArrayList<Tijolo> tijolos = new ArrayList<>();

    // Estatísticas
    private int score = 0;
    private int vidas = 3;
    private int dificuldadeIA = 2; // Usado para velocidade base da bola

    // Controles
    private boolean esquerda, direita;
    private Timer timer;

    public Breakout() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(new Color(15, 15, 15));
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(16, this); // ~60 FPS
        timer.start();
    }

    private void iniciarJogo() {
        score = 0;
        vidas = 3;
        resetarFase();
        estadoAtual = Estado.JOGANDO;
    }

    private void resetarFase() {
        tijolos.clear();
        int larguraTijolo = 70;
        int alturaTijolo = 20;
        int espacamento = 7;
        int margemEsquerda = 15;
        int margemTopo = 60;

        Color[] cores = {
            new Color(255, 80, 80),   // Vermelho (50 pts)
            new Color(255, 160, 50),  // Laranja (40 pts)
            new Color(255, 230, 50),  // Amarelo (30 pts)
            new Color(80, 220, 80),   // Verde (20 pts)
            new Color(80, 180, 255)   // Azul (10 pts)
        };

        for (int i = 0; i < LINHAS_TIJOLOS; i++) {
            for (int j = 0; j < COLUNAS_TIJOLOS; j++) {
                int x = margemEsquerda + j * (larguraTijolo + espacamento);
                int y = margemTopo + i * (alturaTijolo + espacamento);
                int pontos = (LINHAS_TIJOLOS - i) * 10;
                tijolos.add(new Tijolo(x, y, larguraTijolo, alturaTijolo, cores[i], pontos));
            }
        }
        resetarBola();
    }

    private void resetarBola() {
        bolaPresa = true;
        raqueteX = LARGURA / 2 - LARGURA_RAQUETE / 2;
        bolaX = raqueteX + (LARGURA_RAQUETE / 2.0) - (TAMANHO_BOLA / 2.0);
        bolaY = RAQUETE_Y - TAMANHO_BOLA - 2;
        bolaDX = 0;
        bolaDY = 0;
    }

    private void lancarBola() {
        if (bolaPresa) {
            bolaPresa = false;
            double velocidade = 5 + dificuldadeIA;
            bolaDX = (Math.random() > 0.5 ? 1 : -1) * (velocidade - 2);
            bolaDY = -velocidade;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (estadoAtual == Estado.JOGANDO) {
            atualizarJogo();
        }
        repaint();
    }

    private void atualizarJogo() {
        // Movimento da Raquete (A/D ou Setas)
        if (esquerda && raqueteX > 10) raqueteX -= 8;
        if (direita && raqueteX < LARGURA - LARGURA_RAQUETE - 10) raqueteX += 8;

        if (bolaPresa) {
            bolaX = raqueteX + (LARGURA_RAQUETE / 2.0) - (TAMANHO_BOLA / 2.0);
            bolaY = RAQUETE_Y - TAMANHO_BOLA - 2;
            return;
        }

        // Movimento da Bola
        bolaX += bolaDX;
        bolaY += bolaDY;

        // Colisão Parede Esquerda e Direita
        if (bolaX <= 0) {
            bolaX = 0;
            bolaDX *= -1;
        } else if (bolaX >= LARGURA - TAMANHO_BOLA) {
            bolaX = LARGURA - TAMANHO_BOLA;
            bolaDX *= -1;
        }

        // Colisão Parede Superior
        if (bolaY <= 0) {
            bolaY = 0;
            bolaDY *= -1;
        }

        // Queda na parte inferior
        if (bolaY >= ALTURA) {
            vidas--;
            if (vidas <= 0) {
                estadoAtual = Estado.FIM_DE_JOGO;
            } else {
                resetarBola();
            }
        }

        // Colisão Raquete
        Rectangle rectBola = new Rectangle((int) bolaX, (int) bolaY, TAMANHO_BOLA, TAMANHO_BOLA);
        Rectangle rectRaquete = new Rectangle(raqueteX, RAQUETE_Y, LARGURA_RAQUETE, ALTURA_RAQUETE);

        if (rectBola.intersects(rectRaquete) && bolaDY > 0) {
            bolaDY = -Math.abs(bolaDY);
            // Altera o ângulo horizontal baseado em onde a bola atingiu a raquete
            double centroRaquete = raqueteX + (LARGURA_RAQUETE / 2.0);
            double centroBola = bolaX + (TAMANHO_BOLA / 2.0);
            double diferenca = centroBola - centroRaquete;
            bolaDX = diferenca * 0.12;
        }

        // Colisão Tijolos
        for (int i = 0; i < tijolos.size(); i++) {
            Tijolo t = tijolos.get(i);
            if (rectBola.intersects(t.getBounds())) {
                score += t.pontos;
                tijolos.remove(i);

                // Inverte a direção vertical
                bolaDY *= -1;
                break;
            }
        }

        // Vitória ao limpar a tela
        if (tijolos.isEmpty()) {
            estadoAtual = Estado.VITORIA;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (estadoAtual == Estado.MENU) {
            desenharMenu(g2d);
        } else {
            desenharJogo(g2d);
            if (estadoAtual == Estado.FIM_DE_JOGO || estadoAtual == Estado.VITORIA) {
                desenharTelaFinal(g2d);
            }
        }
    }

    private void desenharMenu(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 40));
        g.drawString("BREAKOUT ARCADE", 210, 100);

        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("SELECIONE A DIFICULDADE:", 250, 180);

        g.setColor(Color.GREEN);
        g.drawString("[ 1 ] VELOCIDADE LENTA", 250, 240);
        g.setColor(Color.YELLOW);
        g.drawString("[ 2 ] VELOCIDADE NORMAL", 250, 290);
        g.setColor(Color.RED);
        g.drawString("[ 3 ] VELOCIDADE RAPIDA", 250, 340);

        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.drawString("[ ESC ] VOLTAR A CENTRAL", 270, 480);
    }

    private void desenharJogo(Graphics2D g) {
        // Tijolos
        for (Tijolo t : tijolos) t.desenhar(g);

        // Raquete
        g.setColor(Color.WHITE);
        g.fillRoundRect(raqueteX, RAQUETE_Y, LARGURA_RAQUETE, ALTURA_RAQUETE, 8, 8);

        // Bola
        g.setColor(new Color(255, 220, 100));
        g.fillOval((int) bolaX, (int) bolaY, TAMANHO_BOLA, TAMANHO_BOLA);

        // Placar e Vidas
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("SCORE: " + score, 20, 30);
        g.drawString("VIDAS: " + vidas, LARGURA - 120, 30);

        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.setColor(Color.GRAY);
        if (bolaPresa) {
            g.drawString("ESPAÇO: Lançar Bola | A/D ou Setas: Mover", 250, 30);
        } else {
            g.drawString("ESC: Menu", 20, ALTURA - 15);
        }
    }

    private void desenharTelaFinal(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 210));
        g.fillRect(0, 0, LARGURA, ALTURA);

        g.setFont(new Font("Monospaced", Font.BOLD, 40));
        String titulo = (estadoAtual == Estado.VITORIA) ? "FASE CONCLUIDA!" : "GAME OVER";
        g.setColor((estadoAtual == Estado.VITORIA) ? Color.GREEN : Color.RED);
        
        FontMetrics m = g.getFontMetrics();
        g.drawString(titulo, (LARGURA - m.stringWidth(titulo)) / 2, ALTURA / 2 - 20);

        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.setColor(Color.WHITE);
        String subScore = "Pontuação Final: " + score;
        g.drawString(subScore, (LARGURA - g.getFontMetrics().stringWidth(subScore)) / 2, ALTURA / 2 + 30);

        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(Color.LIGHT_GRAY);
        String msgReset = "Pressione ESPAÇO para jogar novamente";
        g.drawString(msgReset, (LARGURA - g.getFontMetrics().stringWidth(msgReset)) / 2, ALTURA / 2 + 70);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (estadoAtual == Estado.MENU) {
            if (key == KeyEvent.VK_1) { dificuldadeIA = 1; iniciarJogo(); }
            if (key == KeyEvent.VK_2) { dificuldadeIA = 2; iniciarJogo(); }
            if (key == KeyEvent.VK_3) { dificuldadeIA = 4; iniciarJogo(); }
            if (key == KeyEvent.VK_ESCAPE) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) frame.dispose();
            }
        } else if (estadoAtual == Estado.JOGANDO) {
            if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) esquerda = true;
            if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) direita = true;
            if (key == KeyEvent.VK_SPACE) lancarBola();
            if (key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        } else if (estadoAtual == Estado.FIM_DE_JOGO || estadoAtual == Estado.VITORIA) {
            if (key == KeyEvent.VK_SPACE) iniciarJogo();
            if (key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) esquerda = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) direita = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    // Classe auxiliar para os Tijolos
    static class Tijolo {
        int x, y, largura, altura, pontos;
        Color cor;

        public Tijolo(int x, int y, int largura, int altura, Color cor, int pontos) {
            this.x = x; this.y = y;
            this.largura = largura; this.altura = altura;
            this.cor = cor; this.pontos = pontos;
        }

        public void desenhar(Graphics2D g) {
            g.setColor(cor);
            g.fillRoundRect(x, y, largura, altura, 5, 5);
            g.setColor(new Color(0, 0, 0, 80));
            g.drawRoundRect(x, y, largura, altura, 5, 5);
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, largura, altura);
        }
    }
}